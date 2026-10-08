package fr.manaken.plannif.dto;

public record ScoreConstraintConfigDTO(
        Long id,
        String nom,
        Boolean isDefault,
        Integer roomConflictWeight,
        Integer teacherConflictWeight,
        Integer studentGroupConflictWeight,
        Integer teacherDayOffWeight,
        Integer teacherMaxHoursPerDayWeight,
        Integer teacherMaxHoursPerWeekWeight,
        Integer teacherMaxGapWeight,
        Integer seanceDurationMatchWeight,
        Integer studentLunchBreakWeight,
        Integer teacherLunchBreakWeight,
        Integer studentDailySpanWeight,
        Integer studentGapWeight,
        Integer travelDistanceWeight,
        Integer cognitiveLoadMorningWeight,
        Integer twoHourBlockWeight
) {}
