package fr.manaken.plannif.solver.client.data.dto;

import java.time.LocalDate;

public record ClassePresenceDto(
        Long id,
        LocalDate dateDebut,
        LocalDate dateFin
) {}

