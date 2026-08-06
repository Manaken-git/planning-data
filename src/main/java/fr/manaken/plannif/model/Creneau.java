package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "t_creneau")
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Creneau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private LocalDateTime debut;
    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(name = "semaine_type")
    private SemaineType semaineType;

}
