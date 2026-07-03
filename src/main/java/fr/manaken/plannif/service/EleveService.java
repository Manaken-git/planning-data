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
}

