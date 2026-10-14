package fr.manaken.plannif.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlanningDTO(
        Long id,
        String nom,
        LocalDateTime dateCreation,
        List<SeanceDTO> seances,
        List<SeanceNonPlanifieeDTO> seancesNonPlanifiees
) {
    public PlanningDTO(Long id, String nom, LocalDateTime dateCreation, List<SeanceDTO> seances) {
        this(id, nom, dateCreation, seances, List.of());
    }
}
