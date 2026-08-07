package fr.manaken.plannif.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlanningSaveDTO(
        Long id,
        String nom,
        LocalDateTime dateCreation,
        List<SeanceSaveDTO> seances,
        List<CreneauDTO> creneaux
) {}
