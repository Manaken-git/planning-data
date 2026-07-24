package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.MatiereClasseConfigDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.MatiereClasseConfigMapper;
import fr.manaken.plannif.model.MatiereClasseConfig;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatiereClasseConfigService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final MatiereClasseConfigMapper mapper;

    public MatiereClasseConfigService(DataFetcher dataFetcher, DataPusher dataPusher, MatiereClasseConfigMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<MatiereClasseConfigDTO> getMatiereClasseConfigs() {
        return dataFetcher.getMatiereClasseConfigs().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public MatiereClasseConfigDTO saveMatiereClasseConfig(MatiereClasseConfigDTO dto) {
        MatiereClasseConfig entity;
        if (dto.id() != null) {
            entity = dataFetcher.getMatiereClasseConfig(Math.toIntExact(dto.id()));
        } else {
            entity = new MatiereClasseConfig();
        }
        
        mapper.mergeWDTO(entity, dto);
        
        if (dto.classeId() != null) {
            entity.setClasse(dataFetcher.getClasse(Math.toIntExact(dto.classeId())));
        }
        if (dto.matiereId() != null) {
            entity.setMatiere(dataFetcher.getMatiere(Math.toIntExact(dto.matiereId())));
        }
        
        return mapper.toDto(dataPusher.saveMatiereClasseConfig(entity));
    }

    public void deleteMatiereClasseConfig(Long id) {
        dataPusher.deleteMatiereClasseConfig(Math.toIntExact(id));
    }
}
