package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.MatiereClasseConfigDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.MatiereClasseConfigMapper;
import fr.manaken.plannif.model.MatiereClasseConfig;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatiereClasseConfigService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final MatiereClasseConfigMapper mapper;

    public MatiereClasseConfigService(DataFetcher dataFetcher, DataPusher dataPusher, MatiereClasseConfigMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<MatiereClasseConfigDTO> getMatiereClasseConfigs() {
        return dataFetcher.getMatiereClasseConfigs().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public MatiereClasseConfigDTO saveMatiereClasseConfig(MatiereClasseConfigDTO dto) {
        MatiereClasseConfig entity;
        if (dto.id() != null) {
            entity = dataFetcher.getMatiereClasseConfig(Math.toIntExact(dto.id()));
        } else {
            entity = new MatiereClasseConfig();
        }
        
        mapper.mergeWDTO(entity, dto);
        
        if (dto.classeId() != null) {
            entity.setClasse(dataFetcher.getClasse(Math.toIntExact(dto.classeId())));
        }
        if (dto.matiereId() != null) {
            entity.setMatiere(dataFetcher.getMatiere(Math.toIntExact(dto.matiereId())));
        }

        validateConfigDatesAgainstPresences(entity);
        
        return mapper.toDto(dataPusher.saveMatiereClasseConfig(entity));
    }

    public void deleteMatiereClasseConfig(Long id) {
        dataPusher.deleteMatiereClasseConfig(Math.toIntExact(id));
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

            int idIndex = -1, classeIdIndex = -1, matiereIdIndex = -1;
            int dateDebutIndex = -1, dateFinIndex = -1, volumeIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                switch (h) {
                    case "\uFEFFid", "id" -> idIndex = i;
                    case "\uFEFFclasseid", "classeid",  "classe_id" -> classeIdIndex = i;
                    case "\uFEFFmatiereid","matiereid", "matiere_id", "matièreid", "matière_id" -> matiereIdIndex = i;
                    case "\uFEFFdatedebut","datedebut", "date_debut" -> dateDebutIndex = i;
                    case "\uFEFFdatefin", "datefin",   "date_fin" -> dateFinIndex = i;
                    case "\uFEFFvolumehoraireperiode", "volumehoraireperiode",    "volume_horaire_periode" -> volumeIndex = i;
                }
            }

            if (classeIdIndex == -1 || matiereIdIndex == -1) {
                throw new IllegalArgumentException("Les colonnes 'classeId' et 'matiereId' sont requises.");
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
                    if (dataFetcher.existsMatiereClasseConfig(Math.toIntExact(id))) {
                        throw new IllegalArgumentException("Une configuration avec l'ID " + id + " existe déjà en base.");
                    }
                }

                if (line.length <= Math.max(classeIdIndex, matiereIdIndex)) {
                    continue;
                }
                String classeIdStr = line[classeIdIndex].trim();
                String matiereIdStr = line[matiereIdIndex].trim();

                if (classeIdStr.isEmpty() || matiereIdStr.isEmpty()) {
                    continue;
                }

                Long classeId = Long.parseLong(classeIdStr);
                Long matiereId = Long.parseLong(matiereIdStr);

                if (!dataFetcher.existsClasse(Math.toIntExact(classeId))) {
                    throw new IllegalArgumentException("La classe avec l'ID " + classeId + " est introuvable en base.");
                }
                if (!dataFetcher.existsMatiere(Math.toIntExact(matiereId))) {
                    throw new IllegalArgumentException("La matière avec l'ID " + matiereId + " est introuvable en base.");
                }

                fr.manaken.plannif.model.Classe classe = dataFetcher.getClasse(Math.toIntExact(classeId));
                fr.manaken.plannif.model.Matiere matiere = dataFetcher.getMatiere(Math.toIntExact(matiereId));

                MatiereClasseConfig config = new MatiereClasseConfig();
                if (id != null) {
                    config.setId(id);
                }
                config.setClasse(classe);
                config.setMatiere(matiere);

                if (dateDebutIndex != -1 && dateDebutIndex < line.length && !line[dateDebutIndex].trim().isEmpty()) {
                    config.setDateDebut(parseDate(line[dateDebutIndex].trim()));
                }
                if (dateFinIndex != -1 && dateFinIndex < line.length && !line[dateFinIndex].trim().isEmpty()) {
                    config.setDateFin(parseDate(line[dateFinIndex].trim()));
                }
                if (volumeIndex != -1 && volumeIndex < line.length && !line[volumeIndex].trim().isEmpty()) {
                    config.setVolumeHorairePeriode(Long.parseLong(line[volumeIndex].trim()));
                }

                validateConfigDatesAgainstPresences(config);

                dataPusher.saveMatiereClasseConfig(config);
                count++;
            }
            return count;
        }
    }

    private void validateConfigDatesAgainstPresences(MatiereClasseConfig entity) {
        if (entity.getClasse() == null || entity.getClasse().getId() == null || entity.getDateDebut() == null || entity.getDateFin() == null) {
            return;
        }

        if (entity.getDateFin().isBefore(entity.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début.");
        }

        fr.manaken.plannif.model.Classe classe = dataFetcher.getClasse(Math.toIntExact(entity.getClasse().getId()));
        java.util.List<fr.manaken.plannif.model.ClassePresence> presences = classe.getPresences();
        if (presences == null || presences.isEmpty()) {
            return;
        }

        boolean hasOverlappingPresence = false;
        for (fr.manaken.plannif.model.ClassePresence presence : presences) {
            if (presence.getDateDebut() == null || presence.getDateFin() == null) {
                continue;
            }
            boolean overlaps = !presence.getDateFin().isBefore(entity.getDateDebut()) 
                    && !presence.getDateDebut().isAfter(entity.getDateFin());
            if (overlaps) {
                hasOverlappingPresence = true;
                boolean isContained = !presence.getDateDebut().isBefore(entity.getDateDebut()) 
                        && !presence.getDateFin().isAfter(entity.getDateFin());
                if (!isContained) {
                    throw new IllegalArgumentException("La période de la configuration de matière doit englober entièrement les périodes de présence de la classe. La période de présence (" 
                            + presence.getDateDebut() + " à " + presence.getDateFin() + ") n'est pas entièrement englobée dans la période de la configuration ("
                            + entity.getDateDebut() + " à " + entity.getDateFin() + ").");
                }
            }
        }

        if (!hasOverlappingPresence) {
            throw new IllegalArgumentException("La période de la configuration de matière ne contient aucune période de présence de la classe.");
        }
    }

    private java.time.LocalDate parseDate(String value) {
        String val = value.trim();
        try {
            // Essayer d'abord le format dd/MM/yyyy (ou d/M/yyyy pour être plus tolérant)
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy");
            return java.time.LocalDate.parse(val, formatter);
        } catch (java.time.format.DateTimeParseException e) {
            // Fallback sur le format standard yyyy-MM-dd
            return java.time.LocalDate.parse(val);
        }
    }
}
