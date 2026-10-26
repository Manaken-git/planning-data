package fr.manaken.plannif.dto;

import java.util.List;

public record PronoteClassePreviewResponseDTO(
        boolean succes,
        int totalLignes,
        int nbNouveaux,
        int nbExistants,
        List<PronoteClassePreviewItemDTO> items,
        List<String> avertissements
) {
}
