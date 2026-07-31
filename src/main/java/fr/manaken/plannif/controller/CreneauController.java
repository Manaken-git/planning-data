package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.CreneauDTO;
import fr.manaken.plannif.service.CreneauService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/creneaux")
public class CreneauController {

    private final CreneauService creneauService;

    public CreneauController(CreneauService creneauService) {
        this.creneauService = creneauService;
    }

    @GetMapping("/list")
    public List<CreneauDTO> getCreneaux() {
        return creneauService.getCreneaux();
    }

    @PostMapping("/create")
    public CreneauDTO createCreneau(@RequestBody CreneauDTO dto) {
        return creneauService.saveCreneau(dto);
    }

    @PutMapping("/update")
    public CreneauDTO updateCreneau(@RequestBody CreneauDTO dto) {
        return creneauService.saveCreneau(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCreneau(@PathVariable Long id) {
        creneauService.deleteCreneau(id);
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public org.springframework.http.ResponseEntity<String> importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            int count = creneauService.importCsv(file);
            return org.springframework.http.ResponseEntity.ok(count + " créneaux importés avec succès.");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Erreur lors de l'import : " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv() {
        try {
            byte[] csvBytes = creneauService.exportCsv();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename("creneaux.csv").build());
            return new org.springframework.http.ResponseEntity<>(csvBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }
}

