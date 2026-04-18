package fr.manaken.plannif.pusher;

import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.repository.ProfesseurRepository;
import fr.manaken.plannif.repository.SalleRepository;
import lombok.NonNull;

import org.springframework.stereotype.Service;

@Service
public class DataPusher {
    private final ProfesseurRepository professeurRepository;
    private final SalleRepository salleRepository;

    public DataPusher(ProfesseurRepository professeurRepository, SalleRepository salleRepository) {
        this.professeurRepository = professeurRepository;
        this.salleRepository = salleRepository;
    }

    public Professeur saveProfesseur(@NonNull Professeur p) {
        return professeurRepository.save(p);
    }

    public Salle saveSalle(@NonNull Salle s) {
        return salleRepository.save(s);
    }

}
