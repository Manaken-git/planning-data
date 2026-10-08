package fr.manaken.plannif.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "t_professeur_dayoff")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfesseurDayOff {

    public enum DemiJournee {
        JOURNEE_ENTIERE, MATIN, APRES_MIDI
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "professeur_id")
    private Professeur professeur;

    /**
     * 0 = Monday, 1 = Tuesday, ..., 4 = Friday
     */
    private Integer dayOfWeek;

    @Enumerated(EnumType.STRING)
    private DemiJournee demiJournee = DemiJournee.JOURNEE_ENTIERE;

    private LocalTime heureDebut;

    private LocalTime heureFin;
}

