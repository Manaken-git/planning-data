package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.MatiereDTO;
import fr.manaken.plannif.service.MatiereService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matieres")
public class MatiereController {

    private final MatiereService matiereService;

    public MatiereController(MatiereService matiereService) {
        this.matiereService = matiereService;
    }

    @GetMapping("/list")
    public List<MatiereDTO> getMatieres() {
        return matiereService.getMatieres();
    }

    @PostMapping("/create")
    public MatiereDTO createMatiere(@RequestBody MatiereDTO dto) {
        return matiereService.saveMatiere(dto);
    }

    @PutMapping("/update")
    public MatiereDTO updateMatiere(@RequestBody MatiereDTO dto) {
        return matiereService.saveMatiere(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatiere(@PathVariable Long id) {
        matiereService.deleteMatiere(id);
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public org.springframework.http.ResponseEntity<String> importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            int count = matiereService.importCsv(file);
            return org.springframework.http.ResponseEntity.ok(count + " matières importées avec succès.");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Erreur lors de l'import : " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv() {
        try {
            byte[] csvBytes = matiereService.exportCsv();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename("matieres.csv").build());
            return new org.springframework.http.ResponseEntity<>(csvBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }
}

