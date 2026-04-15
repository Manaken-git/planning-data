package fr.manaken.plannif.model;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;


@Getter
@Setter
@Entity

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    @EqualsAndHashCode.Include
    private Long id;

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

    @OneToOne
    @JoinColumn(name = "creneau_id")
    
    private Creneau creneau;

    @Enumerated(EnumType.STRING)
    private TypeSeance type;

    public enum TypeSeance {
        COURS, TP, EXAMEN
    }

}
