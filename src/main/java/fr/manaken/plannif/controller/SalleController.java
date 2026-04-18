package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.SalleDTO;
import fr.manaken.plannif.service.SalleService;
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
}
