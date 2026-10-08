package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ProfesseurDayOffDTO;
import fr.manaken.plannif.model.ProfesseurDayOff;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfesseurDayOffMapper {

    @Mapping(target = "professeur", ignore = true)
    ProfesseurDayOffDTO toDto(ProfesseurDayOff entity);

    @Mapping(target = "professeur", ignore = true)
    ProfesseurDayOff toEntity(ProfesseurDayOffDTO dto);
}
