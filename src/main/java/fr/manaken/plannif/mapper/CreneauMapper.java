package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.CreneauDTO;
import fr.manaken.plannif.model.Creneau;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CreneauMapper {
    CreneauDTO toDto(Creneau entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mergeWDTO(@MappingTarget Creneau entity, CreneauDTO dto);
}
