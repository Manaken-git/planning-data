package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.MatiereDTO;
import fr.manaken.plannif.model.Matiere;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = { SeanceMapper.class })
public interface MatiereMapper {
    MatiereDTO toDto(Matiere entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mergeWDTO(@MappingTarget Matiere entity, MatiereDTO dto);
}
