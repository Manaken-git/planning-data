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
}

