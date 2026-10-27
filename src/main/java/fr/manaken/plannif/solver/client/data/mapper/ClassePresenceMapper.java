package fr.manaken.plannif.solver.client.data.mapper;

import fr.manaken.plannif.solver.client.data.dto.ClassePresenceDto;
import fr.manaken.plannif.solver.model.ClassePresence;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", implementationName = "Solver<CLASS_NAME>Impl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClassePresenceMapper {
    ClassePresenceDto toDto(ClassePresence entity);
    ClassePresence toEntity(ClassePresenceDto dto);
}

