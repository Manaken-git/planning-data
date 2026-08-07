package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeanceRepository extends JpaRepository<Seance, Integer> {
    List<Seance> findByPlanningIsNull();
}
