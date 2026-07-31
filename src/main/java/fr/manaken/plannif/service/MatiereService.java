package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.MatiereDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.MatiereMapper;
import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatiereService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final MatiereMapper mapper;

    public MatiereService(DataFetcher dataFetcher, DataPusher dataPusher, MatiereMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<MatiereDTO> getMatieres() {
        return dataFetcher.getMatieres().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public MatiereDTO saveMatiere(MatiereDTO dto) {
        Matiere entity;
        if (dto.id() != null) {
            entity = dataFetcher.getMatiere(Math.toIntExact(dto.id()));
        } else {
            entity = new Matiere();
        }
        mapper.mergeWDTO(entity, dto);
        return mapper.toDto(dataPusher.saveMatiere(entity));
    }

    public void deleteMatiere(Long id) {
        dataPusher.deleteMatiere(Math.toIntExact(id));
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
                    if (dataFetcher.existsMatiere(Math.toIntExact(id))) {
                        throw new IllegalArgumentException("Une matière avec l'ID " + id + " existe déjà en base.");
                    }
                }

                if (line.length <= nomIndex) {
                    continue;
                }
                String nom = line[nomIndex].trim();
                if (nom.isEmpty()) {
                    continue;
                }

                Matiere m = new Matiere();
                if (id != null) {
                    m.setId(id);
                }
                m.setNom(nom);
                dataPusher.saveMatiere(m);
                count++;
            }
            return count;
        }
    }

    public byte[] exportCsv() throws Exception {
        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
             java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(out, java.nio.charset.StandardCharsets.UTF_8);
             com.opencsv.CSVWriter writer = new com.opencsv.CSVWriter(osw)) {

            writer.writeNext(new String[]{"id", "nom"});

            for (fr.manaken.plannif.model.Matiere m : dataFetcher.getMatieres()) {
                writer.writeNext(new String[]{
                        m.getId() != null ? m.getId().toString() : "",
                        m.getNom() != null ? m.getNom() : ""
                });
            }
            writer.flush();
            osw.flush();
            return out.toByteArray();
        }
    }
}

