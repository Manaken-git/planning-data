package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Professeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;
    private String prenom;
    private String email;
    private BigDecimal nb_heures;
    private BigDecimal maxHeuresParJour;
    private BigDecimal maxHeuresParSemaine;
    private BigDecimal maxHeuresParSeance;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "plage_horaire_preferee_id", referencedColumnName = "id")
    private PlageHoraire plageHorairePreferee; // Nouveau champ

    @OneToMany(mappedBy = "professeur")
    private Set<Seance> seances = new HashSet<>();

    @OneToMany(mappedBy = "professeur", fetch = FetchType.EAGER)
    private List<ProfesseurDayOff> daysOff = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "tj_professeur_matiere",
        joinColumns = @JoinColumn(name = "professeur_id"),
        inverseJoinColumns = @JoinColumn(name = "matiere_id")
    )
    private Set<Matiere> matieres = new HashSet<>();
}
