package fr.manaken.plannif.dto;

import java.util.Set;

public record SalleDTO(Long id, String code, Integer capacite, String type, Set<SeanceDTO> seances) {}
