package fr.manaken.plannif.dto;

import java.util.List;

public record PronoteImportResultDTO(
        boolean succes,
        String typeEntite,
        int totalLignes,
        int nbCrees,
        int nbMisAJour,
        List<String> avertissements
) {
}
