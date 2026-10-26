package fr.manaken.plannif.service;

import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.repository.ProfesseurRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ProfesseurPersistenceIntegrationTest {

    @Autowired
    private ProfesseurRepository professeurRepository;

    @Autowired
    private PronoteImportService pronoteImportService;

    @Test
    void testSaveProfesseurWithoutIdAndWithoutPlageHoraire() {
        Professeur p = new Professeur();
        p.setNom("DUPONT");
        p.setPrenom("Jean");
        p.setNb_heures(BigDecimal.valueOf(18));
        p.setMaxHeuresParJour(BigDecimal.valueOf(6));
        p.setMaxHeuresParSemaine(BigDecimal.valueOf(18));
        p.setMaxHeuresParSeance(BigDecimal.valueOf(2));
        p.setPauseDejeunerDureeMin(45);

        Professeur saved = professeurRepository.save(p);
        assertNotNull(saved.getId(), "L'id généré ne devrait pas être null");

        professeurRepository.delete(saved);
    }

    @Test
    void testImportPronoteWithUserData() throws Exception {
        String tsv = "Civilité\tNom\tPrénom\tPrincipal de\tAdresse E-mail\tDiscipline\r\n" +
                "M.\tALET\tAdrien\t\t\t\r\n" +
                "M.\tAMADIO\tMarc\t\t\t\r\n" +
                "Mme\tANDRIEU\tCéline\tTerm STAV 1\t\t\r\n" +
                "Mme\tAURIOL\tLaurine\tCFA - BTSA TC 1\tlaurine.auriol@educagri.fr\t\r\n" +
                "M.\tBATHELOT\tNicolas\t\t\tMathématiques\r\n" +
                "\tCNAM\t\t\t\t\r\n" +
                "\tINTERVENANT EXTERIEUR\t2\t\t\t\r\n";

        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file", "professeurs.tsv", "text/tab-separated-values", tsv.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        // Nettoyage préalable pour tester la création
        professeurRepository.deleteAll();

        fr.manaken.plannif.dto.PronoteImportResultDTO result = pronoteImportService.importPronoteAuto(file);
        org.junit.jupiter.api.Assertions.assertTrue(result.succes(), "L'import devrait réussir: " + result.avertissements());
        org.junit.jupiter.api.Assertions.assertEquals(7, result.nbCrees());

        // Nettoyage après le test
        professeurRepository.deleteAll();
    }
}
