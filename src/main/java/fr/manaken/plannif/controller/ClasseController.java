package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.ClasseDTO;
import fr.manaken.plannif.service.ClasseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/classes")
@Tag(name = "Classes", description = "Gestion des classes et groupes d'élèves")
public class ClasseController {

    private final ClasseService classeService;

    public ClasseController(ClasseService classeService) {
        this.classeService = classeService;
    }

    @GetMapping("/list")
    @Operation(summary = "Lister les classes", description = "Récupère la liste de toutes les classes.")
    public List<ClasseDTO> getClasses() {
        return classeService.getClasses();
    }

    @PostMapping("/create")
    @Operation(summary = "Créer une classe", description = "Ajoute une nouvelle classe.")
    public ClasseDTO createClasse(@RequestBody ClasseDTO dto) {
        return classeService.saveClasse(dto);
    }

    @PutMapping("/update")
    @Operation(summary = "Mettre à jour une classe", description = "Met à jour une classe existante.")
    public ClasseDTO updateClasse(@RequestBody ClasseDTO dto) {
        return classeService.saveClasse(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une classe", description = "Supprime une classe selon son ID.")
    public void deleteClasse(@PathVariable Long id) {
        classeService.deleteClasse(id);
    }

    @PostMapping(value = "/import", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer des classes via CSV", description = "Importe des classes en masse à partir d'un fichier CSV.")
    public org.springframework.http.ResponseEntity<String> importCsv(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            int count = classeService.importCsv(file);
            return org.springframework.http.ResponseEntity.ok(count + " classes importées avec succès.");
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body("Erreur lors de l'import : " + e.getMessage());
        }
    }
}
