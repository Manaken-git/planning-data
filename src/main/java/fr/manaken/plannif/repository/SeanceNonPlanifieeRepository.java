package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.SeanceNonPlanifiee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeanceNonPlanifieeRepository extends JpaRepository<SeanceNonPlanifiee, Long> {
    List<SeanceNonPlanifiee> findByPlanningId(Long planningId);
    void deleteByPlanningId(Long planningId);
}
