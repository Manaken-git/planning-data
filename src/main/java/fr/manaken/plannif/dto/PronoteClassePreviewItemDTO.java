package fr.manaken.plannif.dto;

public record PronoteClassePreviewItemDTO(
        String nom,
        Integer effectif,
        String niveau,
        String filiere,
        String profPrincipalTitulaire,
        boolean profPrincipalTitulaireExistant,
        Long profPrincipalTitulaireId,
        String profPrincipalSuppleant,
        boolean profPrincipalSuppleantExistant,
        Long profPrincipalSuppleantId,
        boolean existant,
        Long existingId
) {
}
