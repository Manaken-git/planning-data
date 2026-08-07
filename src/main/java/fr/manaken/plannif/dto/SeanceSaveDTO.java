package fr.manaken.plannif.dto;

public record SeanceSaveDTO(
        Long id,
        Long professeurId,
        Long classeId,
        Long matiereId,
        Long salleId,
        Long creneauId,
        String type
) {}
