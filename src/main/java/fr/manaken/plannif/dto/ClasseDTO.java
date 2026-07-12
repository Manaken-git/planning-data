package fr.manaken.plannif.dto;

import fr.manaken.plannif.model.ClassePresence;
import fr.manaken.plannif.model.Eleve;
import fr.manaken.plannif.model.Seance;

import java.util.List;
import java.util.Set;

public record ClasseDTO(Long id, String nom, Set<Seance> seances, Set<Eleve> eleves, List<ClassePresence> presences) {}
