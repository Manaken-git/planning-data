package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.CreneauDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.CreneauMapper;
import fr.manaken.plannif.model.Creneau;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreneauService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final CreneauMapper mapper;

    public CreneauService(DataFetcher dataFetcher, DataPusher dataPusher, CreneauMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<CreneauDTO> getCreneaux() {
        return dataFetcher.getCreneaux().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public CreneauDTO saveCreneau(CreneauDTO dto) {
        Creneau entity;
        if (dto.id() != null) {
            entity = dataFetcher.getCreneau(Math.toIntExact(dto.id()));
        } else {
            entity = new Creneau();
        }
        mapper.mergeWDTO(entity, dto);
        return mapper.toDto(dataPusher.saveCreneau(entity));
    }

    public void deleteCreneau(Long id) {
        dataPusher.deleteCreneau(Math.toIntExact(id));
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

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                switch (h) {
                    case "id" -> idIndex = i;
                    case "debut", "début" -> debutIndex = i;
                    case "fin" -> finIndex = i;
                }
            }

            if (debutIndex == -1 || finIndex == -1) {
                throw new IllegalArgumentException("Les colonnes 'debut' et 'fin' sont requises.");
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
                    if (dataFetcher.existsCreneau(Math.toIntExact(id))) {
                        throw new IllegalArgumentException("Un créneau avec l'ID " + id + " existe déjà en base.");
                    }
                }

                if (line.length <= Math.max(debutIndex, finIndex)) {
                    continue;
                }
                String debutStr = line[debutIndex].trim();
                String finStr = line[finIndex].trim();

                if (debutStr.isEmpty() || finStr.isEmpty()) {
                    continue;
                }

                java.time.LocalDateTime debut = parseDateTime(debutStr);
                java.time.LocalDateTime fin = parseDateTime(finStr);

                Creneau c = new Creneau();
                if (id != null) {
                    c.setId(id);
                }
                c.setDebut(debut);
                c.setFin(fin);

                dataPusher.saveCreneau(c);
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
