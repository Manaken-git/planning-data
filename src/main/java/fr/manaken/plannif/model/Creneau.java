package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

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

    private LocalTime debut;
    private LocalTime fin;

}
