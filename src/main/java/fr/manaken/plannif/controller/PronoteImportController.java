package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.PronoteClassePreviewResponseDTO;
import fr.manaken.plannif.dto.PronoteImportResultDTO;
import fr.manaken.plannif.dto.PronoteProfPreviewResponseDTO;
import fr.manaken.plannif.service.PronoteImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/pronote", "/profs/pronote", "/classes/pronote"})
public class PronoteImportController {

    private final PronoteImportService pronoteImportService;

    public PronoteImportController(PronoteImportService pronoteImportService) {
        this.pronoteImportService = pronoteImportService;
    }

    @PostMapping("/preview/profs")
    public ResponseEntity<PronoteProfPreviewResponseDTO> previewProfs(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteProfPreviewResponseDTO result = pronoteImportService.previewProfesseurs(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/preview/classes")
    public ResponseEntity<PronoteClassePreviewResponseDTO> previewClasses(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteClassePreviewResponseDTO result = pronoteImportService.previewClasses(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import")
    public ResponseEntity<PronoteImportResultDTO> importAuto(@RequestParam("file") MultipartFile file) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importPronoteAuto(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/profs")
    public ResponseEntity<PronoteImportResultDTO> importProfs(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "defaultHeures", defaultValue = "18.0") Double defaultHeures,
            @RequestParam(value = "createMissingMatieres", defaultValue = "true") boolean createMissingMatieres,
            @RequestParam(value = "assignReferents", defaultValue = "true") boolean assignReferents,
            @RequestParam(value = "updateExisting", defaultValue = "true") boolean updateExisting
    ) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importProfesseurs(
                file, defaultHeures, createMissingMatieres, assignReferents, updateExisting);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/classes")
    public ResponseEntity<PronoteImportResultDTO> importClasses(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "assignReferents", defaultValue = "true") boolean assignReferents,
            @RequestParam(value = "updateExisting", defaultValue = "true") boolean updateExisting
    ) throws Exception {
        PronoteImportResultDTO result = pronoteImportService.importClasses(file, assignReferents, updateExisting);
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
