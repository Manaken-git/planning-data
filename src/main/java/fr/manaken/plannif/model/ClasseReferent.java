package fr.manaken.plannif.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@Entity
@Table(name = "t_classe_referent", uniqueConstraints = {
    @UniqueConstraint(name = "uq_classe_professeur_referent", columnNames = {"classe_id", "professeur_id"})
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClasseReferent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    @JsonIgnore
    private Classe classe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "professeur_id", nullable = false)
    private Professeur professeur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoleReferent role = RoleReferent.PRINCIPAL;
}