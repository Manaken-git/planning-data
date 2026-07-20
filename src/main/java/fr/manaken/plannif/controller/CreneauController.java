package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.CreneauDTO;
import fr.manaken.plannif.service.CreneauService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/creneaux")
public class CreneauController {

    private final CreneauService creneauService;

    public CreneauController(CreneauService creneauService) {
        this.creneauService = creneauService;
    }

    @GetMapping("/list")
    public List<CreneauDTO> getCreneaux() {
        return creneauService.getCreneaux();
    }

    @PostMapping("/create")
    public CreneauDTO createCreneau(@RequestBody CreneauDTO dto) {
        return creneauService.saveCreneau(dto);
    }

    @PutMapping("/update")
    public CreneauDTO updateCreneau(@RequestBody CreneauDTO dto) {
        return creneauService.saveCreneau(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCreneau(@PathVariable Long id) {
        creneauService.deleteCreneau(id);
    }
}
