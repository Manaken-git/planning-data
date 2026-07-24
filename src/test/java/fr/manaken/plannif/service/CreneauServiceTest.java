package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.CreneauDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.CreneauMapper;
import fr.manaken.plannif.model.Creneau;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreneauServiceTest {

    @Mock
    private DataFetcher dataFetcher;

    @Mock
    private CreneauMapper mapper;

    @InjectMocks
    private CreneauService creneauService;

    @Test
    void shouldReturnListOfCreneaux() {
        // Given
        Creneau creneau = new Creneau();
        creneau.setId(1L);
        CreneauDTO creneauDTO = new CreneauDTO(1L, LocalDateTime.of(2026, 7, 24, 8, 0), LocalDateTime.of(2026, 7, 24, 10, 0));

        when(dataFetcher.getCreneaux()).thenReturn(List.of(creneau));
        when(mapper.toDto(creneau)).thenReturn(creneauDTO);

        // When
        List<CreneauDTO> result = creneauService.getCreneaux();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(1L);
        assertThat(result.getFirst().debut()).isEqualTo(LocalDateTime.of(2026, 7, 24, 8, 0));
        verify(dataFetcher).getCreneaux();
        verify(mapper).toDto(creneau);
    }
}
