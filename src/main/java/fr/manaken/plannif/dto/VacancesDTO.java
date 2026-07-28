package fr.manaken.plannif.dto;

import java.time.LocalDate;

public record VacancesDTO(
        Long id,
        String nom,
        LocalDate dateDebut,
        LocalDate dateFin
) {}
