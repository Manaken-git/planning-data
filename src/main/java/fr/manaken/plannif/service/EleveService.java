package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.EleveMapper;
import fr.manaken.plannif.model.Eleve;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EleveService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final EleveMapper mapper;

    public EleveService(DataFetcher dataFetcher, DataPusher dataPusher, EleveMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<EleveDTO> getEleves(Long idClasse) {
        return dataFetcher.getElevesByClasse(idClasse).stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public List<EleveDTO> getAllEleves() {
        return dataFetcher.getAllEleves().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public EleveDTO saveEleve(EleveDTO dto, Long classeId) {
        Eleve entity;
        if (dto.id() != null) {
            entity = dataFetcher.getEleve(Math.toIntExact(dto.id()));
        } else {
            entity = new Eleve();
        }
        mapper.mergeWDTO(entity, dto);
        if (classeId != null) {
            entity.setClasse(dataFetcher.getClasse(Math.toIntExact(classeId)));
        }
        return mapper.toDto(dataPusher.saveEleve(entity));
    }

    public void deleteEleve(Long id) {
        dataPusher.deleteEleve(Math.toIntExact(id));
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

            int idIndex = -1, nomIndex = -1, prenomIndex = -1, classeIdIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                switch (h) {
                    case "id" -> idIndex = i;
                    case "nom" -> nomIndex = i;
                    case "prenom", "prénom" -> prenomIndex = i;
                    case "classeid", "classe_id" -> classeIdIndex = i;
                }
            }

            if (nomIndex == -1 || prenomIndex == -1 || classeIdIndex == -1) {
                throw new IllegalArgumentException("Les colonnes 'nom', 'prenom' et 'classeId' sont requises.");
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
                    if (dataFetcher.existsEleve(Math.toIntExact(id))) {
                        throw new IllegalArgumentException("Un élève avec l'ID " + id + " existe déjà en base.");
                    }
                }

                if (line.length <= Math.max(nomIndex, Math.max(prenomIndex, classeIdIndex))) {
                    continue;
                }
                String nom = line[nomIndex].trim();
                String prenom = line[prenomIndex].trim();
                String classeIdStr = line[classeIdIndex].trim();

                if (nom.isEmpty() || prenom.isEmpty() || classeIdStr.isEmpty()) {
                    continue;
                }

                Long classeId = Long.parseLong(classeIdStr);
                if (!dataFetcher.existsClasse(Math.toIntExact(classeId))) {
                    throw new IllegalArgumentException("La classe avec l'ID " + classeId + " est introuvable en base de données.");
                }
                fr.manaken.plannif.model.Classe classe = dataFetcher.getClasse(Math.toIntExact(classeId));

                Eleve e = new Eleve();
                if (id != null) {
                    e.setId(id);
                }
                e.setNom(nom);
                e.setPrenom(prenom);
                e.setClasse(classe);

                dataPusher.saveEleve(e);
                count++;
            }
            return count;
        }
    }
}

