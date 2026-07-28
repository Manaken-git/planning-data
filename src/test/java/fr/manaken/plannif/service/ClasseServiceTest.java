package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.ClasseDTO;
import fr.manaken.plannif.dto.ClassePresenceDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.ClasseMapper;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.ClassePresence;
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
class ClasseServiceTest {

    @Mock
    private DataFetcher dataFetcher;

    @Mock
    private DataPusher dataPusher;

    @Mock
    private ClasseMapper mapper;

    @InjectMocks
    private ClasseService classeService;

    @Test
    void shouldSaveClasseWithNoPresences() {
        // Given
        ClasseDTO inputDTO = new ClasseDTO(null, "Alternance A", null, null, null);
        Classe entity = new Classe();
        entity.setNom("Alternance A");
        ClasseDTO outputDTO = new ClasseDTO(1L, "Alternance A", null, null, null);

        doAnswer(invocation -> {
            Classe c = invocation.getArgument(0);
            c.setId(1L);
            return null;
        }).when(mapper).mergeWDTO(any(Classe.class), any(ClasseDTO.class));
        
        when(dataPusher.saveClasse(any(Classe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(Classe.class))).thenReturn(outputDTO);

        // When
        ClasseDTO result = classeService.saveClasse(inputDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        verify(dataPusher).saveClasse(any(Classe.class));
    }

    @Test
    void shouldSaveClasseWithValidPresencePeriods() {
        // Given
        ClasseDTO inputDTO = new ClasseDTO(null, "Alternance A", null, null, Collections.emptyList());
        Classe entity = new Classe();
        entity.setNom("Alternance A");

        ClassePresence p1 = new ClassePresence();
        p1.setDateDebut(LocalDate.of(2026, 9, 7)); // Monday
        p1.setDateFin(LocalDate.of(2026, 9, 11)); // Friday (5 days -> 1 week)

        ClassePresence p2 = new ClassePresence();
        p2.setDateDebut(LocalDate.of(2026, 9, 14)); // Monday
        p2.setDateFin(LocalDate.of(2026, 9, 27)); // Sunday (14 days -> 2 weeks)

        ClassePresence p3 = new ClassePresence();
        p3.setDateDebut(LocalDate.of(2026, 10, 5)); // Monday
        p3.setDateFin(LocalDate.of(2026, 10, 23)); // Friday (19 days -> 3 weeks)

        entity.setPresences(new ArrayList<>(List.of(p1, p2, p3)));

        doAnswer(invocation -> {
            Classe c = invocation.getArgument(0);
            c.setPresences(entity.getPresences());
            return null;
        }).when(mapper).mergeWDTO(any(Classe.class), any(ClasseDTO.class));

        when(dataPusher.saveClasse(any(Classe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        classeService.saveClasse(inputDTO);

        // Then
        assertThat(p1.getClasse()).isEqualTo(entity);
        assertThat(p2.getClasse()).isEqualTo(entity);
        assertThat(p3.getClasse()).isEqualTo(entity);
        verify(dataPusher).saveClasse(any(Classe.class));
    }

    @Test
    void shouldThrowExceptionForInvalidPresenceDuration() {
        // Given
        ClasseDTO inputDTO = new ClasseDTO(null, "Alternance A", null, null, Collections.emptyList());
        Classe entity = new Classe();
        
        ClassePresence p = new ClassePresence();
        p.setDateDebut(LocalDate.of(2026, 9, 7)); // Monday
        p.setDateFin(LocalDate.of(2026, 9, 10)); // Thursday (4 days -> invalid)
        entity.setPresences(List.of(p));

        doAnswer(invocation -> {
            Classe c = invocation.getArgument(0);
            c.setPresences(entity.getPresences());
            return null;
        }).when(mapper).mergeWDTO(any(Classe.class), any(ClasseDTO.class));

        // When / Then
        assertThatThrownBy(() -> classeService.saveClasse(inputDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chaque période de présence doit être de 1, 2 ou 3 semaines");
    }

    @Test
    void shouldThrowExceptionForEndDateBeforeStartDate() {
        // Given
        ClasseDTO inputDTO = new ClasseDTO(null, "Alternance A", null, null, Collections.emptyList());
        Classe entity = new Classe();
        
        ClassePresence p = new ClassePresence();
        p.setDateDebut(LocalDate.of(2026, 9, 7));
        p.setDateFin(LocalDate.of(2026, 9, 6)); // End before start -> invalid
        entity.setPresences(List.of(p));

        doAnswer(invocation -> {
            Classe c = invocation.getArgument(0);
            c.setPresences(entity.getPresences());
            return null;
        }).when(mapper).mergeWDTO(any(Classe.class), any(ClasseDTO.class));

        // When / Then
        assertThatThrownBy(() -> classeService.saveClasse(inputDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La date de fin ne peut pas être antérieure à la date de début");
    }

    @Test
    void shouldThrowExceptionForMissingDates() {
        // Given
        ClasseDTO inputDTO = new ClasseDTO(null, "Alternance A", null, null, Collections.emptyList());
        Classe entity = new Classe();
        
        ClassePresence p = new ClassePresence();
        p.setDateDebut(LocalDate.of(2026, 9, 7));
        p.setDateFin(null); // Missing end date -> invalid
        entity.setPresences(List.of(p));

        doAnswer(invocation -> {
            Classe c = invocation.getArgument(0);
            c.setPresences(entity.getPresences());
            return null;
        }).when(mapper).mergeWDTO(any(Classe.class), any(ClasseDTO.class));

        // When / Then
        assertThatThrownBy(() -> classeService.saveClasse(inputDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Les dates de début et de fin sont requises");
    }
}
