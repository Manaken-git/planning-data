package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.EleveDTO;
import fr.manaken.plannif.model.Eleve;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EleveMapper {

    EleveDTO toDto(Eleve eleve);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "classe", ignore = true)
    void mergeWDTO(@MappingTarget Eleve entity, EleveDTO dto);
}
