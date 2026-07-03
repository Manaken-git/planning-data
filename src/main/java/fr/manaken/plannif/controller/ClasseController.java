package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.ClasseDTO;
import fr.manaken.plannif.service.ClasseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
public class ClasseController {

    private final ClasseService classeService;

    public ClasseController(ClasseService classeService) {
        this.classeService = classeService;
    }

    @GetMapping("/list")
    public List<ClasseDTO> getClasses() {
        return classeService.getClasses();
    }

    @PostMapping("/create")
    public ClasseDTO createClasse(@RequestBody ClasseDTO dto) {
        return classeService.saveClasse(dto);
    }

    @PutMapping("/update")
    public ClasseDTO updateClasse(@RequestBody ClasseDTO dto) {
        return classeService.saveClasse(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteClasse(@PathVariable Long id) {
        classeService.deleteClasse(id);
    }
}
