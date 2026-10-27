package fr.manaken.plannif.solver.client.data.mapper;

import fr.manaken.plannif.solver.client.data.dto.MatiereDto;
import fr.manaken.plannif.solver.model.Matiere;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "Solver<CLASS_NAME>Impl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatiereMapper {
    MatiereDto toDto(Matiere entity);
    Matiere toEntity(MatiereDto dto);
    List<MatiereDto> toDtoList(List<Matiere> entities);
    List<Matiere> toEntityList(List<MatiereDto> dtos);
}

