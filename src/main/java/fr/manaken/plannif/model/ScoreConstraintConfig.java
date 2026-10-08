package fr.manaken.plannif.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@Entity
@Table(name = "t_constraint_configuration")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ScoreConstraintConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nom;

    private Boolean isDefault = false;

    private Integer roomConflictWeight = 1;
    private Integer teacherConflictWeight = 1;
    private Integer studentGroupConflictWeight = 1;
    private Integer teacherDayOffWeight = 1;
    private Integer teacherMaxHoursPerDayWeight = 1;
    private Integer teacherMaxHoursPerWeekWeight = 1;
    private Integer teacherMaxGapWeight = 1;
    private Integer seanceDurationMatchWeight = 1;
    private Integer studentLunchBreakWeight = 1;
    private Integer teacherLunchBreakWeight = 1;
    private Integer studentDailySpanWeight = 1;
    private Integer studentGapWeight = 1;
    private Integer travelDistanceWeight = 1;
    private Integer cognitiveLoadMorningWeight = 1;
    private Integer twoHourBlockWeight = 1;
}
