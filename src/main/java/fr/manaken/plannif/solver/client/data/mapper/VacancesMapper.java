package fr.manaken.plannif.solver.client.data.mapper;

import fr.manaken.plannif.solver.client.data.dto.VacancesDto;
import fr.manaken.plannif.solver.model.Vacances;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", implementationName = "Solver<CLASS_NAME>Impl", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VacancesMapper {
    VacancesDto toDto(Vacances entity);
    Vacances toEntity(VacancesDto dto);
    List<VacancesDto> toDtoList(List<Vacances> entities);
    List<Vacances> toEntityList(List<VacancesDto> dtos);
}

