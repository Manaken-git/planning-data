package fr.manaken.plannif.dto;

import java.time.LocalDateTime;

public record SeanceNonPlanifieeDTO(
        Long id,
        LocalDateTime debut,
        LocalDateTime fin,
        String professeurNomComplet,
        String classeNom,
        String matiereNom,
        String salleCode,
        String type,
        String groupe,
        String alignementCode,
        String motifEchec
) {}
