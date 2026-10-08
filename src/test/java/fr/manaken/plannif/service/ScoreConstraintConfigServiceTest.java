package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.ScoreConstraintConfigDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.ScoreConstraintConfigMapper;
import fr.manaken.plannif.model.ScoreConstraintConfig;
import fr.manaken.plannif.pusher.DataPusher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreConstraintConfigServiceTest {

    @Mock
    private DataFetcher dataFetcher;

    @Mock
    private DataPusher dataPusher;

    @Mock
    private ScoreConstraintConfigMapper mapper;

    @InjectMocks
    private ScoreConstraintConfigService service;

    @Test
    void shouldGetAllConfigs() {
        ScoreConstraintConfig entity = new ScoreConstraintConfig();
        entity.setId(1L);
        entity.setNom("Config 1");

        ScoreConstraintConfigDTO dto = new ScoreConstraintConfigDTO(1L, "Config 1", true, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);

        when(dataFetcher.getConstraintConfigs()).thenReturn(List.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        List<ScoreConstraintConfigDTO> result = service.getAllConfigs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nom()).isEqualTo("Config 1");
    }

    @Test
    void shouldGetConfigById() {
        ScoreConstraintConfig entity = new ScoreConstraintConfig();
        entity.setId(2L);
        entity.setNom("Config 2");

        ScoreConstraintConfigDTO dto = new ScoreConstraintConfigDTO(2L, "Config 2", false, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);

        when(dataFetcher.existsConstraintConfig(2)).thenReturn(true);
        when(dataFetcher.getConstraintConfig(2)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        ScoreConstraintConfigDTO result = service.getConfig(2L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(2L);
    }

    @Test
    void shouldThrowWhenConfigNotFound() {
        when(dataFetcher.existsConstraintConfig(99)).thenReturn(false);

        assertThatThrownBy(() -> service.getConfig(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    void shouldReturnDefaultConfigWhenPresent() {
        ScoreConstraintConfig entity = new ScoreConstraintConfig();
        entity.setId(1L);
        entity.setIsDefault(true);

        ScoreConstraintConfigDTO dto = new ScoreConstraintConfigDTO(1L, "Default", true, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);

        when(dataFetcher.getDefaultConstraintConfig()).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(dto);

        ScoreConstraintConfigDTO result = service.getDefaultConfig();

        assertThat(result).isNotNull();
        assertThat(result.isDefault()).isTrue();
    }

    @Test
    void shouldCreateDefaultConfigWhenNoneExists() {
        ScoreConstraintConfig entity = new ScoreConstraintConfig();
        entity.setId(1L);
        entity.setNom("Configuration Lycée par défaut");
        entity.setIsDefault(true);

        ScoreConstraintConfigDTO dto = new ScoreConstraintConfigDTO(1L, "Configuration Lycée par défaut", true, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);

        when(dataFetcher.getDefaultConstraintConfig()).thenReturn(Optional.empty());
        when(dataPusher.saveConstraintConfig(any(ScoreConstraintConfig.class))).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(dto);

        ScoreConstraintConfigDTO result = service.getDefaultConfig();

        assertThat(result).isNotNull();
        assertThat(result.nom()).isEqualTo("Configuration Lycée par défaut");
    }

    @Test
    void shouldSaveConfigAndEnforceSingleDefault() {
        ScoreConstraintConfigDTO inputDto = new ScoreConstraintConfigDTO(null, "Nouvelle Config", true, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);

        ScoreConstraintConfig saved = new ScoreConstraintConfig();
        saved.setId(10L);
        saved.setNom("Nouvelle Config");
        saved.setIsDefault(true);

        ScoreConstraintConfig otherExisting = new ScoreConstraintConfig();
        otherExisting.setId(1L);
        otherExisting.setIsDefault(true);

        doAnswer(invocation -> {
            ScoreConstraintConfig c = invocation.getArgument(0);
            c.setId(10L);
            c.setNom("Nouvelle Config");
            c.setIsDefault(true);
            return null;
        }).when(mapper).mergeWDTO(any(ScoreConstraintConfig.class), any(ScoreConstraintConfigDTO.class));

        when(dataFetcher.getConstraintConfigs()).thenReturn(List.of(saved, otherExisting));
        when(dataPusher.saveConstraintConfig(any(ScoreConstraintConfig.class))).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(new ScoreConstraintConfigDTO(10L, "Nouvelle Config", true, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1));

        ScoreConstraintConfigDTO result = service.saveConfig(inputDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(otherExisting.getIsDefault()).isFalse();
        verify(dataPusher).saveConstraintConfig(otherExisting);
    }
}
