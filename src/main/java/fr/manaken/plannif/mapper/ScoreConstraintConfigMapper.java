package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ScoreConstraintConfigDTO;
import fr.manaken.plannif.model.ScoreConstraintConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ScoreConstraintConfigMapper {

    ScoreConstraintConfigDTO toDto(ScoreConstraintConfig entity);

    ScoreConstraintConfig toEntity(ScoreConstraintConfigDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mergeWDTO(@MappingTarget ScoreConstraintConfig entity, ScoreConstraintConfigDTO dto);
}
