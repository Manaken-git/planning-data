package fr.manaken.plannif.dto;

public record DistanceSalleDTO(
        Long id,
        SalleDTO salle1,
        SalleDTO salle2,
        Long distance,
        Integer tempsTransitionMinutes
) {
    public DistanceSalleDTO(Long id, SalleDTO salle1, SalleDTO salle2, Long distance) {
        this(id, salle1, salle2, distance, null);
    }
}
