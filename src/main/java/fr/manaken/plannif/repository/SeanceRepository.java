package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {
    List<Seance> findByPlanningIsNull();
    List<Seance> findByPlanningId(Long planningId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Seance s WHERE s.planning.id = :planningId")
    void deleteByPlanningId(@Param("planningId") Long planningId);
}
