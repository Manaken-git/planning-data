package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.ScoreConstraintConfigDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.ScoreConstraintConfigMapper;
import fr.manaken.plannif.model.ScoreConstraintConfig;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreConstraintConfigService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final ScoreConstraintConfigMapper mapper;

    public ScoreConstraintConfigService(DataFetcher dataFetcher, DataPusher dataPusher, ScoreConstraintConfigMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<ScoreConstraintConfigDTO> getAllConfigs() {
        return dataFetcher.getConstraintConfigs().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public ScoreConstraintConfigDTO getConfig(Long id) {
        if (!dataFetcher.existsConstraintConfig(Math.toIntExact(id))) {
            throw new IllegalArgumentException("Configuration de score avec l'ID " + id + " introuvable.");
        }
        return mapper.toDto(dataFetcher.getConstraintConfig(Math.toIntExact(id)));
    }

    public ScoreConstraintConfigDTO getDefaultConfig() {
        return dataFetcher.getDefaultConstraintConfig()
                .map(mapper::toDto)
                .orElseGet(() -> {
                    // Si aucune configuration par défaut n'existe, on en initialise une standard
                    ScoreConstraintConfig defaultConfig = new ScoreConstraintConfig();
                    defaultConfig.setNom("Configuration Lycée par défaut");
                    defaultConfig.setIsDefault(true);
                    return mapper.toDto(dataPusher.saveConstraintConfig(defaultConfig));
                });
    }

    public ScoreConstraintConfigDTO saveConfig(ScoreConstraintConfigDTO dto) {
        ScoreConstraintConfig entity;
        if (dto.id() != null && dataFetcher.existsConstraintConfig(Math.toIntExact(dto.id()))) {
            entity = dataFetcher.getConstraintConfig(Math.toIntExact(dto.id()));
        } else {
            entity = new ScoreConstraintConfig();
        }
        mapper.mergeWDTO(entity, dto);

        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            // S'assurer qu'une seule configuration est marquée par défaut
            for (ScoreConstraintConfig other : dataFetcher.getConstraintConfigs()) {
                if (!other.getId().equals(entity.getId()) && Boolean.TRUE.equals(other.getIsDefault())) {
                    other.setIsDefault(false);
                    dataPusher.saveConstraintConfig(other);
                }
            }
        }

        return mapper.toDto(dataPusher.saveConstraintConfig(entity));
    }

    public void deleteConfig(Long id) {
        dataPusher.deleteConstraintConfig(Math.toIntExact(id));
    }
}
