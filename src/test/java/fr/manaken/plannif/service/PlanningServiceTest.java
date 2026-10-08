package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.PlanningDTO;
import fr.manaken.plannif.dto.PlanningSaveDTO;
import fr.manaken.plannif.dto.SeanceSaveDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.PlanningMapper;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.model.Planning;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.pusher.DataPusher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlanningServiceTest {

    @Mock
    private DataFetcher dataFetcher;

    @Mock
    private DataPusher dataPusher;

    @Mock
    private PlanningMapper mapper;

    @InjectMocks
    private PlanningService planningService;

    @Test
    void shouldSavePlanningWithSeancesWithoutCreneau() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime debut = now.plusDays(1).withHour(8).withMinute(0);
        LocalDateTime fin = now.plusDays(1).withHour(10).withMinute(0);

        SeanceSaveDTO seanceSaveDTO = new SeanceSaveDTO(
                null, 1L, 2L, 3L, 4L, "COURS", debut, fin
        );

        PlanningSaveDTO planningSaveDTO = new PlanningSaveDTO(
                null, "Planning Test", now, List.of(seanceSaveDTO)
        );

        Planning savedEntity = new Planning();
        savedEntity.setId(10L);
        savedEntity.setNom("Planning Test");

        when(dataPusher.savePlanning(any(Planning.class))).thenReturn(savedEntity);

        Professeur prof = new Professeur();
        prof.setId(1L);
        Classe classe = new Classe();
        classe.setId(2L);
        Matiere matiere = new Matiere();
        matiere.setId(3L);
        Salle salle = new Salle();
        salle.setId(4L);

        when(dataFetcher.getProfesseur(1)).thenReturn(prof);
        when(dataFetcher.getClasse(2)).thenReturn(classe);
        when(dataFetcher.getMatiere(3)).thenReturn(matiere);
        when(dataFetcher.getSalle(4)).thenReturn(salle);

        Seance savedSeance = new Seance();
        savedSeance.setId(100L);
        savedSeance.setDebut(debut);
        savedSeance.setFin(fin);
        savedSeance.setProfesseur(prof);
        savedSeance.setClasse(classe);
        savedSeance.setMatiere(matiere);
        savedSeance.setSalle(salle);
        savedSeance.setType(Seance.TypeSeance.COURS);

        when(dataPusher.saveSeance(any(Seance.class))).thenReturn(savedSeance);

        PlanningDTO expectedDTO = new PlanningDTO(10L, "Planning Test", now, List.of());
        when(mapper.toDto(any(Planning.class))).thenReturn(expectedDTO);

        PlanningDTO result = planningService.savePlanning(planningSaveDTO);

        assertThat(result).isNotNull();
        verify(dataPusher, atLeastOnce()).savePlanning(any(Planning.class));
        verify(dataPusher).saveSeance(argThat(s ->
                s.getDebut().equals(debut) &&
                s.getFin().equals(fin) &&
                s.getType() == Seance.TypeSeance.COURS
        ));
    }

    @Test
    void shouldCalculateFinFromMatiereClasseConfigWhenFinIsNull() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime debut = now.plusDays(1).withHour(8).withMinute(0);

        SeanceSaveDTO seanceSaveDTO = new SeanceSaveDTO(
                null, 1L, 2L, 3L, 4L, "TP", debut, null
        );

        PlanningSaveDTO planningSaveDTO = new PlanningSaveDTO(
                null, "Planning TP", now, List.of(seanceSaveDTO)
        );

        Planning savedEntity = new Planning();
        savedEntity.setId(11L);
        savedEntity.setNom("Planning TP");

        when(dataPusher.savePlanning(any(Planning.class))).thenReturn(savedEntity);

        fr.manaken.plannif.model.MatiereClasseConfig config = new fr.manaken.plannif.model.MatiereClasseConfig();
        config.setDureeTpMinutes(110);
        when(dataFetcher.getMatiereClasseConfigsByClasseAndMatiere(2L, 3L)).thenReturn(List.of(config));

        Seance savedSeance = new Seance();
        savedSeance.setId(101L);
        savedSeance.setDebut(debut);
        savedSeance.setFin(debut.plusMinutes(110));
        when(dataPusher.saveSeance(any(Seance.class))).thenReturn(savedSeance);

        PlanningDTO expectedDTO = new PlanningDTO(11L, "Planning TP", now, List.of());
        when(mapper.toDto(any(Planning.class))).thenReturn(expectedDTO);

        PlanningDTO result = planningService.savePlanning(planningSaveDTO);

        assertThat(result).isNotNull();
        verify(dataPusher).saveSeance(argThat(s ->
                s.getDebut().equals(debut) &&
                s.getFin().equals(debut.plusMinutes(110)) &&
                s.getType() == Seance.TypeSeance.TP
        ));
    }
}
