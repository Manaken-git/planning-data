package fr.manaken.plannif.dto;

import fr.manaken.plannif.model.Seance;

import java.util.Set;

public record MatiereDTO(Long id, String nom, Long volumeHoraireAnnuel, Set<Seance> seances) {}
