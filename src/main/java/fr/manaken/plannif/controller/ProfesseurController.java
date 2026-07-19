package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.ProfesseurDTO;
import fr.manaken.plannif.service.ProfesseurService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/profs")
@Tag(name = "Professeurs", description = "Gestion des enseignants, leurs heures et disponibilités")
public class ProfesseurController {
    private final ProfesseurService professeurService;

    public ProfesseurController(ProfesseurService professeurService) {
        this.professeurService = professeurService;
    }

    @GetMapping("/list")
    @Operation(summary = "Lister les professeurs", description = "Récupère la liste de tous les enseignants.")
    public List<ProfesseurDTO> profs() {
        return professeurService.getProfesseurs();
    }

    @PostMapping("/create")
    @Operation(summary = "Créer un professeur", description = "Ajoute un nouvel enseignant dans le système.")
    public ProfesseurDTO createProf(@RequestBody ProfesseurDTO p) {
        return professeurService.saveProfesseur(p);
    }

    @PutMapping("/update")
    @Operation(summary = "Mettre à jour un professeur", description = "Met à jour les informations d'un enseignant existant.")
    public ProfesseurDTO updateProf(@RequestBody ProfesseurDTO p) {
        return professeurService.saveProfesseur(p);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un professeur", description = "Supprime un enseignant selon son ID.")
    public void deleteProf(@PathVariable Long id) {
        professeurService.deleteProfesseur(id);
    }
}
