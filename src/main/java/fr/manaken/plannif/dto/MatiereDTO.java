package fr.manaken.plannif.dto;

import java.util.Set;

public record MatiereDTO(Long id, String nom, Long volumeHoraireAnnuel, Set<SeanceDTO> seances) {}
