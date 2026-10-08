package fr.manaken.plannif.dto;

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
        Integer pauseDejeunerDureeMin,
        PlageHoraireDTO plageHorairePreferee,
        Set<SeanceDTO> seances,
        List<ProfesseurDayOffDTO> daysOff,
        Set<MatiereDTO> matieres
) {
    public ProfesseurDTO(
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
            List<ProfesseurDayOffDTO> daysOff,
            Set<MatiereDTO> matieres
    ) {
        this(id, nom, prenom, email, nb_heures, maxHeuresParJour, maxHeuresParSemaine, maxHeuresParSeance, null, plageHorairePreferee, seances, daysOff, matieres);
    }
}
