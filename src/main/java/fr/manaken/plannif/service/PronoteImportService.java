package fr.manaken.plannif.service;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import fr.manaken.plannif.dto.PronoteClassePreviewItemDTO;
import fr.manaken.plannif.dto.PronoteClassePreviewResponseDTO;
import fr.manaken.plannif.dto.PronoteImportResultDTO;
import fr.manaken.plannif.dto.PronoteProfPreviewItemDTO;
import fr.manaken.plannif.dto.PronoteProfPreviewResponseDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.model.*;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
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
        return importProfesseurs(file, 18.0, true, true, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importProfesseurs(MultipartFile file, Double defaultHeures, boolean createMissingMatieres, boolean assignReferents, boolean updateExisting) throws Exception {
        byte[] bytes = file.getBytes();
        return processProfesseurs(bytes, detectSeparator(bytes), detectCharset(bytes), defaultHeures, createMissingMatieres, assignReferents, updateExisting);
    }

    @Transactional(readOnly = true)
    public PronoteProfPreviewResponseDTO previewProfesseurs(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        if (bytes.length == 0) {
            return new PronoteProfPreviewResponseDTO(false, 0, 0, 0, List.of(), List.of("Le fichier est vide."));
        }
        return processPreviewProfesseurs(bytes, detectSeparator(bytes), detectCharset(bytes));
    }

    @Transactional(readOnly = true)
    public PronoteClassePreviewResponseDTO previewClasses(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        if (bytes.length == 0) {
            return new PronoteClassePreviewResponseDTO(false, 0, 0, 0, List.of(), List.of("Le fichier est vide."));
        }
        return processPreviewClasses(bytes, detectSeparator(bytes), detectCharset(bytes));
    }

    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importClasses(MultipartFile file) throws Exception {
        return importClasses(file, true, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public PronoteImportResultDTO importClasses(MultipartFile file, boolean assignReferents, boolean updateExisting) throws Exception {
        byte[] bytes = file.getBytes();
        return processClasses(bytes, detectSeparator(bytes), detectCharset(bytes), assignReferents, updateExisting);
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

    private PronoteProfPreviewResponseDTO processPreviewProfesseurs(byte[] bytes, char separator, Charset charset) throws Exception {
        List<String> warnings = new ArrayList<>();
        List<PronoteProfPreviewItemDTO> items = new ArrayList<>();
        int countNouveaux = 0;
        int countExistants = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            String[] header = reader.readNext();
            int colCivilite = findColumn(header, "civilite", "civilité", "civ", "civ.", "titre");
            int colNom = findColumn(header, "nom", "nom d'usage", "nom usuel", "nom de famille");
            int colPrenom = findColumn(header, "prenom", "prénom");
            int colPrincipalDe = findColumn(header, "principal de", "principal", "professeur principal", "professeur principal de", "pp de", "referent de", "référent de", "referent", "référent");
            int colEmail = findColumn(header, "email", "courriel", "mel", "e-mail", "adresse e-mail", "adresse email", "adresse e-m", "adresse electronique", "adresse électronique");
            int colMatiere = findColumn(header, "discipline", "disciplines", "matiere", "matière", "matieres", "matières", "affectation_discipline");

            if (colNom == -1) {
                return new PronoteProfPreviewResponseDTO(false, 0, 0, 0, List.of(), List.of("Colonne 'Nom' introuvable dans l'en-tête."));
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

            Map<String, Classe> existingClasses = new HashMap<>();
            for (Classe c : dataFetcher.getClasses()) {
                existingClasses.put(normalize(c.getNom()).toLowerCase(), c);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                if (isLineEmpty(line)) continue;
                total++;

                String civilite = colCivilite != -1 ? getVal(line, colCivilite) : "";
                String nom = getVal(line, colNom);
                String prenom = colPrenom != -1 ? getVal(line, colPrenom) : "";
                if (nom.isBlank()) {
                    warnings.add("Ligne " + total + " ignorée : nom vide.");
                    continue;
                }

                String email = colEmail != -1 ? getVal(line, colEmail) : "";
                String matiereNom = colMatiere != -1 ? getVal(line, colMatiere) : "";
                String principalDe = colPrincipalDe != -1 ? getVal(line, colPrincipalDe) : "";

                List<String> classesPp = new ArrayList<>();
                List<String> classesConnues = new ArrayList<>();
                List<String> classesInconnues = new ArrayList<>();
                if (!principalDe.isBlank()) {
                    String[] tokens = principalDe.split("[,;]");
                    for (String t : tokens) {
                        String clNom = t.trim();
                        if (!clNom.isEmpty()) {
                            classesPp.add(clNom);
                            if (existingClasses.containsKey(normalize(clNom).toLowerCase())) {
                                classesConnues.add(clNom);
                            } else {
                                classesInconnues.add(clNom);
                            }
                        }
                    }
                }

                String key = (normalize(nom) + "_" + normalize(prenom)).toLowerCase();
                Professeur existing = existingProfs.get(key);
                boolean existant = existing != null;
                if (existant) {
                    countExistants++;
                } else {
                    countNouveaux++;
                }

                boolean matiereConnue = matiereNom.isBlank() || existingMatieres.containsKey(normalize(matiereNom).toLowerCase());

                items.add(new PronoteProfPreviewItemDTO(
                        civilite,
                        nom,
                        prenom,
                        email,
                        matiereNom,
                        classesPp,
                        existant,
                        existant ? existing.getId() : null,
                        matiereConnue,
                        classesConnues,
                        classesInconnues
                ));
            }
        }

        return new PronoteProfPreviewResponseDTO(true, total, countNouveaux, countExistants, items, warnings);
    }

    private PronoteImportResultDTO processProfesseurs(byte[] bytes, char separator, Charset charset) throws Exception {
        return processProfesseurs(bytes, separator, charset, 18.0, true, true, true);
    }

    private PronoteImportResultDTO processProfesseurs(
            byte[] bytes, char separator, Charset charset,
            Double defaultHeures, boolean createMissingMatieres, boolean assignReferents, boolean updateExisting
    ) throws Exception {
        List<String> warnings = new ArrayList<>();
        int countCreated = 0;
        int countUpdated = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            String[] header = reader.readNext();
            int colNom = findColumn(header, "nom", "nom d'usage", "nom usuel", "nom de famille");
            int colPrenom = findColumn(header, "prenom", "prénom");
            int colEmail = findColumn(header, "email", "courriel", "mel", "e-mail", "adresse e-mail", "adresse email", "adresse e-m", "adresse electronique", "adresse électronique");
            int colMatiere = findColumn(header, "discipline", "disciplines", "matiere", "matière", "matieres", "matières", "affectation_discipline");
            int colPrincipalDe = findColumn(header, "principal de", "principal", "professeur principal", "professeur principal de", "pp de", "referent de", "référent de", "referent", "référent");

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

            Map<String, Classe> existingClasses = new HashMap<>();
            for (Classe c : dataFetcher.getClasses()) {
                existingClasses.put(normalize(c.getNom()).toLowerCase(), c);
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
                String principalDe = colPrincipalDe != -1 ? getVal(line, colPrincipalDe) : null;

                String key = (normalize(nom) + "_" + normalize(prenom)).toLowerCase();
                Professeur prof = existingProfs.get(key);
                boolean isNew = false;
                if (prof == null) {
                    prof = new Professeur();
                    prof.setNom(nom);
                    prof.setPrenom(prenom);
                    double h = defaultHeures != null ? defaultHeures : 18.0;
                    prof.setNb_heures(BigDecimal.valueOf(h));
                    prof.setMaxHeuresParJour(BigDecimal.valueOf(6.0));
                    prof.setMaxHeuresParSemaine(BigDecimal.valueOf(h));
                    prof.setMaxHeuresParSeance(BigDecimal.valueOf(2.0));
                    prof.setPauseDejeunerDureeMin(45);
                    isNew = true;
                } else if (!updateExisting) {
                    continue;
                }

                if (email != null && !email.isBlank()) {
                    prof.setEmail(email);
                }

                // Associer la matière si présente
                if (matiereNom != null && !matiereNom.isBlank()) {
                    String matKey = normalize(matiereNom).toLowerCase();
                    Matiere mat = existingMatieres.get(matKey);
                    if (mat == null && createMissingMatieres) {
                        mat = new Matiere();
                        mat.setNom(matiereNom.trim());
                        mat = dataPusher.saveMatiere(mat);
                        existingMatieres.put(matKey, mat);
                    }
                    if (mat != null) {
                        if (prof.getMatieres() == null) {
                            prof.setMatieres(new HashSet<>());
                        }
                        prof.getMatieres().add(mat);
                    }
                }

                prof = dataPusher.saveProfesseur(prof);

                // Associer le rôle de professeur principal si renseigné
                if (assignReferents && principalDe != null && !principalDe.isBlank()) {
                    String[] tokens = principalDe.split("[,;]");
                    for (String token : tokens) {
                        String classeNom = token.trim();
                        if (classeNom.isEmpty()) continue;

                        Classe classe = existingClasses.get(normalize(classeNom).toLowerCase());
                        if (classe != null) {
                            if (classe.getReferents() == null) {
                                classe.setReferents(new ArrayList<>());
                            }
                            Long profId = prof.getId();
                            boolean alreadyReferent = classe.getReferents().stream().anyMatch(
                                    r -> r.getProfesseur() != null && Objects.equals(r.getProfesseur().getId(), profId)
                                            && r.getRole() == RoleReferent.PRINCIPAL
                            );
                            if (!alreadyReferent) {
                                ClasseReferent ref = new ClasseReferent();
                                ref.setClasse(classe);
                                ref.setProfesseur(prof);
                                ref.setRole(RoleReferent.PRINCIPAL);
                                classe.getReferents().add(ref);
                                dataPusher.saveClasse(classe);
                            }
                        } else {
                            warnings.add("Classe '" + classeNom + "' introuvable pour le rôle de professeur principal de " + nom + " " + prenom + ".");
                        }
                    }
                }

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
        return processClasses(bytes, separator, charset, true, true);
    }

    private PronoteClassePreviewResponseDTO processPreviewClasses(byte[] bytes, char separator, Charset charset) throws Exception {
        List<String> warnings = new ArrayList<>();
        List<PronoteClassePreviewItemDTO> items = new ArrayList<>();
        int countNouveaux = 0;
        int countExistants = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            ParsedClasseHeader headerInfo = parseClasseHeader(reader);
            if (headerInfo.colNom == -1) {
                return new PronoteClassePreviewResponseDTO(false, 0, 0, 0, List.of(), List.of("Colonne 'Nom' ou 'Classe' introuvable dans l'en-tête."));
            }

            Map<String, Classe> existingClasses = new HashMap<>();
            for (Classe c : dataFetcher.getClasses()) {
                existingClasses.put(normalize(c.getNom()).toLowerCase(), c);
            }

            List<Professeur> allProfs = dataFetcher.getProfesseurs();

            List<String[]> linesToProcess = new ArrayList<>();
            if (headerInfo.firstDataRow != null && !isLineEmpty(headerInfo.firstDataRow)) {
                linesToProcess.add(headerInfo.firstDataRow);
            }
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (!isLineEmpty(row)) {
                    linesToProcess.add(row);
                }
            }

            for (String[] line : linesToProcess) {
                total++;
                String nom = getVal(line, headerInfo.colNom);
                if (nom.isBlank()) {
                    continue;
                }

                String key = normalize(nom).toLowerCase();
                Classe existing = existingClasses.get(key);
                boolean existant = existing != null;
                if (existant) countExistants++; else countNouveaux++;

                Integer effectif = null;
                if (headerInfo.colEffectif != -1) {
                    String effStr = getVal(line, headerInfo.colEffectif);
                    if (!effStr.isBlank()) {
                        try {
                            effectif = Integer.parseInt(effStr.replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                String niveau = headerInfo.colNiveau != -1 ? getVal(line, headerInfo.colNiveau) : "";
                String filiere = headerInfo.colFiliere != -1 ? getVal(line, headerInfo.colFiliere) : "";

                String titulaireRaw = headerInfo.colTitulaire != -1 ? getVal(line, headerInfo.colTitulaire) : "";
                Professeur profT = !titulaireRaw.isBlank() ? findMatchingProfesseur(titulaireRaw, allProfs) : null;

                String suppleantRaw = headerInfo.colSuppleant != -1 ? getVal(line, headerInfo.colSuppleant) : "";
                Professeur profS = !suppleantRaw.isBlank() ? findMatchingProfesseur(suppleantRaw, allProfs) : null;

                items.add(new PronoteClassePreviewItemDTO(
                        nom,
                        effectif,
                        niveau,
                        filiere,
                        titulaireRaw,
                        profT != null,
                        profT != null ? profT.getId() : null,
                        suppleantRaw,
                        profS != null,
                        profS != null ? profS.getId() : null,
                        existant,
                        existant ? existing.getId() : null
                ));
            }
        }

        return new PronoteClassePreviewResponseDTO(true, total, countNouveaux, countExistants, items, warnings);
    }

    private PronoteImportResultDTO processClasses(
            byte[] bytes, char separator, Charset charset,
            boolean assignReferents, boolean updateExisting
    ) throws Exception {
        List<String> warnings = new ArrayList<>();
        int countCreated = 0;
        int countUpdated = 0;
        int total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes), charset))) {
            CSVReader reader = new CSVReaderBuilder(br)
                    .withCSVParser(new CSVParserBuilder().withSeparator(separator).build())
                    .build();

            ParsedClasseHeader headerInfo = parseClasseHeader(reader);
            if (headerInfo.colNom == -1) {
                return new PronoteImportResultDTO(false, "CLASSES", 0, 0, 0, List.of("Colonne 'Nom' ou 'Classe' introuvable dans l'en-tête."));
            }

            Map<String, Classe> existingClasses = new HashMap<>();
            for (Classe c : dataFetcher.getClasses()) {
                existingClasses.put(normalize(c.getNom()).toLowerCase(), c);
            }

            List<Professeur> allProfs = dataFetcher.getProfesseurs();

            List<String[]> linesToProcess = new ArrayList<>();
            if (headerInfo.firstDataRow != null && !isLineEmpty(headerInfo.firstDataRow)) {
                linesToProcess.add(headerInfo.firstDataRow);
            }
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (!isLineEmpty(row)) {
                    linesToProcess.add(row);
                }
            }

            for (String[] line : linesToProcess) {
                total++;
                String nom = getVal(line, headerInfo.colNom);
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
                    c.setHeureDebutMin(LocalTime.of(8, 0));
                    c.setHeureFinMax(LocalTime.of(18, 0));
                    c.setMaxHeuresParJour(8);
                    c.setPauseDejeunerDebutMin(LocalTime.of(12, 0));
                    c.setPauseDejeunerFinMax(LocalTime.of(14, 0));
                    c.setPauseDejeunerDureeMin(60);
                    isNew = true;
                } else if (!updateExisting) {
                    continue;
                }

                if (headerInfo.colEffectif != -1) {
                    String effStr = getVal(line, headerInfo.colEffectif);
                    if (!effStr.isBlank()) {
                        try {
                            c.setEffectif(Integer.parseInt(effStr.replaceAll("[^0-9]", "")));
                        } catch (NumberFormatException ignored) {}
                    }
                }

                if (assignReferents) {
                    if (headerInfo.colTitulaire != -1) {
                        String titulaireRaw = getVal(line, headerInfo.colTitulaire);
                        if (!titulaireRaw.isBlank()) {
                            Professeur profT = findMatchingProfesseur(titulaireRaw, allProfs);
                            if (profT != null) {
                                assignReferent(c, profT, RoleReferent.PRINCIPAL);
                            } else {
                                warnings.add("Professeur principal titulaire '" + titulaireRaw + "' introuvable pour la classe " + nom + ".");
                            }
                        }
                    }

                    if (headerInfo.colSuppleant != -1) {
                        String suppleantRaw = getVal(line, headerInfo.colSuppleant);
                        if (!suppleantRaw.isBlank()) {
                            Professeur profS = findMatchingProfesseur(suppleantRaw, allProfs);
                            if (profS != null) {
                                assignReferent(c, profS, RoleReferent.CO_REFERENT);
                            } else {
                                warnings.add("Professeur principal suppléant '" + suppleantRaw + "' introuvable pour la classe " + nom + ".");
                            }
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

    private void assignReferent(Classe classe, Professeur prof, RoleReferent role) {
        if (classe.getReferents() == null) {
            classe.setReferents(new ArrayList<>());
        }
        Long profId = prof.getId();
        boolean alreadyReferent = classe.getReferents().stream().anyMatch(
                r -> r.getProfesseur() != null && Objects.equals(r.getProfesseur().getId(), profId)
                        && r.getRole() == role
        );
        if (!alreadyReferent) {
            ClasseReferent ref = new ClasseReferent();
            ref.setClasse(classe);
            ref.setProfesseur(prof);
            ref.setRole(role);
            classe.getReferents().add(ref);
        }
    }

    private static class ParsedClasseHeader {
        int colNom = -1;
        int colEffectif = -1;
        int colNiveau = -1;
        int colFiliere = -1;
        int colTitulaire = -1;
        int colSuppleant = -1;
        String[] firstDataRow = null;
    }

    private ParsedClasseHeader parseClasseHeader(CSVReader reader) throws Exception {
        ParsedClasseHeader result = new ParsedClasseHeader();
        String[] line1 = reader.readNext();
        if (line1 == null) return result;

        String[] line2 = reader.readNext();
        String[] effectiveHeader = line1;

        if (line2 != null) {
            boolean line2HasSubHeader = false;
            for (String cell : line2) {
                if (cell != null) {
                    String c = normalize(cell).toLowerCase().trim();
                    if (c.equals("titulaire") || c.equals("suppleant") || c.equals("suppléant")) {
                        line2HasSubHeader = true;
                        break;
                    }
                }
            }

            if (line2HasSubHeader) {
                int maxLen = Math.max(line1.length, line2.length);
                effectiveHeader = new String[maxLen];
                for (int i = 0; i < maxLen; i++) {
                    String p1 = i < line1.length && line1[i] != null ? line1[i].trim() : "";
                    String p2 = i < line2.length && line2[i] != null ? line2[i].trim() : "";
                    if (!p1.isEmpty() && !p2.isEmpty()) {
                        effectiveHeader[i] = p1 + " " + p2;
                    } else if (!p2.isEmpty()) {
                        effectiveHeader[i] = p2;
                    } else {
                        effectiveHeader[i] = p1;
                    }
                }
            } else {
                result.firstDataRow = line2;
            }
        }

        result.colNom = findColumn(effectiveHeader, "nom", "classe", "libelle", "libellé");
        result.colEffectif = findColumn(effectiveHeader, "effectif", "nb_eleves", "nbeleves", "nombre d'eleves", "nombre d'élèves");
        result.colNiveau = findColumn(effectiveHeader, "niveau", "cycle");
        result.colFiliere = findColumn(effectiveHeader, "filiere", "filière", "serie", "série");
        result.colTitulaire = findColumn(effectiveHeader, "titulaire", "prof. principal titulaire", "professeur principal titulaire", "pp titulaire", "prof. principal", "professeur principal", "pp");
        result.colSuppleant = findColumn(effectiveHeader, "suppleant", "suppléant", "prof. principal suppleant", "prof. principal suppléant", "professeur principal suppleant", "pp suppleant", "pp suppléant", "co-referent", "co-référent");

        return result;
    }

    private Professeur findMatchingProfesseur(String raw, List<Professeur> profs) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.replaceAll("(?i)^(m\\.|mme|mlle|monsieur|madame)\\s+", "").trim();
        if (cleaned.isEmpty()) return null;

        String normCleaned = normalize(cleaned).toLowerCase();

        // 1. Exact match "nom_prenom" or "prenom_nom"
        for (Professeur p : profs) {
            String np = (normalize(p.getNom()) + " " + normalize(p.getPrenom())).toLowerCase();
            String pn = (normalize(p.getPrenom()) + " " + normalize(p.getNom())).toLowerCase();
            if (normCleaned.equals(np) || normCleaned.equals(pn)) {
                return p;
            }
        }

        // 2. Token match (e.g., "MARTY Béatrice")
        String[] tokens = normCleaned.split("[\\s,;]+");
        if (tokens.length >= 2) {
            for (Professeur p : profs) {
                String nomNorm = normalize(p.getNom()).toLowerCase();
                String prenomNorm = normalize(p.getPrenom()).toLowerCase();
                boolean matchesNom = false;
                boolean matchesPrenom = false;
                for (String t : tokens) {
                    if (t.equals(nomNorm)) matchesNom = true;
                    if (t.equals(prenomNorm) || (t.length() == 1 && prenomNorm.startsWith(t)) || (t.endsWith(".") && prenomNorm.startsWith(t.substring(0, t.length() - 1)))) {
                        matchesPrenom = true;
                    }
                }
                if (matchesNom && matchesPrenom) {
                    return p;
                }
            }
        }

        // 3. Name-only match if unambiguous
        List<Professeur> matches = new ArrayList<>();
        for (Professeur p : profs) {
            String nomNorm = normalize(p.getNom()).toLowerCase();
            if (normCleaned.equals(nomNorm) || (tokens.length > 0 && tokens[0].equals(nomNorm))) {
                matches.add(p);
            }
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }

        return null;
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
        // 1. Correspondance exacte
        for (int i = 0; i < header.length; i++) {
            String h = normalize(header[i]).toLowerCase().trim();
            for (String candidate : candidates) {
                if (h.equals(normalize(candidate).toLowerCase().trim())) {
                    return i;
                }
            }
        }
        // 2. Correspondance souple (préfixe ou contient)
        for (int i = 0; i < header.length; i++) {
            String h = normalize(header[i]).toLowerCase().trim();
            for (String candidate : candidates) {
                String cand = normalize(candidate).toLowerCase().trim();
                if (h.startsWith(cand) || cand.startsWith(h) || h.contains(cand)) {
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
