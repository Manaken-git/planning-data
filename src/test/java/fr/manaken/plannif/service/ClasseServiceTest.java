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

    @Test
    void shouldExportCsvWithPresences() throws Exception {
        // Given
        Classe c1 = new Classe();
        c1.setId(1L);
        c1.setNom("Classe A");
        
        ClassePresence p1 = new ClassePresence();
        p1.setDateDebut(LocalDate.of(2026, 9, 7));
        p1.setDateFin(LocalDate.of(2026, 9, 11));
        p1.setClasse(c1);
        
        ClassePresence p2 = new ClassePresence();
        p2.setDateDebut(LocalDate.of(2026, 9, 14));
        p2.setDateFin(LocalDate.of(2026, 9, 18));
        p2.setClasse(c1);
        
        c1.setPresences(List.of(p1, p2));
        
        when(dataFetcher.getClasses()).thenReturn(List.of(c1));

        // When
        byte[] csvBytes = classeService.exportCsv();

        // Then
        String csvContent = new String(csvBytes, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csvContent).contains("id", "nom", "presences");
        assertThat(csvContent).contains("1", "Classe A", "2026-09-07:2026-09-11;2026-09-14:2026-09-18");
    }

    @Test
    void shouldImportCsvWithPresences() throws Exception {
        // Given
        String csvData = "id,nom,presences\n" +
                "1,Classe A,2026-09-07:2026-09-11;2026-09-14:2026-09-18\n";
        
        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        when(dataFetcher.existsClasse(1)).thenReturn(false);

        // When
        int count = classeService.importCsv(mockFile);

        // Then
        assertThat(count).isEqualTo(1);
        verify(dataPusher).saveClasse(argThat(c -> {
            assertThat(c.getId()).isEqualTo(1L);
            assertThat(c.getNom()).isEqualTo("Classe A");
            assertThat(c.getPresences()).hasSize(2);
            assertThat(c.getPresences().get(0).getDateDebut()).isEqualTo(LocalDate.of(2026, 9, 7));
            assertThat(c.getPresences().get(0).getDateFin()).isEqualTo(LocalDate.of(2026, 9, 11));
            assertThat(c.getPresences().get(1).getDateDebut()).isEqualTo(LocalDate.of(2026, 9, 14));
            assertThat(c.getPresences().get(1).getDateFin()).isEqualTo(LocalDate.of(2026, 9, 18));
            return true;
        }));
    }

    @Test
    void shouldUpdateCsvWithPresences() throws Exception {
        // Given
        String csvData = "id,nom,presences\n" +
                "1,Classe A Modifiee,2026-09-07:2026-09-11\n";
        
        Classe existingClasse = new Classe();
        existingClasse.setId(1L);
        existingClasse.setNom("Classe A Originale");
        
        ClassePresence existingPresence = new ClassePresence();
        existingPresence.setId(99L);
        existingPresence.setDateDebut(LocalDate.of(2026, 9, 14));
        existingPresence.setDateFin(LocalDate.of(2026, 9, 18));
        existingPresence.setClasse(existingClasse);
        existingClasse.setPresences(new java.util.ArrayList<>(List.of(existingPresence)));

        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        when(dataFetcher.existsClasse(1)).thenReturn(true);
        when(dataFetcher.getClasse(1)).thenReturn(existingClasse);

        // When
        int count = classeService.importCsv(mockFile);

        // Then
        assertThat(count).isEqualTo(1);
        verify(dataPusher).saveClasse(argThat(c -> {
            assertThat(c.getId()).isEqualTo(1L);
            assertThat(c.getNom()).isEqualTo("Classe A Modifiee");
            assertThat(c.getPresences()).hasSize(1);
            assertThat(c.getPresences().get(0).getDateDebut()).isEqualTo(LocalDate.of(2026, 9, 7));
            assertThat(c.getPresences().get(0).getDateFin()).isEqualTo(LocalDate.of(2026, 9, 11));
            return true;
        }));
    }
}

