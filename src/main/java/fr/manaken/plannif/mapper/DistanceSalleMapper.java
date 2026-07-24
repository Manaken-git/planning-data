package fr.manaken.plannif.mapper;

import fr.manaken.plannif.dto.DistanceSalleDTO;
import fr.manaken.plannif.model.DistanceSalle;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { SalleMapper.class })
public interface DistanceSalleMapper {
    DistanceSalleDTO toDto(DistanceSalle entity);
}
