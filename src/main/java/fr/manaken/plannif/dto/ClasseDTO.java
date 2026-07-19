package fr.manaken.plannif.dto;

import java.util.List;
import java.util.Set;

public record ClasseDTO(Long id, String nom, Set<SeanceDTO> seances, Set<EleveDTO> eleves, List<ClassePresenceDTO> presences) {}
