package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.MatiereClasseConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatiereClasseConfigRepository extends JpaRepository<MatiereClasseConfig, Integer> {
    List<MatiereClasseConfig> findByClasseIdAndMatiereId(Long classeId, Long matiereId);
}
