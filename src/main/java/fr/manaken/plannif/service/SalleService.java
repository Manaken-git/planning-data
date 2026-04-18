package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.SalleDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.SalleMapper;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalleService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final SalleMapper mapper;

    public SalleService(DataFetcher dataFetcher, DataPusher dataPusher, SalleMapper mapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
    }

    public List<SalleDTO> getSalles() {
        return dataFetcher.getSalles().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public SalleDTO saveSalle(SalleDTO s) {
        Salle sBDD = null;
        if (s.id() != null) {
            sBDD = dataFetcher.getSalle(Math.toIntExact(s.id()));
        } else {
            sBDD = new Salle();
        }
        mapper.mergeWDTO(sBDD, s);
        return mapper.toDto(dataPusher.saveSalle(sBDD));
    }
}
