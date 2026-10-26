package fr.manaken.plannif.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import fr.manaken.plannif.dto.PronoteImportResultDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.model.*;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class PronoteImportService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;

    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    };

    public PronoteImportService(DataFetcher dataFetcher, DataPusher dataPusher) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
    }

    /**
     * Analyse automatiquement le contenu du fichier Pronote et importe l'entité correspondante.
     */
    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importPronoteAuto(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        if (bytes.length == 0) {
            return new PronoteImportResultDTO(false, "INCONNU", 0, 0, 0, List.of("Le fichier est vide."));
        }

        char separator = detectSeparator(bytes);
        Charset charset = detectCharset(bytes);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            String[] header = reader.readNext();
            if (header == null) {
                return new PronoteImportResultDTO(false, "INCONNU", 0, 0, 0, List.of("Aucune ligne d'en-tête trouvée."));
            }

            String detectedType = detectEntityType(header);
            return switch (detectedType) {
                case "PROFESSEURS" -> processProfesseurs(bytes, separator, charset);
                case "CLASSES" -> processClasses(bytes, separator, charset);
                case "SALLES" -> processSalles(bytes, separator, charset);
                case "MATIERES" -> processMatieres(bytes, separator, charset);
                case "PRESENCES" -> processPresences(bytes, separator, charset);
                default -> new PronoteImportResultDTO(false, "INCONNU", 0, 0, 0,
                        List.of("Impossible de déterminer le type de données Pronote à partir des colonnes : " + Arrays.toString(header)));
            };
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importProfesseurs(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        return processProfesseurs(bytes, detectSeparator(bytes), detectCharset(bytes));
    }

    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importClasses(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        return processClasses(bytes, detectSeparator(bytes), detectCharset(bytes));
    }

    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importSalles(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        return processSalles(bytes, detectSeparator(bytes), detectCharset(bytes));
    }

    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importMatieres(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        return processMatieres(bytes, detectSeparator(bytes), detectCharset(bytes));
    }

    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importPresences(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        return processPresences(bytes, detectSeparator(bytes), detectCharset(bytes));
    }

    // =========================================================================
    // Traitement spécifique des entités
    // =========================================================================

    private PronoteImportResultDTO processProfesseurs(byte[] bytes, char separator, Charset charset) throws Exception {
        List<String> warnings = new ArrayList<>();
        int countCreated = 0;
        int countUpdated = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            String[] header = reader.readNext();
            int colNom = findColumn(header, "nom");
            int colPrenom = findColumn(header, "prenom", "prénom");
            int colEmail = findColumn(header, "email", "courriel", "mel", "e-mail");
            int colMatiere = findColumn(header, "matiere", "matière", "discipline");

            if (colNom == -1) {
                return new PronoteImportResultDTO(false, "PROFESSEURS", 0, 0, 0, List.of("Colonne 'Nom' introuvable dans l'en-tête."));
            }

            Map<String, Professeur> existingProfs = new HashMap<>();
            for (Professeur p : dataFetcher.getProfesseurs()) {
                String key = (normalize(p.getNom()) + "_" + normalize(p.getPrenom())).toLowerCase();
                existingProfs.put(key, p);
            }

            Map<String, Matiere> existingMatieres = new HashMap<>();
            for (Matiere m : dataFetcher.getMatieres()) {
                existingMatieres.put(normalize(m.getNom()).toLowerCase(), m);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isLineEmpty(line)) continue;
                total++;

                String nom = getVal(line, colNom);
                String prenom = colPrenom != -1 ? getVal(line, colPrenom) : "";
                if (nom.isBlank()) {
                    warnings.add("Ligne " + total + " ignorée : nom vide.");
                    continue;
                }

                String email = colEmail != -1 ? getVal(line, colEmail) : null;
                String matiereNom = colMatiere != -1 ? getVal(line, colMatiere) : null;

                String key = (normalize(nom) + "_" + normalize(prenom)).toLowerCase();
                Professeur prof = existingProfs.get(key);
                boolean isNew = false;
                if (prof == null) {
                    prof = new Professeur();
                    prof.setNom(nom);
                    prof.setPrenom(prenom);
                    isNew = true;
                }

                if (email != null && !email.isBlank()) {
                    prof.setEmail(email);
                }

                // Associer la matière si présente
                if (matiereNom != null && !matiereNom.isBlank()) {
                    String matKey = normalize(matiereNom).toLowerCase();
                    Matiere mat = existingMatieres.get(matKey);
                    if (mat == null) {
                        mat = new Matiere();
                        mat.setNom(matiereNom.trim());
                        mat = dataPusher.saveMatiere(mat);
                        existingMatieres.put(matKey, mat);
                    }
                    if (prof.getMatieres() == null) {
                        prof.setMatieres(new HashSet<>());
                    }
                    prof.getMatieres().add(mat);
                }

                dataPusher.saveProfesseur(prof);
                if (isNew) {
                    countCreated++;
                    existingProfs.put(key, prof);
                } else {
                    countUpdated++;
                }
            }
        }

        return new PronoteImportResultDTO(true, "PROFESSEURS", total, countCreated, countUpdated, warnings);
    }

    private PronoteImportResultDTO processClasses(byte[] bytes, char separator, Charset charset) throws Exception {
        List<String> warnings = new ArrayList<>();
        int countCreated = 0;
        int countUpdated = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            String[] header = reader.readNext();
            int colNom = findColumn(header, "nom", "classe", "libelle", "libellé");
            int colEffectif = findColumn(header, "effectif", "nb_eleves", "nbeleves");

            if (colNom == -1) {
                return new PronoteImportResultDTO(false, "CLASSES", 0, 0, 0, List.of("Colonne 'Nom' ou 'Classe' introuvable dans l'en-tête."));
            }

            Map<String, Classe> existingClasses = new HashMap<>();
            for (Classe c : dataFetcher.getClasses()) {
                existingClasses.put(normalize(c.getNom()).toLowerCase(), c);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isLineEmpty(line)) continue;
                total++;

                String nom = getVal(line, colNom);
                if (nom.isBlank()) {
                    warnings.add("Ligne " + total + " ignorée : nom de classe vide.");
                    continue;
                }

                String key = normalize(nom).toLowerCase();
                Classe c = existingClasses.get(key);
                boolean isNew = false;
                if (c == null) {
                    c = new Classe();
                    c.setNom(nom);
                    isNew = true;
                }

                if (colEffectif != -1) {
                    String effStr = getVal(line, colEffectif);
                    if (!effStr.isBlank()) {
                        try {
                            c.setEffectif(Integer.parseInt(effStr.replaceAll("[^0-9]", "")));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                dataPusher.saveClasse(c);
                if (isNew) {
                    countCreated++;
                    existingClasses.put(key, c);
                } else {
                    countUpdated++;
                }
            }
        }

        return new PronoteImportResultDTO(true, "CLASSES", total, countCreated, countUpdated, warnings);
    }

    private PronoteImportResultDTO processSalles(byte[] bytes, char separator, Charset charset) throws Exception {
        List<String> warnings = new ArrayList<>();
        int countCreated = 0;
        int countUpdated = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            String[] header = reader.readNext();
            int colCode = findColumn(header, "code", "nom", "salle", "libelle", "libellé");
            int colCapacite = findColumn(header, "capacite", "capacité", "places");
            int colBatiment = findColumn(header, "batiment", "bâtiment", "site");

            if (colCode == -1) {
                return new PronoteImportResultDTO(false, "SALLES", 0, 0, 0, List.of("Colonne 'Code' ou 'Salle' introuvable dans l'en-tête."));
            }

            Map<String, Salle> existingSalles = new HashMap<>();
            for (Salle s : dataFetcher.getSalles()) {
                existingSalles.put(normalize(s.getCode()).toLowerCase(), s);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isLineEmpty(line)) continue;
                total++;

                String code = getVal(line, colCode);
                if (code.isBlank()) {
                    warnings.add("Ligne " + total + " ignorée : code de salle vide.");
                    continue;
                }

                String key = normalize(code).toLowerCase();
                Salle s = existingSalles.get(key);
                boolean isNew = false;
                if (s == null) {
                    s = new Salle();
                    s.setCode(code);
                    isNew = true;
                }

                if (colCapacite != -1) {
                    String capStr = getVal(line, colCapacite);
                    if (!capStr.isBlank()) {
                        try {
                            s.setCapacite(Integer.parseInt(capStr.replaceAll("[^0-9]", "")));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }

                if (colBatiment != -1) {
                    String bat = getVal(line, colBatiment);
                    if (!bat.isBlank()) {
                        s.setBatiment(bat);
                    }
                }

                dataPusher.saveSalle(s);
                if (isNew) {
                    countCreated++;
                    existingSalles.put(key, s);
                } else {
                    countUpdated++;
                }
            }
        }

        return new PronoteImportResultDTO(true, "SALLES", total, countCreated, countUpdated, warnings);
    }

    private PronoteImportResultDTO processMatieres(byte[] bytes, char separator, Charset charset) throws Exception {
        List<String> warnings = new ArrayList<>();
        int countCreated = 0;
        int countUpdated = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            String[] header = reader.readNext();
            int colNom = findColumn(header, "nom", "matiere", "matière", "discipline", "libelle", "libellé");

            if (colNom == -1) {
                return new PronoteImportResultDTO(false, "MATIERES", 0, 0, 0, List.of("Colonne 'Nom' ou 'Matière' introuvable dans l'en-tête."));
            }

            Map<String, Matiere> existingMatieres = new HashMap<>();
            for (Matiere m : dataFetcher.getMatieres()) {
                existingMatieres.put(normalize(m.getNom()).toLowerCase(), m);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isLineEmpty(line)) continue;
                total++;

                String nom = getVal(line, colNom);
                if (nom.isBlank()) {
                    warnings.add("Ligne " + total + " ignorée : nom de matière vide.");
                    continue;
                }

                String key = normalize(nom).toLowerCase();
                Matiere m = existingMatieres.get(key);
                boolean isNew = false;
                if (m == null) {
                    m = new Matiere();
                    m.setNom(nom);
                    isNew = true;
                }

                dataPusher.saveMatiere(m);
                if (isNew) {
                    countCreated++;
                    existingMatieres.put(key, m);
                } else {
                    countUpdated++;
                }
            }
        }

        return new PronoteImportResultDTO(true, "MATIERES", total, countCreated, countUpdated, warnings);
    }

    private PronoteImportResultDTO processPresences(byte[] bytes, char separator, Charset charset) throws Exception {
        List<String> warnings = new ArrayList<>();
        int countCreated = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            String[] header = reader.readNext();
            int colClasse = findColumn(header, "classe", "nom", "libelle", "libellé");
            int colDebut = findColumn(header, "debut", "début", "datedebut", "date_debut", "du");
            int colFin = findColumn(header, "fin", "datefin", "date_fin", "au");
            int colStatut = findColumn(header, "statut", "etat", "état", "type");

            if (colClasse == -1 || colDebut == -1 || colFin == -1) {
                return new PronoteImportResultDTO(false, "PRESENCES", 0, 0, 0,
                        List.of("Colonnes requises introuvables. Nécessite: 'Classe', 'DateDébut' et 'DateFin'."));
            }

            Map<String, Classe> classMap = new HashMap<>();
            for (Classe c : dataFetcher.getClasses()) {
                classMap.put(normalize(c.getNom()).toLowerCase(), c);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isLineEmpty(line)) continue;
                total++;

                String classeNom = getVal(line, colClasse);
                String debutStr = getVal(line, colDebut);
                String finStr = getVal(line, colFin);
                String statut = colStatut != -1 ? getVal(line, colStatut).toLowerCase() : "";

                // Si le statut indique expressément une absence/stage de l'alternant au lycée (ex: "entreprise", "stage"), on l'ignore ou adapte
                if (statut.contains("entreprise") || statut.contains("stage") || statut.contains("absent")) {
                    continue;
                }

                Classe c = classMap.get(normalize(classeNom).toLowerCase());
                if (c == null) {
                    warnings.add("Ligne " + total + " : classe '" + classeNom + "' inexistante, présence ignorée.");
                    continue;
                }

                LocalDate dDebut = parseFlexibleDate(debutStr);
                LocalDate dFin = parseFlexibleDate(finStr);

                if (dDebut == null || dFin == null) {
                    warnings.add("Ligne " + total + " : format de date invalide (Début: " + debutStr + ", Fin: " + finStr + ").");
                    continue;
                }

                if (dFin.isBefore(dDebut)) {
                    warnings.add("Ligne " + total + " : date de fin antérieure à la date de début.");
                    continue;
                }

                if (c.getPresences() == null) {
                    c.setPresences(new ArrayList<>());
                }

                ClassePresence cp = new ClassePresence();
                cp.setClasse(c);
                cp.setDateDebut(dDebut);
                cp.setDateFin(dFin);

                c.getPresences().add(cp);
                dataPusher.saveClasse(c);
                countCreated++;
            }
        }

        return new PronoteImportResultDTO(true, "PRESENCES", total, countCreated, 0, warnings);
    }

    // =========================================================================
    // Détection et utilitaires
    // =========================================================================

    private String detectEntityType(String[] header) {
        String hStr = String.join(" ", header).toLowerCase();
        if (hStr.contains("prenom") || hStr.contains("prénom") || hStr.contains("civilite") || hStr.contains("discipline") || hStr.contains("courriel")) {
            return "PROFESSEURS";
        }
        if (hStr.contains("datedebut") || hStr.contains("date_debut") || hStr.contains("du") && (hStr.contains("au") || hStr.contains("fin"))) {
            return "PRESENCES";
        }
        if (hStr.contains("effectif") || hStr.contains("cycle") || hStr.contains("niveau")) {
            return "CLASSES";
        }
        if (hStr.contains("capacite") || hStr.contains("capacité") || hStr.contains("batiment") || hStr.contains("bâtiment")) {
            return "SALLES";
        }
        if (hStr.contains("matiere") || hStr.contains("matière")) {
            return "MATIERES";
        }
        return "INCONNU";
    }

    private char detectSeparator(byte[] bytes) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.ISO_8859_1))) {
            String firstLine = br.readLine();
            if (firstLine != null) {
                int semiCount = firstLine.split(";", -1).length;
                int tabCount = firstLine.split("\t", -1).length;
                int commaCount = firstLine.split(",", -1).length;

                if (tabCount > semiCount && tabCount > commaCount) return '\t';
                if (semiCount >= commaCount) return ';';
                return ',';
            }
        } catch (IOException ignored) {
        }
        return ';';
    }

    private Charset detectCharset(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return StandardCharsets.UTF_8;
        }
        // Tentative UTF-8 stricte
        try {
            StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes));
            return StandardCharsets.UTF_8;
        } catch (Exception e) {
            return Charset.forName("windows-1252");
        }
    }

    private int findColumn(String[] header, String... candidates) {
        if (header == null) return -1;
        for (int i = 0; i < header.length; i++) {
            String h = normalize(header[i]).toLowerCase();
            for (String candidate : candidates) {
                if (h.equals(normalize(candidate).toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private String getVal(String[] line, int index) {
        if (index >= 0 && index < line.length && line[index] != null) {
            return line[index].trim();
        }
        return "";
    }

    private boolean isLineEmpty(String[] line) {
        if (line == null || line.length == 0) return true;
        for (String cell : line) {
            if (cell != null && !cell.trim().isEmpty()) return false;
        }
        return true;
    }

    private String normalize(String str) {
        if (str == null) return "";
        String clean = str.replace("\uFEFF", "").trim();
        clean = java.text.Normalizer.normalize(clean, java.text.Normalizer.Form.NFD);
        return Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(clean).replaceAll("");
    }

    private LocalDate parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        String clean = dateStr.trim();
        for (DateTimeFormatter dtf : DATE_FORMATS) {
            try {
                return LocalDate.parse(clean, dtf);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
