package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.MatiereClasseConfigDTO;
import fr.manaken.plannif.model.MatiereClasseConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MatiereClasseConfigMapper {

    @Mapping(target = "classeId", source = "classe.id")
    @Mapping(target = "classeNom", source = "classe.nom")
    @Mapping(target = "matiereId", source = "matiere.id")
    @Mapping(target = "matiereNom", source = "matiere.nom")
    MatiereClasseConfigDTO toDto(MatiereClasseConfig entity);

    @Mapping(target = "classe", ignore = true)
    @Mapping(target = "matiere", ignore = true)
    MatiereClasseConfig toEntity(MatiereClasseConfigDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "classe", ignore = true)
    @Mapping(target = "matiere", ignore = true)
    void mergeWDTO(@MappingTarget MatiereClasseConfig entity, MatiereClasseConfigDTO dto);
}
