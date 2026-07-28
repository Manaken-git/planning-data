package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.ClasseDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.ClasseMapper;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.pusher.DataPusher;
import fr.manaken.plannif.model.ClassePresence;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClasseService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final ClasseMapper mapper;

    public ClasseService(DataFetcher dataFetcher, DataPusher dataPusher, ClasseMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<ClasseDTO> getClasses() {
        return dataFetcher.getClasses().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ClasseDTO saveClasse(ClasseDTO dto) {
        Classe entity;
        if (dto.id() != null) {
            entity = dataFetcher.getClasse(Math.toIntExact(dto.id()));
        } else {
            entity = new Classe();
        }
        mapper.mergeWDTO(entity, dto);
        
        if (entity.getPresences() != null) {
            validatePresences(entity.getPresences());
            entity.getPresences().forEach(p -> p.setClasse(entity));
        }
        
        return mapper.toDto(dataPusher.saveClasse(entity));
    }

    private void validatePresences(List<ClassePresence> presences) {
        if (presences == null) {
            return;
        }
        for (ClassePresence p : presences) {
            if (p.getDateDebut() == null || p.getDateFin() == null) {
                throw new IllegalArgumentException("Les dates de début et de fin sont requises pour chaque période de présence.");
            }
            if (p.getDateFin().isBefore(p.getDateDebut())) {
                throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début.");
            }
            long days = java.time.temporal.ChronoUnit.DAYS.between(p.getDateDebut(), p.getDateFin()) + 1;
            boolean isValid = (days >= 5 && days <= 7) || (days >= 12 && days <= 14) || (days >= 19 && days <= 21);
            if (!isValid) {
                throw new IllegalArgumentException("Chaque période de présence doit être de 1, 2 ou 3 semaines (entre 5 et 7 jours, 12 et 14 jours, ou 19 et 21 jours).");
            }
        }
    }

    public void deleteClasse(Long id) {
        dataPusher.deleteClasse(Math.toIntExact(id));
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

            int idIndex = -1;
            int nomIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                switch (h) {
                    case "\uFEFFid", "id" -> idIndex = i;
                    case "\uFEFFnom", "nom" -> nomIndex = i;
                }
            }

            if (nomIndex == -1) {
                throw new IllegalArgumentException("La colonne 'nom' est requise dans le fichier CSV.");
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
                    if (dataFetcher.existsClasse(Math.toIntExact(id))) {
                        throw new IllegalArgumentException("Une classe avec l'ID " + id + " existe déjà en base.");
                    }
                }

                if (line.length <= nomIndex) {
                    continue;
                }
                String nom = line[nomIndex].trim();
                if (nom.isEmpty()) {
                    continue;
                }

                Classe c = new Classe();
                if (id != null) {
                    c.setId(id);
                }
                c.setNom(nom);
                dataPusher.saveClasse(c);
                count++;
            }
            return count;
        }
    }
}
