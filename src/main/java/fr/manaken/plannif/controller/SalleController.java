package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.SalleDTO;
import fr.manaken.plannif.service.SalleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/salles")
public class SalleController {
    private final SalleService salleService;

    public SalleController(SalleService salleService) {
        this.salleService = salleService;
    }

    @GetMapping("/list")
    public List<SalleDTO> salles() {
        return salleService.getSalles();
    }

    @PostMapping("/create")
    public SalleDTO createSalle(@RequestBody SalleDTO s) {
        return salleService.saveSalle(s);
    }

    @PutMapping("/update")
    public SalleDTO updateSalle(@RequestBody SalleDTO s) {
        return salleService.saveSalle(s);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSalle(@PathVariable Long id) {
        salleService.deleteSalle(id);
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public org.springframework.http.ResponseEntity<String> importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            int count = salleService.importCsv(file);
            return org.springframework.http.ResponseEntity.ok(count + " salles importées avec succès.");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Erreur lors de l'import : " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv() {
        try {
            byte[] csvBytes = salleService.exportCsv();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename("salles.csv").build());
            return new org.springframework.http.ResponseEntity<>(csvBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }
}

