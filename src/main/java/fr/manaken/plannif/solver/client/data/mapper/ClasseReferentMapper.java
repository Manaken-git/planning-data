package fr.manaken.plannif.solver.client.data.mapper;

import fr.manaken.plannif.solver.client.data.dto.ClasseReferentDto;
import fr.manaken.plannif.solver.model.ClasseReferent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "Solver<CLASS_NAME>Impl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClasseReferentMapper {

    @Mapping(target = "professeurId", source = "professeur.id")
    @Mapping(target = "professeurNom", source = "professeur.nom")
    @Mapping(target = "professeurPrenom", source = "professeur.prenom")
    ClasseReferentDto toDto(ClasseReferent entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "professeur.id", source = "professeurId")
    @Mapping(target = "professeur.nom", source = "professeurNom")
    @Mapping(target = "professeur.prenom", source = "professeurPrenom")
    @Mapping(target = "classe", ignore = true)
    ClasseReferent toEntity(ClasseReferentDto dto);
}

