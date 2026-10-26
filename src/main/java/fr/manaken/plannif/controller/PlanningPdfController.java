package fr.manaken.plannif.controller;

import fr.manaken.plannif.service.PdfExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plannings")
public class PlanningPdfController {

    private final PdfExportService pdfExportService;

    public PlanningPdfController(PdfExportService pdfExportService) {
        this.pdfExportService = pdfExportService;
    }

    @GetMapping("/{id}/pdf/classe")
    public ResponseEntity<byte[]> getPdfClasse(
            @PathVariable Long id,
            @RequestParam String nom) throws Exception {

        byte[] pdfBytes = pdfExportService.generatePdfForClasse(id, nom);
        String safeName = nom.replaceAll("[^a-zA-Z0-9-_]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"planning_classe_" + safeName + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/pdf/classes")
    public ResponseEntity<byte[]> getLivretClasses(@PathVariable Long id) throws Exception {
        byte[] pdfBytes = pdfExportService.generateLivretClasses(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"livret_classes_complet.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/pdf/prof")
    public ResponseEntity<byte[]> getPdfProfesseur(
            @PathVariable Long id,
            @RequestParam String nom) throws Exception {

        byte[] pdfBytes = pdfExportService.generatePdfForProfesseur(id, nom);
        String safeName = nom.replaceAll("[^a-zA-Z0-9-_]", "_");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"planning_prof_" + safeName + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/pdf/profs")
    public ResponseEntity<byte[]> getLivretProfesseurs(@PathVariable Long id) throws Exception {
        byte[] pdfBytes = pdfExportService.generateLivretProfesseurs(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"livret_professeurs_complet.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/pdf/zip")
    public ResponseEntity<byte[]> getZipArchive(@PathVariable Long id) throws Exception {
        byte[] zipBytes = pdfExportService.generateZipArchive(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"planning_" + id + "_exports_pdf.zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zipBytes);
    }
}
