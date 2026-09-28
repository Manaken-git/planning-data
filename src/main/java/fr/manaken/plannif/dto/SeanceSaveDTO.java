package fr.manaken.plannif.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SeanceSaveDTO(
        Long id,
        Long professeurId,
        Long classeId,
        Long matiereId,
        Long salleId,
        String type,
        LocalDateTime debut,
        LocalDateTime fin
) {}
