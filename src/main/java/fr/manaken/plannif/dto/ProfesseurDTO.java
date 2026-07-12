package fr.manaken.plannif.dto;

import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.model.ProfesseurDayOff;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record ProfesseurDTO(
    Long id,
    String nom,
    String prenom,
    String email,
    BigDecimal nb_heures,
    BigDecimal maxHeuresParJour,
    BigDecimal maxHeuresParSemaine,
    BigDecimal maxHeuresParSeance,
    PlageHoraireDTO plageHorairePreferee,
    Set<SeanceDTO> seances,
    List<ProfesseurDayOff> daysOff,
    Set<Matiere> matieres) {}
