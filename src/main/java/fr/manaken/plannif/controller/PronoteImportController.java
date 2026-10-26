package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.PronoteImportResultDTO;
import fr.manaken.plannif.service.PronoteImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pronote")
public class PronoteImportController {

    private final PronoteImportService pronoteImportService;

    public PronoteImportController(PronoteImportService pronoteImportService) {
        this.pronoteImportService = pronoteImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<PronoteImportResultDTO> importAuto(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importPronoteAuto(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/profs")
    public ResponseEntity<PronoteImportResultDTO> importProfs(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importProfesseurs(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/classes")
    public ResponseEntity<PronoteImportResultDTO> importClasses(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importClasses(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/salles")
    public ResponseEntity<PronoteImportResultDTO> importSalles(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importSalles(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/matieres")
    public ResponseEntity<PronoteImportResultDTO> importMatieres(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importMatieres(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/presences")
    public ResponseEntity<PronoteImportResultDTO> importPresences(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importPresences(file);
        return ResponseEntity.ok(result);
    }
}
