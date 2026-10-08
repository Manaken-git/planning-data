package fr.manaken.plannif.dto;

import java.time.LocalDateTime;

public record SeanceDTO(
        Long id,
        LocalDateTime debut,
        LocalDateTime fin,
        String professeurNomComplet,
        String classeNom,
        String matiereNom,
        String salleCode,
        String type,
        String groupe,
        String alignementCode
) {
    public SeanceDTO(Long id, LocalDateTime debut, LocalDateTime fin, String professeurNomComplet, String classeNom, String matiereNom, String salleCode) {
        this(id, debut, fin, professeurNomComplet, classeNom, matiereNom, salleCode, null, null, null);
    }
}
