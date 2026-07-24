package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.ClasseDTO;
import fr.manaken.plannif.dto.ClassePresenceDTO;
import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.ClassePresence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassePresenceMapper {

    @Mapping(target = "dateDebut", source = "dateDebut")
    @Mapping(target = "dateFin", source = "dateFin")
    @Mapping(target = "classe", source = "classe")
    ClassePresenceDTO toDto(ClassePresence entity);

    @Mapping(target = "classe", ignore = true)
    ClassePresence toEntity(ClassePresenceDTO dto);

    default ClasseDTO classeToClasseDTO(Classe classe) {
        if (classe == null) {
            return null;
        }
        return new ClasseDTO(classe.getId(), classe.getNom(), null, null, null);
    }
}
