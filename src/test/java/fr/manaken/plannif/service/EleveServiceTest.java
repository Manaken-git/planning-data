package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.EleveMapper;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Eleve;
import fr.manaken.plannif.pusher.DataPusher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EleveServiceTest {

    @Mock
    private DataFetcher dataFetcher;

    @Mock
    private DataPusher dataPusher;

    @Mock
    private EleveMapper mapper;

    @InjectMocks
    private EleveService eleveService;

    @Test
    void shouldGetAllEleves() {
        Eleve eleve = new Eleve();
        eleve.setId(1L);
        eleve.setNom("Dupont");
        eleve.setPrenom("Jean");

        Classe classe = new Classe();
        classe.setId(10L);
        eleve.setClasse(classe);

        EleveDTO dto = new EleveDTO(1L, "Dupont", "Jean", 10L);

        when(dataFetcher.getAllEleves()).thenReturn(List.of(eleve));
        when(mapper.toDto(eleve)).thenReturn(dto);

        List<EleveDTO> result = eleveService.getAllEleves();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).classeId()).isEqualTo(10L);
    }

    @Test
    void shouldSaveEleveWithClasseIdInDto() {
        EleveDTO inputDto = new EleveDTO(null, "Dupont", "Jean", 5L);
        Classe classe = new Classe();
        classe.setId(5L);

        when(dataFetcher.getClasse(5)).thenReturn(classe);
        when(dataPusher.saveEleve(any(Eleve.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toDto(any(Eleve.class))).thenReturn(new EleveDTO(1L, "Dupont", "Jean", 5L));

        EleveDTO result = eleveService.saveEleve(inputDto, null);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.classeId()).isEqualTo(5L);
        verify(dataFetcher).getClasse(5);
        verify(dataPusher).saveEleve(any(Eleve.class));
    }
}
