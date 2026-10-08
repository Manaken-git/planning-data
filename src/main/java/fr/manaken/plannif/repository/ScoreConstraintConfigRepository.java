package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.ScoreConstraintConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoreConstraintConfigRepository extends JpaRepository<ScoreConstraintConfig, Integer> {
    Optional<ScoreConstraintConfig> findByIsDefaultTrue();
}
