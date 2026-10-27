package fr.manaken.plannif.solver.client.data.dto;

import java.time.LocalDate;

public record VacancesDto(
        Long id,
        String nom,
        LocalDate dateDebut,
        LocalDate dateFin
) {}

