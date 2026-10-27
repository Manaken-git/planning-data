package fr.manaken.plannif.solver.client.data.dto;

import fr.manaken.plannif.solver.model.SemaineType;
import java.time.LocalDateTime;

public record CreneauDTO(Long id, LocalDateTime debut, LocalDateTime fin, SemaineType semaineType) {}


