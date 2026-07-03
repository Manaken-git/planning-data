package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ClasseDTO;
import fr.manaken.plannif.model.Classe;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ClasseMapper {
    ClasseDTO toDto(Classe entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mergeWDTO(@MappingTarget Classe entity, ClasseDTO dto);
}
