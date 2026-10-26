package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.PronoteClassePreviewResponseDTO;
import fr.manaken.plannif.dto.PronoteImportResultDTO;
import fr.manaken.plannif.dto.PronoteProfPreviewResponseDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.RoleReferent;
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

        when(dataPusher.saveProfesseur(any())).thenAnswer(i -> {
            Professeur p = i.getArgument(0);
            if (p.getId() == null) p.setId(10L);
            return p;
        });
        when(dataPusher.saveClasse(any())).thenAnswer(i -> i.getArgument(0));
        when(dataPusher.saveSalle(any())).thenAnswer(i -> i.getArgument(0));
        when(dataPusher.saveMatiere(any())).thenAnswer(i -> {
            Matiere m = i.getArgument(0);
            if (m.getId() == null) m.setId(5L);
            return m;
        });
    }

    @Test
    void testPreviewAndImportProfesseursWithPronoteExactColumns() throws Exception {
        Classe c = new Classe();
        c.setId(1L);
        c.setNom("Term STAV 2");
        c.setReferents(new ArrayList<>());
        when(dataFetcher.getClasses()).thenReturn(List.of(c));

        String csv = "Civilité;Nom;Prénom;Principal de;Adresse E-mail;Discipline\n" +
                "Mme;MARTY;Béatrice;Term STAV 2;beatrice.marty@lycee.fr;Biologie\n" +
                "M.;LUMINET;Stéphane;;stephane.luminet@lycee.fr;Physique-Chimie\n";

        MockMultipartFile file = new MockMultipartFile("file", "export_profs.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        // 1. Test Preview
        PronoteProfPreviewResponseDTO preview = service.previewProfesseurs(file);
        assertTrue(preview.succes());
        assertEquals(2, preview.totalLignes());
        assertEquals(2, preview.nbNouveaux());
        assertEquals(0, preview.nbExistants());
        assertEquals("MARTY", preview.items().get(0).nom());
        assertEquals("Béatrice", preview.items().get(0).prenom());
        assertEquals("Biologie", preview.items().get(0).discipline());
        assertEquals(List.of("Term STAV 2"), preview.items().get(0).classesConnues());

        // 2. Test Import
        PronoteImportResultDTO result = service.importProfesseurs(file, 18.0, true, true, true);
        assertTrue(result.succes());
        assertEquals(2, result.nbCrees());
        assertEquals(1, c.getReferents().size());
        assertEquals(RoleReferent.PRINCIPAL, c.getReferents().get(0).getRole());
        assertEquals("MARTY", c.getReferents().get(0).getProfesseur().getNom());
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
    void testPreviewAndImportClassesPronoteHeaders() throws Exception {
        Professeur p1 = new Professeur();
        p1.setId(101L);
        p1.setNom("MARTY");
        p1.setPrenom("Béatrice");

        Professeur p2 = new Professeur();
        p2.setId(102L);
        p2.setNom("LUMINET");
        p2.setPrenom("Stéphane");

        when(dataFetcher.getProfesseurs()).thenReturn(List.of(p1, p2));

        // Format avec 2 lignes d'en-tête comme dans l'export Pronote (Images de l'utilisateur)
        String csv = "Nom;Effectif;Niveau;Filière;Notation;Positionnement dans LSU;Prof. principal;Prof. principal;Délégués;CPE;Couleur;Nombre de filles;Nombre de garçons;MEF\n" +
                ";;;;;;Titulaire;Suppléant;;;;;;\n" +
                "Term STAV 2;28;Terminale;STAV;Notes;LSU;Mme MARTY Béatrice;M. LUMINET Stéphane;Dupont;Durand;#FF0000;14;14;MEF123\n" +
                "BTS ME1-A;30;BTS 1;ME;Notes;Non;MARTY Béatrice;;;;#00FF00;10;20;MEF456\n";

        MockMultipartFile file = new MockMultipartFile("file", "classes_pronote.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        // 1. Preview
        PronoteClassePreviewResponseDTO preview = service.previewClasses(file);
        assertTrue(preview.succes());
        assertEquals(2, preview.totalLignes());
        assertEquals(2, preview.nbNouveaux());
        assertEquals(0, preview.nbExistants());

        assertEquals("Term STAV 2", preview.items().get(0).nom());
        assertEquals(28, preview.items().get(0).effectif());
        assertEquals("Terminale", preview.items().get(0).niveau());
        assertEquals("STAV", preview.items().get(0).filiere());
        assertTrue(preview.items().get(0).profPrincipalTitulaireExistant());
        assertEquals(101L, preview.items().get(0).profPrincipalTitulaireId());
        assertTrue(preview.items().get(0).profPrincipalSuppleantExistant());
        assertEquals(102L, preview.items().get(0).profPrincipalSuppleantId());

        assertEquals("BTS ME1-A", preview.items().get(1).nom());
        assertEquals(30, preview.items().get(1).effectif());
        assertTrue(preview.items().get(1).profPrincipalTitulaireExistant());
        assertFalse(preview.items().get(1).profPrincipalSuppleantExistant());

        // 2. Import
        PronoteImportResultDTO result = service.importClasses(file, true, true);
        assertTrue(result.succes());
        assertEquals(2, result.nbCrees());
        assertEquals(0, result.nbMisAJour());
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
