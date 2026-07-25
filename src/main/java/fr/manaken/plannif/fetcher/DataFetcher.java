package fr.manaken.plannif.fetcher;

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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataFetcher {

    private final ProfesseurRepository professeurRepository;
    private final SeanceRepository seanceRepository;
    private final EleveRepository eleveRepository;
    private final SalleRepository salleRepository;
    private final ClasseRepository classeRepository;
    private final MatiereRepository matiereRepository;
    private final CreneauRepository creneauRepository;
    private final MatiereClasseConfigRepository matiereClasseConfigRepository;

    public DataFetcher(ProfesseurRepository professeurRepository, SeanceRepository seanceRepository,
            EleveRepository eleveRepository, SalleRepository salleRepository,
            ClasseRepository classeRepository, MatiereRepository matiereRepository,
            CreneauRepository creneauRepository,
            MatiereClasseConfigRepository matiereClasseConfigRepository) {
        this.professeurRepository = professeurRepository;
        this.seanceRepository = seanceRepository;
        this.eleveRepository = eleveRepository;
        this.salleRepository = salleRepository;
        this.classeRepository = classeRepository;
        this.matiereRepository = matiereRepository;
        this.creneauRepository = creneauRepository;
        this.matiereClasseConfigRepository = matiereClasseConfigRepository;
    }

    // --- Professeur ---
    public List<Professeur> getProfesseurs() {
        return professeurRepository.findAll();
    }

    @SuppressWarnings("null")
    public Professeur getProfesseur(Integer id) {
        return professeurRepository.getReferenceById(id);
    }

    // --- Salle ---
    public List<Salle> getSalles() {
        return salleRepository.findAll();
    }

    @SuppressWarnings("null")
    public Salle getSalle(Integer id) {
        return salleRepository.getReferenceById(id);
    }

    // --- Classe ---
    public List<Classe> getClasses() {
        return classeRepository.findAll();
    }

    @SuppressWarnings("null")
    public Classe getClasse(Integer id) {
        return classeRepository.getReferenceById(id);
    }

    // --- Matiere ---
    public List<Matiere> getMatieres() {
        return matiereRepository.findAll();
    }

    @SuppressWarnings("null")
    public Matiere getMatiere(Integer id) {
        return matiereRepository.getReferenceById(id);
    }

    // --- Eleve ---
    public List<Eleve> getElevesByClasse(Long idClasse) {
        return eleveRepository.findByClasseId(idClasse);
    }

    public List<Eleve> getAllEleves() {
        return eleveRepository.findAll();
    }

    @SuppressWarnings("null")
    public Eleve getEleve(Integer id) {
        return eleveRepository.getReferenceById(id);
    }

    // --- Seance ---
    public List<Seance> getSeances() {
        return seanceRepository.findAll();
    }

    @SuppressWarnings("null")
    public Seance getSeance(Integer id) {
        return seanceRepository.getReferenceById(id);
    }

    // --- Creneau ---
    public List<Creneau> getCreneaux() {
        return creneauRepository.findAll();
    }

    @SuppressWarnings("null")
    public Creneau getCreneau(Integer id) {
        return creneauRepository.getReferenceById(id);
    }

    // --- MatiereClasseConfig ---
    public List<MatiereClasseConfig> getMatiereClasseConfigs() {
        return matiereClasseConfigRepository.findAll();
    }

    @SuppressWarnings("null")
    public MatiereClasseConfig getMatiereClasseConfig(Integer id) {
        return matiereClasseConfigRepository.getReferenceById(id);
    }

    // --- Existence checks for CSV import ---
    public boolean existsProfesseur(Integer id) {
        return professeurRepository.existsById(id);
    }

    public boolean existsSalle(Integer id) {
        return salleRepository.existsById(id);
    }

    public boolean existsClasse(Integer id) {
        return classeRepository.existsById(id);
    }

    public boolean existsMatiere(Integer id) {
        return matiereRepository.existsById(id);
    }

    public boolean existsEleve(Integer id) {
        return eleveRepository.existsById(id);
    }

    public boolean existsSeance(Integer id) {
        return seanceRepository.existsById(id);
    }

    public boolean existsCreneau(Integer id) {
        return creneauRepository.existsById(id);
    }

    public boolean existsMatiereClasseConfig(Integer id) {
        return matiereClasseConfigRepository.existsById(id);
    }
}

