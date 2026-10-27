package fr.manaken.plannif.solver.client.data.mapper;

import fr.manaken.plannif.solver.client.data.dto.SalleDto;
import fr.manaken.plannif.solver.model.Salle;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "Solver<CLASS_NAME>Impl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SalleMapper {
    SalleDto toDto(Salle entity);
    Salle toEntity(SalleDto dto);
    List<SalleDto> toDtoList(List<Salle> entities);
    List<Salle> toEntityList(List<SalleDto> dtos);
}

