package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.MatiereClasseConfigDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.MatiereClasseConfigMapper;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.ClassePresence;
import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.model.MatiereClasseConfig;
import fr.manaken.plannif.pusher.DataPusher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatiereClasseConfigServiceTest {

    @Mock
    private DataFetcher dataFetcher;

    @Mock
    private DataPusher dataPusher;

    @Mock
    private MatiereClasseConfigMapper mapper;

    @InjectMocks
    private MatiereClasseConfigService configService;

    @Test
    void shouldSaveConfigWhenClassHasNoPresences() {
        // Given
        MatiereClasseConfigDTO dto = new MatiereClasseConfigDTO(null, 1L, "Classe A", 2L, "Maths", LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 25), 10L);
        Classe classe = new Classe();
        classe.setId(1L);
        classe.setPresences(Collections.emptyList());
        Matiere matiere = new Matiere();
        matiere.setId(2L);

        MatiereClasseConfig entity = new MatiereClasseConfig();
        entity.setClasse(classe);
        entity.setMatiere(matiere);
        entity.setDateDebut(LocalDate.of(2026, 9, 7));
        entity.setDateFin(LocalDate.of(2026, 9, 25));

        doAnswer(invocation -> {
            MatiereClasseConfig c = invocation.getArgument(0);
            c.setDateDebut(dto.dateDebut());
            c.setDateFin(dto.dateFin());
            return null;
        }).when(mapper).mergeWDTO(any(MatiereClasseConfig.class), any(MatiereClasseConfigDTO.class));

        when(dataFetcher.getClasse(1)).thenReturn(classe);
        when(dataFetcher.getMatiere(2)).thenReturn(matiere);
        when(dataPusher.saveMatiereClasseConfig(any(MatiereClasseConfig.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDto(any(MatiereClasseConfig.class))).thenReturn(dto);

        // When
        MatiereClasseConfigDTO result = configService.saveMatiereClasseConfig(dto);

        // Then
        assertThat(result).isNotNull();
        verify(dataPusher).saveMatiereClasseConfig(any(MatiereClasseConfig.class));
    }

    @Test
    void shouldSaveConfigWhenConfigEncompassesOverlappingPresences() {
        // Given
        MatiereClasseConfigDTO dto = new MatiereClasseConfigDTO(null, 1L, "Classe A", 2L, "Maths", LocalDate.of(2026, 9, 7), LocalDate.of(2026, 9, 25), 10L);
        Classe classe = new Classe();
        classe.setId(1L);

        ClassePresence p1 = new ClassePresence();
        p1.setDateDebut(LocalDate.of(2026, 9, 7));
        p1.setDateFin(LocalDate.of(2026, 9, 11)); // overlapping & contained

        ClassePresence p2 = new ClassePresence();
        p2.setDateDebut(LocalDate.of(2026, 9, 14));
        p2.setDateFin(LocalDate.of(2026, 9, 18)); // overlapping & contained

        classe.setPresences(List.of(p1, p2));
        Matiere matiere = new Matiere();
        matiere.setId(2L);

        MatiereClasseConfig entity = new MatiereClasseConfig();
        entity.setClasse(classe);
        entity.setMatiere(matiere);
        entity.setDateDebut(LocalDate.of(2026, 9, 7));
        entity.setDateFin(LocalDate.of(2026, 9, 25));

        doAnswer(invocation -> {
            MatiereClasseConfig c = invocation.getArgument(0);
            c.setDateDebut(dto.dateDebut());
            c.setDateFin(dto.dateFin());
            return null;
        }).when(mapper).mergeWDTO(any(MatiereClasseConfig.class), any(MatiereClasseConfigDTO.class));

        when(dataFetcher.getClasse(1)).thenReturn(classe);
        when(dataFetcher.getMatiere(2)).thenReturn(matiere);
        when(dataPusher.saveMatiereClasseConfig(any(MatiereClasseConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        configService.saveMatiereClasseConfig(dto);

        // Then
        verify(dataPusher).saveMatiereClasseConfig(any(MatiereClasseConfig.class));
    }

    @Test
    void shouldThrowExceptionWhenConfigDoesNotEncompassOverlappingPresence() {
        // Given
        MatiereClasseConfigDTO dto = new MatiereClasseConfigDTO(null, 1L, "Classe A", 2L, "Maths", LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 25), 10L);
        Classe classe = new Classe();
        classe.setId(1L);

        ClassePresence p1 = new ClassePresence();
        p1.setDateDebut(LocalDate.of(2026, 9, 7));
        p1.setDateFin(LocalDate.of(2026, 9, 11)); // overlaps from 9/10 to 9/11 but starts on 9/7 (outside!)

        classe.setPresences(List.of(p1));
        Matiere matiere = new Matiere();
        matiere.setId(2L);

        doAnswer(invocation -> {
            MatiereClasseConfig c = invocation.getArgument(0);
            c.setDateDebut(dto.dateDebut());
            c.setDateFin(dto.dateFin());
            return null;
        }).when(mapper).mergeWDTO(any(MatiereClasseConfig.class), any(MatiereClasseConfigDTO.class));

        when(dataFetcher.getClasse(1)).thenReturn(classe);
        when(dataFetcher.getMatiere(2)).thenReturn(matiere);

        // When / Then
        assertThatThrownBy(() -> configService.saveMatiereClasseConfig(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La période de la configuration de matière doit englober entièrement les périodes de présence");
    }

    @Test
    void shouldThrowExceptionWhenNoPresenceOverlapsConfig() {
        // Given
        MatiereClasseConfigDTO dto = new MatiereClasseConfigDTO(null, 1L, "Classe A", 2L, "Maths", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 10), 10L);
        Classe classe = new Classe();
        classe.setId(1L);

        ClassePresence p1 = new ClassePresence();
        p1.setDateDebut(LocalDate.of(2026, 9, 7));
        p1.setDateFin(LocalDate.of(2026, 9, 11)); // No overlap with Oct 1 - Oct 10

        classe.setPresences(List.of(p1));
        Matiere matiere = new Matiere();
        matiere.setId(2L);

        doAnswer(invocation -> {
            MatiereClasseConfig c = invocation.getArgument(0);
            c.setDateDebut(dto.dateDebut());
            c.setDateFin(dto.dateFin());
            return null;
        }).when(mapper).mergeWDTO(any(MatiereClasseConfig.class), any(MatiereClasseConfigDTO.class));

        when(dataFetcher.getClasse(1)).thenReturn(classe);
        when(dataFetcher.getMatiere(2)).thenReturn(matiere);

        // When / Then
        assertThatThrownBy(() -> configService.saveMatiereClasseConfig(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La période de la configuration de matière ne contient aucune période de présence");
    }

    @Test
    void shouldExportCsv() throws Exception {
        // Given
        Classe classe = new Classe();
        classe.setId(10L);
        Matiere matiere = new Matiere();
        matiere.setId(20L);

        MatiereClasseConfig config = new MatiereClasseConfig();
        config.setId(5L);
        config.setClasse(classe);
        config.setMatiere(matiere);
        config.setDateDebut(LocalDate.of(2026, 9, 7));
        config.setDateFin(LocalDate.of(2026, 9, 25));
        config.setVolumeHorairePeriode(30L);

        when(dataFetcher.getMatiereClasseConfigs()).thenReturn(List.of(config));

        // When
        byte[] csvBytes = configService.exportCsv();

        // Then
        String csvContent = new String(csvBytes, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csvContent).contains("id", "classeId", "matiereId", "dateDebut", "dateFin", "volumeHorairePeriode");
        assertThat(csvContent).contains("5", "10", "20", "2026-09-07", "2026-09-25", "30");
    }
}

