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
}
