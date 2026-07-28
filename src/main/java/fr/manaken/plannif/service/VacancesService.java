package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.VacancesDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.VacancesMapper;
import fr.manaken.plannif.model.Vacances;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VacancesService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final VacancesMapper mapper;

    public VacancesService(DataFetcher dataFetcher, DataPusher dataPusher, VacancesMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<VacancesDTO> getVacances() {
        return dataFetcher.getVacances().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public VacancesDTO saveVacances(VacancesDTO dto) {
        if (dto.dateDebut() == null || dto.dateFin() == null) {
            throw new IllegalArgumentException("Les dates de début et de fin de vacances sont requises.");
        }
        if (dto.dateFin().isBefore(dto.dateDebut())) {
            throw new IllegalArgumentException("La date de fin ne peut pas être antérieure à la date de début.");
        }

        Vacances entity;
        if (dto.id() != null) {
            entity = dataFetcher.getVacances(Math.toIntExact(dto.id()));
        } else {
            entity = new Vacances();
        }
        
        mapper.mergeWDTO(entity, dto);
        return mapper.toDto(dataPusher.saveVacances(entity));
    }

    public void deleteVacances(Long id) {
        dataPusher.deleteVacances(Math.toIntExact(id));
    }
}
