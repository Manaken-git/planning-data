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
            int presencesIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                switch (h) {
                    case "\uFEFFid", "id" -> idIndex = i;
                    case "\uFEFFnom", "nom" -> nomIndex = i;
                    case "\uFEFFpresences", "\uFEFFprésences", "presences", "présences", "périodes", "periodes" -> presencesIndex = i;
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
                }

                if (line.length <= nomIndex) {
                    continue;
                }
                String nom = line[nomIndex].trim();
                if (nom.isEmpty()) {
                    continue;
                }

                Classe c;
                if (id != null && dataFetcher.existsClasse(Math.toIntExact(id))) {
                    c = dataFetcher.getClasse(Math.toIntExact(id));
                } else {
                    c = new Classe();
                    if (id != null) {
                        c.setId(id);
                    }
                }
                c.setNom(nom);

                if (presencesIndex != -1 && presencesIndex < line.length) {
                    String presencesStr = line[presencesIndex].trim();
                    if (!presencesStr.isEmpty()) {
                        java.util.List<fr.manaken.plannif.model.ClassePresence> presenceList = new java.util.ArrayList<>();
                        String[] periods = presencesStr.split(";");
                        for (String period : periods) {
                            String[] dates = period.split(":");
                            if (dates.length == 2) {
                                fr.manaken.plannif.model.ClassePresence cp = new fr.manaken.plannif.model.ClassePresence();
                                cp.setDateDebut(parseDate(dates[0].trim()));
                                cp.setDateFin(parseDate(dates[1].trim()));
                                cp.setClasse(c);
                                presenceList.add(cp);
                            }
                        }
                        validatePresences(presenceList);
                        if (c.getPresences() == null) {
                            c.setPresences(new java.util.ArrayList<>());
                        } else {
                            c.getPresences().clear();
                        }
                        c.getPresences().addAll(presenceList);
                    } else {
                        if (c.getPresences() != null) {
                            c.getPresences().clear();
                        }
                    }
                }

                dataPusher.saveClasse(c);
                count++;
            }
            return count;
        }
    }

    public byte[] exportCsv() throws Exception {
        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
             java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8);
             com.opencsv.CSVWriter writer = new com.opencsv.CSVWriter(osw)) {

            writer.writeNext(new String[]{"id", "nom", "presences"});

            for (fr.manaken.plannif.model.Classe c : dataFetcher.getClasses()) {
                String presencesStr = "";
                if (c.getPresences() != null && !c.getPresences().isEmpty()) {
                    presencesStr = c.getPresences().stream()
                            .map(p -> p.getDateDebut().toString() + ":" + p.getDateFin().toString())
                            .collect(Collectors.joining(";"));
                }

                writer.writeNext(new String[]{
                        c.getId() != null ? c.getId().toString() : "",
                        c.getNom() != null ? c.getNom() : "",
                        presencesStr
                });
            }
            writer.flush();
            osw.flush();
            return out.toByteArray();
        }
    }

    private java.time.LocalDate parseDate(String value) {
        String val = value.trim();
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy");
            return java.time.LocalDate.parse(val, formatter);
        } catch (java.time.format.DateTimeParseException e) {
            return java.time.LocalDate.parse(val);
        }
    }
}

