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
}
