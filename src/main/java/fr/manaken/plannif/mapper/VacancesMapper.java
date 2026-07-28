package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.VacancesDTO;
import fr.manaken.plannif.model.Vacances;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface VacancesMapper {
    VacancesDTO toDto(Vacances entity);
    Vacances toEntity(VacancesDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mergeWDTO(@MappingTarget Vacances entity, VacancesDTO dto);
}
