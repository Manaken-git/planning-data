package fr.manaken.plannif.dto;

import java.util.List;

public record PronoteProfPreviewItemDTO(
        String civilite,
        String nom,
        String prenom,
        String email,
        String discipline,
        List<String> classesPp,
        boolean existant,
        Long existingId,
        boolean matiereConnue,
        List<String> classesConnues,
        List<String> classesInconnues
) {
}
