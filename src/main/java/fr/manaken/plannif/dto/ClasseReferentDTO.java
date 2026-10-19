package fr.manaken.plannif.dto;

import fr.manaken.plannif.model.RoleReferent;

public record ClasseReferentDTO(
        Long id,
        Long professeurId,
        String professeurNom,
        String professeurPrenom,
        RoleReferent role
) {
}