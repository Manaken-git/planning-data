package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.MatiereClasseConfigDTO;
import fr.manaken.plannif.service.MatiereClasseConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/configs")
public class MatiereClasseConfigController {

    private final MatiereClasseConfigService configService;

    public MatiereClasseConfigController(MatiereClasseConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/list")
    public List<MatiereClasseConfigDTO> getMatiereClasseConfigs() {
        return configService.getMatiereClasseConfigs();
    }

    @PostMapping("/create")
    public MatiereClasseConfigDTO createMatiereClasseConfig(@RequestBody MatiereClasseConfigDTO dto) {
        return configService.saveMatiereClasseConfig(dto);
    }

    @PutMapping("/update")
    public MatiereClasseConfigDTO updateMatiereClasseConfig(@RequestBody MatiereClasseConfigDTO dto) {
        return configService.saveMatiereClasseConfig(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatiereClasseConfig(@PathVariable Long id) {
        configService.deleteMatiereClasseConfig(id);
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public org.springframework.http.ResponseEntity<String> importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            int count = configService.importCsv(file);
            return org.springframework.http.ResponseEntity.ok(count + " configurations importées avec succès.");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Erreur lors de l'import : " + e.getMessage());
        }
    }

    @GetMapping("/export")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv() {
        try {
            byte[] csvBytes = configService.exportCsv();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename("configs.csv").build());
            return new org.springframework.http.ResponseEntity<>(csvBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }
}

