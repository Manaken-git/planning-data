package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.PlanningDTO;
import fr.manaken.plannif.dto.SeanceDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PdfExportServiceTest {

    private PlanningService planningService;
    private PdfExportService pdfExportService;

    @BeforeEach
    void setUp() {
        planningService = Mockito.mock(PlanningService.class);
        pdfExportService = new PdfExportService(planningService);
    }

    @Test
    void testGeneratePdfClasseAndProf() throws Exception {
        LocalDateTime mondayMorning = LocalDateTime.of(2026, 9, 7, 8, 0);
        LocalDateTime mondayEnd = LocalDateTime.of(2026, 9, 7, 10, 0);

        SeanceDTO s1 = new SeanceDTO(1L, mondayMorning, mondayEnd, "M. DUPONT", "BTS SIO 1", "Maths", "B102", "COURS", "G1", null);
        SeanceDTO s2 = new SeanceDTO(2L, mondayMorning.plusHours(2), mondayMorning.plusHours(4), "MME. MARTIN", "BTS SIO 1", "Informatique", "Labo 1", "TP", null, null);

        PlanningDTO mockPlanning = new PlanningDTO(1L, "Planning Rentrée 2026", LocalDateTime.now(), List.of(s1, s2));
        when(planningService.getPlanning(1L)).thenReturn(mockPlanning);

        // Test export classe
        byte[] pdfClasse = pdfExportService.generatePdfForClasse(1L, "BTS SIO 1");
        assertNotNull(pdfClasse);
        assertTrue(pdfClasse.length > 500, "Le PDF de classe doit contenir des octets");
        assertTrue(new String(pdfClasse, 0, 5).startsWith("%PDF-"), "Le fichier doit avoir la signature PDF");

        // Test export professeur
        byte[] pdfProf = pdfExportService.generatePdfForProfesseur(1L, "M. DUPONT");
        assertNotNull(pdfProf);
        assertTrue(pdfProf.length > 500);
        assertTrue(new String(pdfProf, 0, 5).startsWith("%PDF-"));

        // Test livret complet classes
        byte[] livretClasses = pdfExportService.generateLivretClasses(1L);
        assertNotNull(livretClasses);
        assertTrue(livretClasses.length > 500);

        // Test livret complet professeurs
        byte[] livretProfs = pdfExportService.generateLivretProfesseurs(1L);
        assertNotNull(livretProfs);
        assertTrue(livretProfs.length > 500);

        // Test archive ZIP
        byte[] zipArchive = pdfExportService.generateZipArchive(1L);
        assertNotNull(zipArchive);
        assertTrue(zipArchive.length > 1000, "L'archive ZIP doit contenir tous les livrets et exports individuels");
        // Signature ZIP : PK\x03\x04
        assertEquals(0x50, zipArchive[0]); // 'P'
        assertEquals(0x4B, zipArchive[1]); // 'K'
    }
}
