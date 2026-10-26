package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.PronoteImportResultDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.pusher.DataPusher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PronoteImportServiceTest {

    private DataFetcher dataFetcher;
    private DataPusher dataPusher;
    private PronoteImportService service;

    @BeforeEach
    void setUp() {
        dataFetcher = Mockito.mock(DataFetcher.class);
        dataPusher = Mockito.mock(DataPusher.class);
        service = new PronoteImportService(dataFetcher, dataPusher);

        when(dataFetcher.getProfesseurs()).thenReturn(new ArrayList<>());
        when(dataFetcher.getClasses()).thenReturn(new ArrayList<>());
        when(dataFetcher.getSalles()).thenReturn(new ArrayList<>());
        when(dataFetcher.getMatieres()).thenReturn(new ArrayList<>());

        when(dataPusher.saveProfesseur(any())).thenAnswer(i -> i.getArgument(0));
        when(dataPusher.saveClasse(any())).thenAnswer(i -> i.getArgument(0));
        when(dataPusher.saveSalle(any())).thenAnswer(i -> i.getArgument(0));
        when(dataPusher.saveMatiere(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void testImportProfesseursAutoDetect() throws Exception {
        String csv = "Civilite;Nom;Prenom;Discipline;Courriel\n" +
                "M.;DUPONT;Jean;Mathematiques;jean.dupont@lycee.fr\n" +
                "Mme;MARTIN;Claire;Anglais;claire.martin@lycee.fr\n";

        MockMultipartFile file = new MockMultipartFile("file", "profs_pronote.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        PronoteImportResultDTO result = service.importPronoteAuto(file);

        assertTrue(result.succes());
        assertEquals("PROFESSEURS", result.typeEntite());
        assertEquals(2, result.totalLignes());
        assertEquals(2, result.nbCrees());
    }

    @Test
    void testImportClasses() throws Exception {
        String csv = "Classe;Effectif;Niveau\n" +
                "BTS SIO 1;32;Superieur\n" +
                "Terminale G1;35;Terminale\n";

        MockMultipartFile file = new MockMultipartFile("file", "classes.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        PronoteImportResultDTO result = service.importPronoteAuto(file);

        assertTrue(result.succes());
        assertEquals("CLASSES", result.typeEntite());
        assertEquals(2, result.nbCrees());
    }

    @Test
    void testImportPresencesAlternance() throws Exception {
        Classe c = new Classe();
        c.setId(1L);
        c.setNom("BTS SIO 1");
        c.setPresences(new ArrayList<>());
        when(dataFetcher.getClasses()).thenReturn(List.of(c));

        String csv = "Classe;Du;Au;Statut\n" +
                "BTS SIO 1;01/09/2026;25/10/2026;En cours\n" +
                "BTS SIO 1;26/10/2026;08/11/2026;Entreprise\n" + // Doit être filtré (période en entreprise)
                "BTS SIO 1;09/11/2026;18/12/2026;En cours\n";

        MockMultipartFile file = new MockMultipartFile("file", "alternance.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));
        PronoteImportResultDTO result = service.importPresences(file);

        assertTrue(result.succes());
        assertEquals(2, result.nbCrees()); // 2 périodes de présence en cours
        assertEquals(2, c.getPresences().size());
    }
}
