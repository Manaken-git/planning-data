package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "t_seance_non_planifiee")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SeanceNonPlanifiee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private LocalDateTime debut;
    private LocalDateTime fin;

    @ManyToOne
    @JoinColumn(name = "professeur_id")
    private Professeur professeur;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @ManyToOne
    @JoinColumn(name = "matiere_id")
    private Matiere matiere;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    private Salle salle;

    @ManyToOne
    @JoinColumn(name = "planning_id")
    private Planning planning;

    @Enumerated(EnumType.STRING)
    private Seance.TypeSeance type;

    private String groupe;

    private String alignementCode;

    @Column(name = "motif_echec")
    private String motifEchec;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;
}
