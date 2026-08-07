package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.PlanningDTO;
import fr.manaken.plannif.dto.PlanningSaveDTO;
import fr.manaken.plannif.service.PlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plannings")
public class PlanningController {

    private final PlanningService planningService;

    public PlanningController(PlanningService planningService) {
        this.planningService = planningService;
    }

    @GetMapping("/list")
    public List<PlanningDTO> getPlannings() {
        return planningService.getPlannings();
    }

    @GetMapping("/{id}")
    public PlanningDTO getPlanning(@PathVariable Long id) {
        return planningService.getPlanning(id);
    }

    @PostMapping("/save")
    public PlanningDTO savePlanning(@RequestBody PlanningSaveDTO dto) {
        return planningService.savePlanning(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlanning(@PathVariable Long id) {
        planningService.deletePlanning(id);
    }
}

