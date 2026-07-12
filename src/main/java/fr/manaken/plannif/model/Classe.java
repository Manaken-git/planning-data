package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;

    @OneToMany(mappedBy = "classe")
    private Set<Seance> seances = new HashSet<>();

    @OneToMany(mappedBy = "classe")
    private Set<Eleve> eleves = new HashSet<>();

    @OneToMany(mappedBy = "classe")
    private List<ClassePresence> presences = new java.util.ArrayList<>();
}
