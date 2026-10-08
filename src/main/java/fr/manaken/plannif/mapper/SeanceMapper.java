package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.SeanceDTO;
import fr.manaken.plannif.model.Seance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeanceMapper {

    @Mapping(target = "professeurNomComplet", expression = "java(seance.getProfesseur() != null ? seance.getProfesseur().getPrenom() + \" \" + seance.getProfesseur().getNom() : null)")
    @Mapping(target = "classeNom", source = "classe.nom")
    @Mapping(target = "matiereNom", source = "matiere.nom")
    @Mapping(target = "salleCode", source = "salle.code")
    @Mapping(target = "type", expression = "java(seance.getType() != null ? seance.getType().name() : null)")
    @Mapping(target = "debut", source = "debut")
    @Mapping(target = "fin", source = "fin")
    @Mapping(target = "groupe", source = "groupe")
    @Mapping(target = "alignementCode", source = "alignementCode")
    SeanceDTO toDto(Seance seance);

    @Mapping(target = "professeur", ignore = true)
    @Mapping(target = "classe", ignore = true)
    @Mapping(target = "matiere", ignore = true)
    @Mapping(target = "salle", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "planning", ignore = true)
    @Mapping(target = "debut", source = "debut")
    @Mapping(target = "fin", source = "fin")
    @Mapping(target = "groupe", source = "groupe")
    @Mapping(target = "alignementCode", source = "alignementCode")
    Seance toEntity(SeanceDTO seanceDTO);

}
