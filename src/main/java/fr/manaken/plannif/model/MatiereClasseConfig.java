package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "t_matiere_classe_config")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MatiereClasseConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;

    @ManyToOne
    @JoinColumn(name = "matiere_id", nullable = false)
    private Matiere matiere;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    private Long volumeHorairePeriode;

    private String typeSalleRequis;

    private Integer dureeCoursMinutes;

    private Integer dureeTpMinutes;

    @Builder.Default
    private Boolean forteChargeCognitive = false;

    @Builder.Default
    private Boolean autoriserBlocDeuxHeures = false;

    @ManyToMany
    @JoinTable(
        name = "tj_matiere_classe_config_equipement",
        joinColumns = @JoinColumn(name = "config_id"),
        inverseJoinColumns = @JoinColumn(name = "equipement_id")
    )
    @Builder.Default
    private Set<Equipement> equipementsRequis = new HashSet<>();
}
