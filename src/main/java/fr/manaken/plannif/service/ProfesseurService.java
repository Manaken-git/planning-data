package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.ProfesseurMapper;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfesseurService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final ProfesseurMapper mapper;

    public ProfesseurService(DataFetcher dataFetcher, DataPusher dataPusher, ProfesseurMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<ProfesseurDTO> getProfesseurs() {
        return dataFetcher.getProfesseurs().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public ProfesseurDTO saveProfesseur(ProfesseurDTO p) {
        Professeur pBDD = null;
        if (p.id() != null) {
            pBDD = dataFetcher.getProfesseur(Math.toIntExact(p.id()));
        } else {
            pBDD = new Professeur();
        }
        mapper.mergeWDTO(pBDD, p);
        return mapper.toDto(dataPusher.saveProfesseur(pBDD));
    }

    public void deleteProfesseur(Long id) {
        dataPusher.deleteProfesseur(Math.toIntExact(id));
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

            int idIndex = -1, nomIndex = -1, prenomIndex = -1, emailIndex = -1;
            int nbHeuresIndex = -1, maxHJourIndex = -1, maxHSemaineIndex = -1, maxHSeanceIndex = -1;

            for (int i = 0; i < header.length; i++) {
                String h = header[i].trim().toLowerCase();
                switch (h) {
                    case "\uFEFFid", "id" -> idIndex = i;
                    case "\uFEFFnom", "nom" -> nomIndex = i;
                    case "\uFEFFprenom", "\uFEFFprénom", "prenom", "prénom" -> prenomIndex = i;
                    case "\uFEFFemail", "email" -> emailIndex = i;
                    case "\uFEFFnb_heures", "\uFEFFnbheures", "nb_heures", "nbheures" -> nbHeuresIndex = i;
                    case "\uFEFFmaxheuresparjour", "\uFEFFmax_heures_par_jour", "maxheuresparjour", "max_heures_par_jour" -> maxHJourIndex = i;
                    case "\uFEFFmaxheuresparsemaine", "\uFEFFmax_heures_par_semaine", "maxheuresparsemaine", "max_heures_par_semaine" -> maxHSemaineIndex = i;
                    case "\uFEFFmaxheuresparseance", "\uFEFFmax_heures_par_seance", "maxheuresparseance", "max_heures_par_seance" -> maxHSeanceIndex = i;
                }
            }

            if (nomIndex == -1 || prenomIndex == -1) {
                throw new IllegalArgumentException("Les colonnes 'nom' et 'prenom' sont requises.");
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
                    if (dataFetcher.existsProfesseur(Math.toIntExact(id))) {
                        throw new IllegalArgumentException("Un professeur avec l'ID " + id + " existe déjà en base.");
                    }
                }

                if (line.length <= Math.max(nomIndex, prenomIndex)) {
                    continue;
                }
                String nom = line[nomIndex].trim();
                String prenom = line[prenomIndex].trim();
                if (nom.isEmpty() || prenom.isEmpty()) {
                    continue;
                }

                Professeur p = new Professeur();
                if (id != null) {
                    p.setId(id);
                }
                p.setNom(nom);
                p.setPrenom(prenom);

                if (emailIndex != -1 && emailIndex < line.length) {
                    p.setEmail(line[emailIndex].trim());
                }
                if (nbHeuresIndex != -1 && nbHeuresIndex < line.length && !line[nbHeuresIndex].trim().isEmpty()) {
                    p.setNb_heures(new java.math.BigDecimal(line[nbHeuresIndex].trim()));
                }
                if (maxHJourIndex != -1 && maxHJourIndex < line.length && !line[maxHJourIndex].trim().isEmpty()) {
                    p.setMaxHeuresParJour(new java.math.BigDecimal(line[maxHJourIndex].trim()));
                }
                if (maxHSemaineIndex != -1 && maxHSemaineIndex < line.length && !line[maxHSemaineIndex].trim().isEmpty()) {
                    p.setMaxHeuresParSemaine(new java.math.BigDecimal(line[maxHSemaineIndex].trim()));
                }
                if (maxHSeanceIndex != -1 && maxHSeanceIndex < line.length && !line[maxHSeanceIndex].trim().isEmpty()) {
                    p.setMaxHeuresParSeance(new java.math.BigDecimal(line[maxHSeanceIndex].trim()));
                }

                dataPusher.saveProfesseur(p);
                count++;
            }
            return count;
        }
    }
}
