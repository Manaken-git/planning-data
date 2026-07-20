package fr.manaken.plannif.repository;

import fr.manaken.plannif.model.Creneau;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreneauRepository extends JpaRepository<Creneau, Integer> {
}
