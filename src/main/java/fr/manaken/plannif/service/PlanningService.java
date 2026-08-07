package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.PlanningDTO;
import fr.manaken.plannif.dto.PlanningSaveDTO;
import fr.manaken.plannif.dto.SeanceSaveDTO;
import fr.manaken.plannif.dto.CreneauDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.PlanningMapper;
import fr.manaken.plannif.model.Planning;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.Creneau;
import fr.manaken.plannif.pusher.DataPusher;
import fr.manaken.plannif.repository.CreneauRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlanningService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final PlanningMapper mapper;
    private final CreneauRepository creneauRepository;

    public PlanningService(DataFetcher dataFetcher, DataPusher dataPusher, PlanningMapper mapper, CreneauRepository creneauRepository) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
        this.creneauRepository = creneauRepository;
    }

    public List<PlanningDTO> getPlannings() {
        return dataFetcher.getPlannings().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public PlanningDTO getPlanning(Long id) {
        if (!dataFetcher.existsPlanning(Math.toIntExact(id))) {
            throw new IllegalArgumentException("Planning avec l'ID " + id + " introuvable.");
        }
        return mapper.toDto(dataFetcher.getPlanning(Math.toIntExact(id)));
    }

    @Transactional(rollbackFor = Exception.class)
    public PlanningDTO savePlanning(PlanningSaveDTO dto) {
        Planning entity;
        if (dto.id() != null && dto.id() > 0 && dataFetcher.existsPlanning(Math.toIntExact(dto.id()))) {
            entity = dataFetcher.getPlanning(Math.toIntExact(dto.id()));
        } else {
            entity = new Planning();
        }
        entity.setNom(dto.nom());
        entity.setDateCreation(dto.dateCreation() != null ? dto.dateCreation() : LocalDateTime.now());

        // First, persist the creneaux and map their temp IDs to database objects (with generated IDs)
        java.util.Map<Long, Creneau> tempIdToDbCreneauMap = new java.util.HashMap<>();
        if (dto.creneaux() != null) {
            for (CreneauDTO cDto : dto.creneaux()) {
                Optional<Creneau> existingOpt = creneauRepository.findByDebutAndFinAndSemaineType(
                        cDto.debut(), cDto.fin(), cDto.semaineType()
                );
                Creneau creneau;
                if (existingOpt.isPresent()) {
                    creneau = existingOpt.get();
                } else {
                    creneau = new Creneau();
                    creneau.setDebut(cDto.debut());
                    creneau.setFin(cDto.fin());
                    creneau.setSemaineType(cDto.semaineType());
                    creneau = dataPusher.saveCreneau(creneau);
                }
                tempIdToDbCreneauMap.put(cDto.id(), creneau);
            }
        }

        // Save planning first
        entity = dataPusher.savePlanning(entity);

        List<Seance> seanceEntities = new ArrayList<>();
        if (dto.seances() != null) {
            for (SeanceSaveDTO sDto : dto.seances()) {
                Seance seance;
                if (sDto.id() != null && sDto.id() > 0 && dataFetcher.existsSeance(Math.toIntExact(sDto.id()))) {
                    seance = dataFetcher.getSeance(Math.toIntExact(sDto.id()));
                } else {
                    seance = new Seance();
                }

                // Resolve relations
                if (sDto.professeurId() != null) {
                    seance.setProfesseur(dataFetcher.getProfesseur(Math.toIntExact(sDto.professeurId())));
                } else {
                    seance.setProfesseur(null);
                }

                if (sDto.classeId() != null) {
                    seance.setClasse(dataFetcher.getClasse(Math.toIntExact(sDto.classeId())));
                } else {
                    seance.setClasse(null);
                }

                if (sDto.matiereId() != null) {
                    seance.setMatiere(dataFetcher.getMatiere(Math.toIntExact(sDto.matiereId())));
                } else {
                    seance.setMatiere(null);
                }

                if (sDto.salleId() != null) {
                    seance.setSalle(dataFetcher.getSalle(Math.toIntExact(sDto.salleId())));
                } else {
                    seance.setSalle(null);
                }

                if (sDto.creneauId() != null) {
                    Creneau creneau = tempIdToDbCreneauMap.get(sDto.creneauId());
                    if (creneau == null && dataFetcher.existsCreneau(Math.toIntExact(sDto.creneauId()))) {
                        creneau = dataFetcher.getCreneau(Math.toIntExact(sDto.creneauId()));
                    }
                    if (creneau != null) {
                        seance.setCreneau(creneau);
                        seance.setDebut(creneau.getDebut());
                        seance.setFin(creneau.getFin());
                    } else {
                        seance.setCreneau(null);
                        seance.setDebut(null);
                        seance.setFin(null);
                    }
                } else {
                    seance.setCreneau(null);
                    seance.setDebut(null);
                    seance.setFin(null);
                }

                if (sDto.type() != null) {
                    seance.setType(Seance.TypeSeance.valueOf(sDto.type().toUpperCase()));
                } else {
                    seance.setType(null);
                }

                seance.setPlanning(entity);
                seance = dataPusher.saveSeance(seance);
                seanceEntities.add(seance);
            }
        }

        entity.setSeances(seanceEntities);
        entity = dataPusher.savePlanning(entity);

        return mapper.toDto(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePlanning(Long id) {
        if (!dataFetcher.existsPlanning(Math.toIntExact(id))) {
            throw new IllegalArgumentException("Planning avec l'ID " + id + " introuvable.");
        }
        Planning planning = dataFetcher.getPlanning(Math.toIntExact(id));
        List<Seance> seances = new ArrayList<>(planning.getSeances());
        for (Seance seance : seances) {
            dataPusher.deleteSeance(Math.toIntExact(seance.getId()));
        }
        dataPusher.deletePlanning(Math.toIntExact(id));
    }
}

