package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.PlanningDTO;
import fr.manaken.plannif.model.Planning;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SeanceMapper.class})
public interface PlanningMapper {
    PlanningDTO toDto(Planning planning);
}
