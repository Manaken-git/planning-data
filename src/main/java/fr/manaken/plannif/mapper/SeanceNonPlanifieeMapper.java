package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.SeanceNonPlanifieeDTO;
import fr.manaken.plannif.model.SeanceNonPlanifiee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeanceNonPlanifieeMapper {

    @Mapping(target = "professeurNomComplet", expression = "java(seance.getProfesseur() != null ? seance.getProfesseur().getPrenom() + \" \" + seance.getProfesseur().getNom() : null)")
    @Mapping(target = "classeNom", source = "classe.nom")
    @Mapping(target = "matiereNom", source = "matiere.nom")
    @Mapping(target = "salleCode", source = "salle.code")
    @Mapping(target = "type", expression = "java(seance.getType() != null ? seance.getType().name() : null)")
    @Mapping(target = "debut", source = "debut")
    @Mapping(target = "fin", source = "fin")
    @Mapping(target = "groupe", source = "groupe")
    @Mapping(target = "alignementCode", source = "alignementCode")
    @Mapping(target = "motifEchec", source = "motifEchec")
    SeanceNonPlanifieeDTO toDto(SeanceNonPlanifiee seance);
}
