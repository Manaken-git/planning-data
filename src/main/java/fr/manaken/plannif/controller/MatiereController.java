package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.MatiereDTO;
import fr.manaken.plannif.service.MatiereService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matieres")
public class MatiereController {

    private final MatiereService matiereService;

    public MatiereController(MatiereService matiereService) {
        this.matiereService = matiereService;
    }

    @GetMapping("/list")
    public List<MatiereDTO> getMatieres() {
        return matiereService.getMatieres();
    }

    @PostMapping("/create")
    public MatiereDTO createMatiere(@RequestBody MatiereDTO dto) {
        return matiereService.saveMatiere(dto);
    }

    @PutMapping("/update")
    public MatiereDTO updateMatiere(@RequestBody MatiereDTO dto) {
        return matiereService.saveMatiere(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatiere(@PathVariable Long id) {
        matiereService.deleteMatiere(id);
    }
}
