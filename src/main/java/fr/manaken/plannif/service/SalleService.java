package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.SalleDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.SalleMapper;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalleService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final SalleMapper mapper;

    public SalleService(DataFetcher dataFetcher, DataPusher dataPusher, SalleMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<SalleDTO> getSalles() {
        return dataFetcher.getSalles().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public SalleDTO saveSalle(SalleDTO s) {
        Salle sBDD = null;
        if (s.id() != null) {
            sBDD = dataFetcher.getSalle(Math.toIntExact(s.id()));
        } else {
            sBDD = new Salle();
        }
        mapper.mergeWDTO(sBDD, s);
        return mapper.toDto(dataPusher.saveSalle(sBDD));
    }

    public void deleteSalle(Long id) {
        dataPusher.deleteSalle(Math.toIntExact(id));
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

            int idIndex = -1, codeIndex = -1, capaciteIndex = -1, typeIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                switch (h) {
                    case "\uFEFFid", "id" -> idIndex = i;
                    case "\uFEFFcode", "code" -> codeIndex = i;
                    case "\uFEFFcapacite", "\uFEFFcapacité", "capacite", "capacité" -> capaciteIndex = i;
                    case "\uFEFFtype", "type" -> typeIndex = i;
                }
            }

            if (codeIndex == -1) {
                throw new IllegalArgumentException("La colonne 'code' est requise.");
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
                    if (dataFetcher.existsSalle(Math.toIntExact(id))) {
                        throw new IllegalArgumentException("Une salle avec l'ID " + id + " existe déjà en base.");
                    }
                }

                if (line.length <= codeIndex) {
                    continue;
                }
                String code = line[codeIndex].trim();
                if (code.isEmpty()) {
                    continue;
                }

                Salle s = new Salle();
                if (id != null) {
                    s.setId(id);
                }
                s.setCode(code);

                if (capaciteIndex != -1 && capaciteIndex < line.length && !line[capaciteIndex].trim().isEmpty()) {
                    s.setCapacite(Integer.parseInt(line[capaciteIndex].trim()));
                }
                if (typeIndex != -1 && typeIndex < line.length) {
                    s.setType(line[typeIndex].trim());
                }

                dataPusher.saveSalle(s);
                count++;
            }
            return count;
        }
    }
}
