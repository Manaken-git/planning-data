package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.ScoreConstraintConfigDTO;
import fr.manaken.plannif.service.ScoreConstraintConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/constraint-configs")
@Tag(name = "Configuration des scores", description = "Gestion des pondérations des contraintes Timefold")
public class ScoreConstraintConfigController {

    private final ScoreConstraintConfigService service;

    public ScoreConstraintConfigController(ScoreConstraintConfigService service) {
        this.service = service;
    }

    @GetMapping("/list")
    @Operation(summary = "Lister les configurations de score", description = "Retourne l'ensemble des configurations de pénalités.")
    public List<ScoreConstraintConfigDTO> getAllConfigs() {
        return service.getAllConfigs();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une configuration", description = "Retourne la configuration de score par son ID.")
    public ScoreConstraintConfigDTO getConfig(@PathVariable Long id) {
        return service.getConfig(id);
    }

    @GetMapping("/default")
    @Operation(summary = "Configuration par défaut", description = "Retourne la configuration de score active/par défaut.")
    public ScoreConstraintConfigDTO getDefaultConfig() {
        return service.getDefaultConfig();
    }

    @PostMapping("/save")
    @Operation(summary = "Créer ou mettre à jour une configuration de score", description = "Enregistre une configuration de score.")
    public ScoreConstraintConfigDTO saveConfig(@RequestBody ScoreConstraintConfigDTO dto) {
        return service.saveConfig(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une configuration", description = "Supprime la configuration de score par son ID.")
    public void deleteConfig(@PathVariable Long id) {
        service.deleteConfig(id);
    }
}
