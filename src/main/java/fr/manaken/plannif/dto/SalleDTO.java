package fr.manaken.plannif.dto;

import fr.manaken.plannif.model.Seance;

import java.util.Set;

public record SalleDTO(Long id, String code, Integer capacite, String type, Set<Seance> seances) {}
