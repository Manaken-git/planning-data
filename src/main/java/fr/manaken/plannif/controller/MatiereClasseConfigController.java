package fr.manaken.plannif.controller;

import fr.manaken.plannif.dto.MatiereClasseConfigDTO;
import fr.manaken.plannif.service.MatiereClasseConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/configs")
public class MatiereClasseConfigController {

    private final MatiereClasseConfigService configService;

    public MatiereClasseConfigController(MatiereClasseConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/list")
    public List<MatiereClasseConfigDTO> getMatiereClasseConfigs() {
        return configService.getMatiereClasseConfigs();
    }

    @PostMapping("/create")
    public MatiereClasseConfigDTO createMatiereClasseConfig(@RequestBody MatiereClasseConfigDTO dto) {
        return configService.saveMatiereClasseConfig(dto);
    }

    @PutMapping("/update")
    public MatiereClasseConfigDTO updateMatiereClasseConfig(@RequestBody MatiereClasseConfigDTO dto) {
        return configService.saveMatiereClasseConfig(dto);
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMatiereClasseConfig(@PathVariable Long id) {
        configService.deleteMatiereClasseConfig(id);
    }
}
