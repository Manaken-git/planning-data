package fr.manaken.plannif.dto;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public record ClasseDTO(
        Long id,
        String nom,
        Integer effectif,
        LocalTime heureDebutMin,
        LocalTime heureFinMax,
        Integer maxHeuresParJour,
        LocalTime pauseDejeunerDebutMin,
        LocalTime pauseDejeunerFinMax,
        Integer pauseDejeunerDureeMin,
        Set<SeanceDTO> seances,
        Set<EleveDTO> eleves,
        List<ClassePresenceDTO> presences
) {
    public ClasseDTO(Long id, String nom, Set<SeanceDTO> seances, Set<EleveDTO> eleves, List<ClassePresenceDTO> presences) {
        this(id, nom, null, null, null, null, null, null, null, seances, eleves, presences);
    }
}
