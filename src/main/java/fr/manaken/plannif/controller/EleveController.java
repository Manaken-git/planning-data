package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.service.EleveService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eleves")
public class EleveController {

    private final EleveService eleveService;

    public EleveController(EleveService eleveService) {
        this.eleveService = eleveService;
    }

    @GetMapping("/list")
    public List<EleveDTO> getAllEleves() {
        return eleveService.getAllEleves();
    }

    @GetMapping("/list/{idClasse}")
    public List<EleveDTO> getElevesByClasse(@PathVariable Long idClasse) {
        return eleveService.getEleves(idClasse);
    }

    /**
     * Crée un élève. Le paramètre optionnel {@code classeId} permet d'associer
     * directement l'élève à une classe (le DTO Eleve n'expose pas d'id de classe).
     */
    @PostMapping("/create")
    public EleveDTO createEleve(@RequestBody EleveDTO dto,
                                @RequestParam(required = false) Long classeId) {
        return eleveService.saveEleve(dto, classeId);
    }

    @PutMapping("/update")
    public EleveDTO updateEleve(@RequestBody EleveDTO dto,
                                @RequestParam(required = false) Long classeId) {
        return eleveService.saveEleve(dto, classeId);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEleve(@PathVariable Long id) {
        eleveService.deleteEleve(id);
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public org.springframework.http.ResponseEntity<String> importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            int count = eleveService.importCsv(file);
            return org.springframework.http.ResponseEntity.ok(count + " élèves importés avec succès.");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Erreur lors de l'import : " + e.getMessage());
        }
    }
}
