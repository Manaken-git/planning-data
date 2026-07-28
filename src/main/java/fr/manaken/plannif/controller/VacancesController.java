package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.VacancesDTO;
import fr.manaken.plannif.service.VacancesService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/vacances")
@Tag(name = "Vacances", description = "Gestion du calendrier des vacances scolaires")
public class VacancesController {

    private final VacancesService vacancesService;

    public VacancesController(VacancesService vacancesService) {
        this.vacancesService = vacancesService;
    }

    @GetMapping("/list")
    @Operation(summary = "Lister les vacances", description = "Récupère toutes les périodes de vacances.")
    public List<VacancesDTO> getVacances() {
        return vacancesService.getVacances();
    }

    @PostMapping("/create")
    @Operation(summary = "Créer une période de vacances", description = "Ajoute une nouvelle période de vacances.")
    public VacancesDTO createVacances(@RequestBody VacancesDTO dto) {
        return vacancesService.saveVacances(dto);
    }

    @PutMapping("/update")
    @Operation(summary = "Modifier une période de vacances", description = "Met à jour une période de vacances existante.")
    public VacancesDTO updateVacances(@RequestBody VacancesDTO dto) {
        return vacancesService.saveVacances(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une période de vacances", description = "Supprime une période de vacances par son ID.")
    public void deleteVacances(@PathVariable Long id) {
        vacancesService.deleteVacances(id);
    }
}
