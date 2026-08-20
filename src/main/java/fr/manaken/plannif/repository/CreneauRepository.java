package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.Creneau;
import fr.manaken.plannif.model.SemaineType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CreneauRepository extends JpaRepository<Creneau, Integer> {
    Optional<Creneau> findByDebutAndFinAndSemaineType(LocalDateTime debut, LocalDateTime fin, SemaineType semaineType);
    Optional<Creneau> findByDebutAndFinAndSemaineTypeAndTypeClasse(LocalDateTime debut, LocalDateTime fin, SemaineType semaineType, String typeClasse);
}
