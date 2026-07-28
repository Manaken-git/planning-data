package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.SeanceMapper;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeanceService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final SeanceMapper mapper;

    public SeanceService(DataFetcher dataFetcher, DataPusher dataPusher, SeanceMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<SeanceDTO> getSeances() {
        return dataFetcher.getSeances().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    /**
     * Crée ou met à jour une séance.
     * Les IDs des entités liées (professeur, classe, matière, salle) sont passés
     * séparément car le SeanceDTO est dénormalisé (il contient des libellés, pas des IDs).
     */
    public SeanceDTO saveSeance(SeanceDTO dto, Long professeurId, Long classeId,
                                Long matiereId, Long salleId) {
        Seance entity;
        if (dto.id() != null) {
            entity = dataFetcher.getSeance(Math.toIntExact(dto.id()));
        } else {
            entity = new Seance();
        }
        Seance mapped = mapper.toEntity(dto);
        entity.setDebut(mapped.getDebut());
        entity.setFin(mapped.getFin());
        if (professeurId != null) entity.setProfesseur(dataFetcher.getProfesseur(Math.toIntExact(professeurId)));
        if (classeId != null)     entity.setClasse(dataFetcher.getClasse(Math.toIntExact(classeId)));
        if (matiereId != null)    entity.setMatiere(dataFetcher.getMatiere(Math.toIntExact(matiereId)));
        if (salleId != null)      entity.setSalle(dataFetcher.getSalle(Math.toIntExact(salleId)));
        return mapper.toDto(dataPusher.saveSeance(entity));
    }

    public void deleteSeance(Long id) {
        dataPusher.deleteSeance(Math.toIntExact(id));
    }

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public int importCsv(org.springframework.web.multipart.MultipartFile file) throws Exception {
        try (java.io.Reader reader = new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)) {
            com.opencsv.CSVReader csvReader = new com.opencsv.CSVReaderBuilder(reader)
                    .withCSVParser(new com.opencsv.RFC4180ParserBuilder().build())
                    .build();

            String[] header = csvReader.readNext();
            if (header == null) {
                throw new IllegalArgumentException("Le fichier CSV est vide.");
            }

            int idIndex = -1, debutIndex = -1, finIndex = -1;
            int profIdIndex = -1, classeIdIndex = -1, matiereIdIndex = -1, salleIdIndex = -1;
            int creneauIdIndex = -1, typeIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                switch (h) {
                    case "\uFEFFid", "id" -> idIndex = i;
                    case "\uFEFFdebut", "\uFEFFdébut", "debut", "début" -> debutIndex = i;
                    case "\uFEFFfin", "fin" -> finIndex = i;
                    case "\uFEFFprofesseurid", "\uFEFFprofesseur_id", "\uFEFFprof_id", "professeurid", "professeur_id", "prof_id" -> profIdIndex = i;
                    case "\uFEFFclasseid", "\uFEFFclasse_id", "classeid", "classe_id" -> classeIdIndex = i;
                    case "\uFEFFmatiereid", "\uFEFFmatiere_id", "\uFEFFmatièreid", "\uFEFFmatière_id", "matiereid", "matiere_id", "matièreid", "matière_id" -> matiereIdIndex = i;
                    case "\uFEFFsalleid", "\uFEFFsalle_id", "salleid", "salle_id" -> salleIdIndex = i;
                    case "\uFEFFcreneauid", "\uFEFFcreneau_id", "\uFEFFcréneauid", "\uFEFFcréneau_id", "creneauid", "creneau_id", "créneauid", "créneau_id" -> creneauIdIndex = i;
                    case "\uFEFFtype", "type" -> typeIndex = i;
                }
            }

            if (profIdIndex == -1 || classeIdIndex == -1 || matiereIdIndex == -1 || salleIdIndex == -1) {
                throw new IllegalArgumentException("Les colonnes 'professeurId', 'classeId', 'matiereId' et 'salleId' sont requises.");
            }

            int count = 0;
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length == 0 || (line.length == 1 && line[0].trim().isEmpty())) {
                    continue;
                }

                Long id = null;
                if (idIndex != -1 && idIndex < line.length && !line[idIndex].trim().isEmpty()) {
                    id = Long.parseLong(line[idIndex].trim());
                    if (dataFetcher.existsSeance(Math.toIntExact(id))) {
                        throw new IllegalArgumentException("Une séance avec l'ID " + id + " existe déjà en base.");
                    }
                }

                if (line.length <= Math.max(profIdIndex, Math.max(classeIdIndex, Math.max(matiereIdIndex, salleIdIndex)))) {
                    continue;
                }

                String profIdStr = line[profIdIndex].trim();
                String classeIdStr = line[classeIdIndex].trim();
                String matiereIdStr = line[matiereIdIndex].trim();
                String salleIdStr = line[salleIdIndex].trim();

                if (profIdStr.isEmpty() || classeIdStr.isEmpty() || matiereIdStr.isEmpty() || salleIdStr.isEmpty()) {
                    continue;
                }

                Long profId = Long.parseLong(profIdStr);
                Long classeId = Long.parseLong(classeIdStr);
                Long matiereId = Long.parseLong(matiereIdStr);
                Long salleId = Long.parseLong(salleIdStr);

                if (!dataFetcher.existsProfesseur(Math.toIntExact(profId))) {
                    throw new IllegalArgumentException("Le professeur avec l'ID " + profId + " est introuvable.");
                }
                if (!dataFetcher.existsClasse(Math.toIntExact(classeId))) {
                    throw new IllegalArgumentException("La classe avec l'ID " + classeId + " est introuvable.");
                }
                if (!dataFetcher.existsMatiere(Math.toIntExact(matiereId))) {
                    throw new IllegalArgumentException("La matière avec l'ID " + matiereId + " est introuvable.");
                }
                if (!dataFetcher.existsSalle(Math.toIntExact(salleId))) {
                    throw new IllegalArgumentException("La salle avec l'ID " + salleId + " est introuvable.");
                }

                Seance s = new Seance();
                if (id != null) {
                    s.setId(id);
                }

                s.setProfesseur(dataFetcher.getProfesseur(Math.toIntExact(profId)));
                s.setClasse(dataFetcher.getClasse(Math.toIntExact(classeId)));
                s.setMatiere(dataFetcher.getMatiere(Math.toIntExact(matiereId)));
                s.setSalle(dataFetcher.getSalle(Math.toIntExact(salleId)));

                if (debutIndex != -1 && debutIndex < line.length && !line[debutIndex].trim().isEmpty()) {
                    s.setDebut(parseDateTime(line[debutIndex].trim()));
                }
                if (finIndex != -1 && finIndex < line.length && !line[finIndex].trim().isEmpty()) {
                    s.setFin(parseDateTime(line[finIndex].trim()));
                }

                if (creneauIdIndex != -1 && creneauIdIndex < line.length && !line[creneauIdIndex].trim().isEmpty()) {
                    Long creneauId = Long.parseLong(line[creneauIdIndex].trim());
                    if (!dataFetcher.existsCreneau(Math.toIntExact(creneauId))) {
                        throw new IllegalArgumentException("Le créneau avec l'ID " + creneauId + " est introuvable.");
                    }
                    s.setCreneau(dataFetcher.getCreneau(Math.toIntExact(creneauId)));
                }

                if (typeIndex != -1 && typeIndex < line.length && !line[typeIndex].trim().isEmpty()) {
                    s.setType(Seance.TypeSeance.valueOf(line[typeIndex].trim().toUpperCase()));
                }

                dataPusher.saveSeance(s);
                count++;
            }
            return count;
        }
    }

    private java.time.LocalDateTime parseDateTime(String value) {
        String val = value.trim();
        if (val.contains(" ")) {
            val = val.replace(" ", "T");
        }
        if (val.length() == 16) {
            val += ":00";
        }
        return java.time.LocalDateTime.parse(val);
    }
}

