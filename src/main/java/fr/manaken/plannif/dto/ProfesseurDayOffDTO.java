package fr.manaken.plannif.dto;

import java.time.LocalTime;

public record ProfesseurDayOffDTO(
        Long id,
        ProfesseurDTO professeur,
        Integer dayOfWeek,
        String demiJournee,
        LocalTime heureDebut,
        LocalTime heureFin
) {
    public ProfesseurDayOffDTO(Long id, ProfesseurDTO professeur, Integer dayOfWeek) {
        this(id, professeur, dayOfWeek, "JOURNEE_ENTIERE", null, null);
    }
}
