package fr.manaken.plannif.dto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

public record MatiereClasseConfigDTO(
        Long id,
        Long classeId,
        String classeNom,
        Long matiereId,
        String matiereNom,
        LocalDate dateDebut,
        LocalDate dateFin,
        Long volumeHorairePeriode,
        String typeSalleRequis,
        Integer dureeCoursMinutes,
        Integer dureeTpMinutes,
        Boolean forteChargeCognitive,
        Boolean autoriserBlocDeuxHeures,
        Set<EquipementDTO> equipementsRequis
) {
    public MatiereClasseConfigDTO(
            Long id,
            Long classeId,
            String classeNom,
            Long matiereId,
            String matiereNom,
            LocalDate dateDebut,
            LocalDate dateFin,
            Long volumeHorairePeriode
    ) {
        this(id, classeId, classeNom, matiereId, matiereNom, dateDebut, dateFin, volumeHorairePeriode,
                null, null, null, false, false, Collections.emptySet());
    }
}
