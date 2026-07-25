package fr.manaken.plannif.dto;

import java.time.LocalDate;

public record MatiereClasseConfigDTO(
        Long id,
        Long classeId,
        String classeNom,
        Long matiereId,
        String matiereNom,
        LocalDate dateDebut,
        LocalDate dateFin,
        Long volumeHorairePeriode
) {}
