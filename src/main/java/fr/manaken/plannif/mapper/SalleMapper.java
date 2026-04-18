package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.SalleDTO;
import fr.manaken.plannif.model.Salle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SalleMapper {
    SalleDTO toDto(Salle entity);

    @org.mapstruct.BeanMapping(nullValuePropertyMappingStrategy = org.mapstruct.NullValuePropertyMappingStrategy.IGNORE)
    void mergeWDTO(@org.mapstruct.MappingTarget Salle entity, SalleDTO dto);
}
