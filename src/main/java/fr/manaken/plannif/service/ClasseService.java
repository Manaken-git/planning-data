package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.ClasseDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.ClasseMapper;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.pusher.DataPusher;
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
        return mapper.toDto(dataPusher.saveClasse(entity));
    }

    public void deleteClasse(Long id) {
        dataPusher.deleteClasse(Math.toIntExact(id));
    }
}
