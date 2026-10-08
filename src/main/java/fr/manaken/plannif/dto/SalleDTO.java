package fr.manaken.plannif.dto;

import java.util.Set;

public record SalleDTO(
        Long id,
        String code,
        Integer capacite,
        String type,
        String batiment,
        Integer etage,
        Set<SeanceDTO> seances
) {
    public SalleDTO(Long id, String code, Integer capacite, String type, Set<SeanceDTO> seances) {
        this(id, code, capacite, type, null, null, seances);
    }
}
