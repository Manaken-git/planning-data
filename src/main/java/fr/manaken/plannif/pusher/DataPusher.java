package fr.manaken.plannif.pusher;

import fr.manaken.plannif.model.Classe;
import fr.manaken.plannif.model.Creneau;
import fr.manaken.plannif.model.Eleve;
import fr.manaken.plannif.model.Matiere;
import fr.manaken.plannif.model.Professeur;
import fr.manaken.plannif.model.Salle;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.repository.ClasseRepository;
import fr.manaken.plannif.repository.CreneauRepository;
import fr.manaken.plannif.repository.EleveRepository;
import fr.manaken.plannif.repository.MatiereRepository;
import fr.manaken.plannif.repository.ProfesseurRepository;
import fr.manaken.plannif.repository.SalleRepository;
import fr.manaken.plannif.repository.SeanceRepository;
import fr.manaken.plannif.repository.MatiereClasseConfigRepository;
import fr.manaken.plannif.model.MatiereClasseConfig;
import fr.manaken.plannif.model.Vacances;
import fr.manaken.plannif.repository.VacancesRepository;
import lombok.NonNull;

import org.springframework.stereotype.Service;

@Service
public class DataPusher {
    private final ProfesseurRepository professeurRepository;
    private final SalleRepository salleRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final EleveRepository eleveRepository;
    private final SeanceRepository seanceRepository;
    private final CreneauRepository creneauRepository;
    private final MatiereClasseConfigRepository matiereClasseConfigRepository;
    private final VacancesRepository vacancesRepository;

    public DataPusher(ProfesseurRepository professeurRepository, SalleRepository salleRepository,
            ClasseRepository classeRepository, MatiereRepository matiereRepository,
            EleveRepository eleveRepository, SeanceRepository seanceRepository,
            CreneauRepository creneauRepository,
            MatiereClasseConfigRepository matiereClasseConfigRepository,
            VacancesRepository vacancesRepository) {
        this.professeurRepository = professeurRepository;
        this.salleRepository = salleRepository;
        this.classeRepository = classeRepository;
        this.matiereRepository = matiereRepository;
        this.eleveRepository = eleveRepository;
        this.seanceRepository = seanceRepository;
        this.creneauRepository = creneauRepository;
        this.matiereClasseConfigRepository = matiereClasseConfigRepository;
        this.vacancesRepository = vacancesRepository;
    }

    // --- Professeur ---
    public Professeur saveProfesseur(@NonNull Professeur p) {
        return professeurRepository.save(p);
    }

    public void deleteProfesseur(@NonNull Integer id) {
        professeurRepository.deleteById(id);
    }

    // --- Salle ---
    public Salle saveSalle(@NonNull Salle s) {
        return salleRepository.save(s);
    }

    public void deleteSalle(@NonNull Integer id) {
        salleRepository.deleteById(id);
    }

    // --- Classe ---
    public Classe saveClasse(@NonNull Classe c) {
        return classeRepository.save(c);
    }

    public void deleteClasse(@NonNull Integer id) {
        classeRepository.deleteById(id);
    }

    // --- Matiere ---
    public Matiere saveMatiere(@NonNull Matiere m) {
        return matiereRepository.save(m);
    }

    public void deleteMatiere(@NonNull Integer id) {
        matiereRepository.deleteById(id);
    }

    // --- Eleve ---
    public Eleve saveEleve(@NonNull Eleve e) {
        return eleveRepository.save(e);
    }

    public void deleteEleve(@NonNull Integer id) {
        eleveRepository.deleteById(id);
    }

    // --- Seance ---
    public Seance saveSeance(@NonNull Seance s) {
        return seanceRepository.save(s);
    }

    public void deleteSeance(@NonNull Integer id) {
        seanceRepository.deleteById(id);
    }

    // --- Creneau ---
    public Creneau saveCreneau(@NonNull Creneau c) {
        return creneauRepository.save(c);
    }

    public void deleteCreneau(@NonNull Integer id) {
        creneauRepository.deleteById(id);
    }

    // --- MatiereClasseConfig ---
    public MatiereClasseConfig saveMatiereClasseConfig(@NonNull MatiereClasseConfig c) {
        return matiereClasseConfigRepository.save(c);
    }

    public void deleteMatiereClasseConfig(@NonNull Integer id) {
        matiereClasseConfigRepository.deleteById(id);
    }

    // --- Vacances ---
    public Vacances saveVacances(@NonNull Vacances v) {
        return vacancesRepository.save(v);
    }

    public void deleteVacances(@NonNull Integer id) {
        vacancesRepository.deleteById((long) id);
    }
}
