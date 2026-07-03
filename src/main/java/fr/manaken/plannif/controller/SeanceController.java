package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.service.SeanceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seances")
public class SeanceController {

    private final SeanceService seanceService;

    public SeanceController(SeanceService seanceService) {
        this.seanceService = seanceService;
    }

    @GetMapping("/list")
    public List<SeanceDTO> getSeances() {
        return seanceService.getSeances();
    }

    /**
     * Crée une séance. Les IDs des entités liées sont des query params optionnels
     * car le SeanceDTO est dénormalisé (libellés uniquement, pas d'IDs).
     */
    @PostMapping("/create")
    public SeanceDTO createSeance(@RequestBody SeanceDTO dto,
                                  @RequestParam(required = false) Long professeurId,
                                  @RequestParam(required = false) Long classeId,
                                  @RequestParam(required = false) Long matiereId,
                                  @RequestParam(required = false) Long salleId) {
        return seanceService.saveSeance(dto, professeurId, classeId, matiereId, salleId);
    }

    @PutMapping("/update")
    public SeanceDTO updateSeance(@RequestBody SeanceDTO dto,
                                  @RequestParam(required = false) Long professeurId,
                                  @RequestParam(required = false) Long classeId,
                                  @RequestParam(required = false) Long matiereId,
                                  @RequestParam(required = false) Long salleId) {
        return seanceService.saveSeance(dto, professeurId, classeId, matiereId, salleId);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeance(@PathVariable Long id) {
        seanceService.deleteSeance(id);
    }
}
