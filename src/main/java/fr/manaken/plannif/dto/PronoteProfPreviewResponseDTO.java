package fr.manaken.plannif.dto;

import java.util.List;

public record PronoteProfPreviewResponseDTO(
        boolean succes,
        int totalLignes,
        int nbNouveaux,
        int nbExistants,
        List<PronoteProfPreviewItemDTO> items,
        List<String> avertissements
) {
}
