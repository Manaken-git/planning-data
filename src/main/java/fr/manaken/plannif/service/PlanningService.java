package fr.manaken.plannif.service;

import fr.manaken.plannif.dto.PlanningDTO;
import fr.manaken.plannif.dto.PlanningSaveDTO;
import fr.manaken.plannif.dto.SeanceNonPlanifieeDTO;
import fr.manaken.plannif.dto.SeanceSaveDTO;
import fr.manaken.plannif.fetcher.DataFetcher;
import fr.manaken.plannif.mapper.PlanningMapper;
import fr.manaken.plannif.mapper.SeanceNonPlanifieeMapper;
import fr.manaken.plannif.model.Planning;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.SeanceNonPlanifiee;
import fr.manaken.plannif.pusher.DataPusher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlanningService {

    private final DataFetcher dataFetcher;
    private final DataPusher dataPusher;
    private final PlanningMapper mapper;
    private final SeanceNonPlanifieeMapper seanceNonPlanifieeMapper;

    public PlanningService(DataFetcher dataFetcher, DataPusher dataPusher, PlanningMapper mapper, SeanceNonPlanifieeMapper seanceNonPlanifieeMapper) {
        this.dataFetcher = dataFetcher;
        this.dataPusher = dataPusher;
        this.mapper = mapper;
        this.seanceNonPlanifieeMapper = seanceNonPlanifieeMapper;
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

    public List<SeanceNonPlanifieeDTO> getSeancesNonPlanifiees(Long planningId) {
        if (!dataFetcher.existsPlanning(Math.toIntExact(planningId))) {
            throw new IllegalArgumentException("Planning avec l'ID " + planningId + " introuvable.");
        }
        return dataFetcher.getSeancesNonPlanifieesByPlanningId(planningId).stream()
                .map(seanceNonPlanifieeMapper::toDto)
                .collect(Collectors.toList());
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

        // Save planning first
        entity = dataPusher.savePlanning(entity);

        List<Seance> seanceEntities = new ArrayList<>();
        List<SeanceNonPlanifiee> seancesNonPlanifiees = new ArrayList<>();

        if (dto.seances() != null) {
            for (SeanceSaveDTO sDto : dto.seances()) {
                List<String> motifs = new ArrayList<>();
                if (sDto.professeurId() == null) {
                    motifs.add("PROFESSEUR_MANQUANT");
                }
                if (sDto.salleId() == null) {
                    motifs.add("SALLE_MANQUANTE");
                }
                if (sDto.debut() == null) {
                    motifs.add("CRENEAU_DEBUT_MANQUANT");
                }
                if (sDto.classeId() == null) {
                    motifs.add("CLASSE_MANQUANTE");
                }
                if (sDto.matiereId() == null) {
                    motifs.add("MATIERE_MANQUANTE");
                }

                if (motifs.isEmpty()) {
                    // Séance valide et complète
                    Seance seance;
                    if (sDto.id() != null && sDto.id() > 0 && dataFetcher.existsSeance(Math.toIntExact(sDto.id()))) {
                        seance = dataFetcher.getSeance(Math.toIntExact(sDto.id()));
                    } else {
                        seance = new Seance();
                    }

                    seance.setProfesseur(dataFetcher.getProfesseur(Math.toIntExact(sDto.professeurId())));
                    seance.setClasse(dataFetcher.getClasse(Math.toIntExact(sDto.classeId())));
                    seance.setMatiere(dataFetcher.getMatiere(Math.toIntExact(sDto.matiereId())));
                    seance.setSalle(dataFetcher.getSalle(Math.toIntExact(sDto.salleId())));
                    seance.setDebut(sDto.debut());
                    seance.setFin(calculateFin(sDto));

                    if (sDto.type() != null) {
                        try {
                            seance.setType(Seance.TypeSeance.valueOf(sDto.type().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            seance.setType(null);
                        }
                    } else {
                        seance.setType(null);
                    }

                    seance.setGroupe(sDto.groupe());
                    seance.setAlignementCode(sDto.alignementCode());
                    seance.setPlanning(entity);

                    seance = dataPusher.saveSeance(seance);
                    seanceEntities.add(seance);
                } else {
                    // Séance incomplète ou non planifiée
                    SeanceNonPlanifiee snp = new SeanceNonPlanifiee();
                    if (sDto.professeurId() != null) {
                        snp.setProfesseur(dataFetcher.getProfesseur(Math.toIntExact(sDto.professeurId())));
                    }
                    if (sDto.classeId() != null) {
                        snp.setClasse(dataFetcher.getClasse(Math.toIntExact(sDto.classeId())));
                    }
                    if (sDto.matiereId() != null) {
                        snp.setMatiere(dataFetcher.getMatiere(Math.toIntExact(sDto.matiereId())));
                    }
                    if (sDto.salleId() != null) {
                        snp.setSalle(dataFetcher.getSalle(Math.toIntExact(sDto.salleId())));
                    }
                    snp.setDebut(sDto.debut());
                    snp.setFin(calculateFin(sDto));

                    if (sDto.type() != null) {
                        try {
                            snp.setType(Seance.TypeSeance.valueOf(sDto.type().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            snp.setType(null);
                        }
                    } else {
                        snp.setType(null);
                    }

                    snp.setGroupe(sDto.groupe());
                    snp.setAlignementCode(sDto.alignementCode());
                    snp.setMotifEchec(String.join(", ", motifs));
                    snp.setDateCreation(LocalDateTime.now());
                    snp.setPlanning(entity);

                    snp = dataPusher.saveSeanceNonPlanifiee(snp);
                    seancesNonPlanifiees.add(snp);
                }
            }
        }

        entity.setSeances(seanceEntities);
        entity.setSeancesNonPlanifiees(seancesNonPlanifiees);
        entity = dataPusher.savePlanning(entity);

        return mapper.toDto(entity);
    }

    private LocalDateTime calculateFin(SeanceSaveDTO sDto) {
        if (sDto.fin() != null) {
            return sDto.fin();
        }
        if (sDto.debut() == null) {
            return null;
        }

        int duration = 60;
        boolean isTp = sDto.type() != null && sDto.type().equalsIgnoreCase("TP");
        fr.manaken.plannif.model.MatiereClasseConfig config = null;
        if (sDto.classeId() != null && sDto.matiereId() != null) {
            List<fr.manaken.plannif.model.MatiereClasseConfig> configs =
                    dataFetcher.getMatiereClasseConfigsByClasseAndMatiere(sDto.classeId(), sDto.matiereId());
            if (configs != null && !configs.isEmpty()) {
                config = configs.get(0);
            }
        }

        if (isTp) {
            duration = (config != null && config.getDureeTpMinutes() != null)
                    ? config.getDureeTpMinutes()
                    : 90;
        } else {
            duration = (config != null && config.getDureeCoursMinutes() != null)
                    ? config.getDureeCoursMinutes()
                    : 60;
        }
        return sDto.debut().plusMinutes(duration);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePlanning(Long id) {
        if (!dataFetcher.existsPlanning(Math.toIntExact(id))) {
            throw new IllegalArgumentException("Planning avec l'ID " + id + " introuvable.");
        }
        dataPusher.deleteSeancesByPlanningId(id);
        dataPusher.deleteSeancesNonPlanifieesByPlanningId(id);
        dataPusher.deletePlanning(Math.toIntExact(id));
    }
}
