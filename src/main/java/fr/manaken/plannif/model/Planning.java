package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "t_planning")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Planning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seance> seances = new ArrayList<>();

    @OneToMany(mappedBy = "planning", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeanceNonPlanifiee> seancesNonPlanifiees = new ArrayList<>();

    public void setSeances(List<Seance> seances) {
        if (this.seances == null) {
            this.seances = new ArrayList<>();
        } else {
            this.seances.clear();
        }
        if (seances != null) {
            this.seances.addAll(seances);
        }
    }

    public void setSeancesNonPlanifiees(List<SeanceNonPlanifiee> seancesNonPlanifiees) {
        if (this.seancesNonPlanifiees == null) {
            this.seancesNonPlanifiees = new ArrayList<>();
        } else {
            this.seancesNonPlanifiees.clear();
        }
        if (seancesNonPlanifiees != null) {
            this.seancesNonPlanifiees.addAll(seancesNonPlanifiees);
        }
    }
}
