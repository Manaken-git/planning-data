package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.Vacances;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VacancesRepository extends JpaRepository<Vacances, Long> {
}
