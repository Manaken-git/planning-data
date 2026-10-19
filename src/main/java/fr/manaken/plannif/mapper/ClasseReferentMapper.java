package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ClasseReferentDTO;
import fr.manaken.plannif.model.ClasseReferent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClasseReferentMapper {

    @Mapping(target = "professeurId", source = "professeur.id")
    @Mapping(target = "professeurNom", source = "professeur.nom")
    @Mapping(target = "professeurPrenom", source = "professeur.prenom")
    ClasseReferentDTO toDto(ClasseReferent entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "professeur.id", source = "professeurId")
    @Mapping(target = "classe", ignore = true)
    ClasseReferent toEntity(ClasseReferentDTO dto);
}