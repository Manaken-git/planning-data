package fr.manaken.plannif.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlanningSaveDTO(
        Long id,
        String nom,
        LocalDateTime dateCreation,
        List<SeanceSaveDTO> seances
) {}
