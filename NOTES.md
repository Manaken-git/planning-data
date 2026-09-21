# 📋 Documentation & Notes Techniques – Projet `plannif-data`

Ce document synthétise l'architecture, le modèle de données, les règles métier et les API du projet **planning-data** afin d'éviter d'avoir à reparcourir tous les fichiers source lors des futures sessions d'assistance.

---

## 1. ⚙️ Informations Générales & Environnement

- **Nom du projet** : `plannif-data`
- **Stack technique** :
  - **Java** : 25
  - **Spring Boot** : 4.0.5 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-liquibase`)
  - **Base de données** : MariaDB (client 3.5.6)
  - **Mappers** : MapStruct 1.6.3 + Lombok 1.18.42 + `lombok-mapstruct-binding` 0.2.0
  - **Documentation API** : SpringDoc OpenAPI 3.0.3 (Swagger UI `/planning-data/swagger-ui.html`, `/planning-data/v3/api-docs`)
  - **Gestion CSV** : OpenCSV 5.9
- **Configuration Serveur (`application.yml`)** :
  - Port : `8081`
  - Context Path : `/planning-data`
  - URL Base : `http://localhost:8081/planning-data`
  - JDBC URL : `jdbc:mariadb://localhost:3306/planning_ecole_full` (user: `root`, password: `***`)
  - Liquibase changelog : `classpath:db/changelog/db.changelog-master.yaml`
  - JPA Hibernate : `ddl-auto: none`

---

## 2. 🏛️ Pattern d'Architecture

Le projet suit une variante propre de séparation CQRS / Data Layer :

```
[Controller (REST / CSV)]
          ↓
     [Service] (Règles métier, validations, transactions @Transactional)
       ↙      ↘
[DataFetcher]  [DataPusher]  +  [Mapper (MapStruct)]
       ↘      ↙                       ↕
   [Repositories]               [DTO (Records)] <-> [Entities (JPA)]
          ↓
     [Database]
```

1. **`DataFetcher`** (`fr.manaken.plannif.fetcher.DataFetcher`) :
   - Centralise toutes les opérations de lecture (`findAll`, `getReferenceById`, `findBy...`) et de vérification d'existence (`existsById`) pour les entités.
2. **`DataPusher`** (`fr.manaken.plannif.pusher.DataPusher`) :
   - Centralise toutes les opérations d'écriture et de suppression (`save`, `deleteById`).
3. **`Mappers`** (`fr.manaken.plannif.mapper.*`) :
   - Interfaces MapStruct injectées via Spring (`componentModel = "spring"`).
   - Fournissent des méthodes `toDto(Entity)` et `mergeWDTO(@MappingTarget Entity, DTO)` avec `NullValuePropertyMappingStrategy.IGNORE`.
4. **`DTOs`** (`fr.manaken.plannif.dto.*`) :
   - Tous implémentés sous forme de **Java Records** immutables.
5. **`Services`** (`fr.manaken.plannif.service.*`) :
   - Contiennent la logique de validation métier, l'import/export CSV (UTF-8, parsing tolérant des dates/en-têtes avec BOM), et la gestion transactionnelle (`@Transactional`).

---

## 3. 🗃️ Modèle de Données & Entités JPA

| Entité JPA | Table BDD | Attributs Clés & Types | Relations & Détails |
|---|---|---|---|
| **`Classe`** | `classe` | `id` (Long), `nom` (String) | - `presences` : `@OneToMany` `ClassePresence` (cascade `ALL`, `orphanRemoval`)<br>- `eleves` : `@OneToMany` `Eleve`<br>- `seances` : `@OneToMany` `Seance`<br>- Méthode `needsVieDeClasse(presence, allVacances)` |
| **`ClassePresence`** | `t_classe_presence` | `id` (Long), `dateDebut` (LocalDate), `dateFin` (LocalDate) | - `classe` : `@ManyToOne` `Classe` (nullable=false)<br>- Validé pour des durées de 1, 2 ou 3 sem (5-7j, 12-14j, 19-21j) |
| **`Eleve`** | `eleve` | `id` (Long), `nom` (String), `prenom` (String) | - `classe` : `@ManyToOne` `Classe` (`@JoinColumn(name="classe_id")`) |
| **`Professeur`** | `professeur` | `id` (Long), `nom` (String), `prenom` (String), `email` (String), `nb_heures` (BigDecimal), `maxHeuresParJour` (BigDecimal), `maxHeuresParSemaine` (BigDecimal), `maxHeuresParSeance` (BigDecimal) | - `plageHorairePreferee` : `@ManyToOne` `PlageHoraire`<br>- `daysOff` : `@OneToMany` `ProfesseurDayOff` (FetchType.EAGER)<br>- `matieres` : `@ManyToMany` via table `tj_professeur_matiere`<br>- `seances` : `@OneToMany` `Seance` |
| **`ProfesseurDayOff`** | `t_professeur_dayoff` | `id` (Long), `dayOfWeek` (Integer: 0=Lun, 1=Mar, ..., 4=Ven) | - `professeur` : `@ManyToOne` `Professeur` |
| **`Matiere`** | `matiere` | `id` (Long), `nom` (String) | - `seances` : `@OneToMany` `Seance` |
| **`Salle`** | `salle` | `id` (Long), `code` (String, unique), `capacite` (Integer), `type` (String) | - `seances` : `@OneToMany` `Seance` |
| **`DistanceSalle`** | `tj_distance_salle` | `id` (Long), `distance` (Long) | - `salle1` : `@ManyToOne` `Salle`<br>- `salle2` : `@ManyToOne` `Salle` |
| **`Equipement`** | `t_equipement` | `id` (Long), `libelle` (String) | Référentiel des équipements |
| **`EquipementSalle`**| `tj_equipements_salle`| `id` (Long) | - `salle` : `@ManyToOne` `Salle`<br>- `equipement` : `@ManyToOne` `Equipement` |
| **`PlageHoraire`** | `t_plage_horaire` | `id` (Long), `libelle` (String) | Plage horaire préférée (ex. Matin, Après-midi) |
| **`Creneau`** | `t_creneau` | `id` (Long), `debut` (LocalDateTime), `fin` (LocalDateTime), `semaineType` (SemaineType), `typeClasse` (String) | Enum `SemaineType` : `SEMAINE_1`, `SEMAINE_2`, `SEMAINE_3` |
| **`MatiereClasseConfig`**| `t_matiere_classe_config` | `id` (Long), `dateDebut` (LocalDate), `dateFin` (LocalDate), `volumeHorairePeriode` (Long) | - `classe` : `@ManyToOne` `Classe`<br>- `matiere` : `@ManyToOne` `Matiere`<br>- Doit englober les périodes de présence associées |
| **`Seance`** | `seance` | `id` (Long), `debut` (LocalDateTime), `fin` (LocalDateTime), `type` (TypeSeance) | Enum `TypeSeance` : `COURS`, `TP`, `EXAMEN`, `VIE_DE_CLASSE`<br>- `professeur` (ManyToOne)<br>- `classe` (ManyToOne)<br>- `matiere` (ManyToOne)<br>- `salle` (ManyToOne)<br>- `creneau` (ManyToOne, cascade PERSIST/MERGE)<br>- `planning` (ManyToOne) |
| **`Planning`** | `t_planning` | `id` (Long), `nom` (String), `dateCreation` (LocalDateTime) | - `seances` : `@OneToMany` `Seance` (cascade PERSIST/MERGE) |
| **`Vacances`** | `t_vacances` | `id` (Long), `nom` (String), `dateDebut` (LocalDate), `dateFin` (LocalDate) | Périodes de congés scolaires |

---

## 4. 📦 DTOs (Records) & Correspondances

- **`ClasseDTO`** : `(Long id, String nom, Set<SeanceDTO> seances, Set<EleveDTO> eleves, List<ClassePresenceDTO> presences)`
- **`ClassePresenceDTO`** : `(Long id, Long classeId, String classeNom, LocalDate dateDebut, LocalDate dateFin)`
- **`EleveDTO`** : `(Long id, String nom, String prenom, Long classeId)`
- **`ProfesseurDTO`** : `(Long id, String nom, String prenom, String email, BigDecimal nb_heures, BigDecimal maxHeuresParJour, BigDecimal maxHeuresParSemaine, BigDecimal maxHeuresParSeance, PlageHoraireDTO plageHorairePreferee, Set<MatiereDTO> matieres, Set<SeanceDTO> seances, List<ProfesseurDayOffDTO> daysOff)`
- **`ProfesseurDayOffDTO`** : `(Long id, ProfesseurDTO professeur, Integer dayOfWeek)`
- **`MatiereDTO`** : `(Long id, String nom, Set<SeanceDTO> seances)`
- **`SalleDTO`** : `(Long id, String code, Integer capacite, String type, Set<SeanceDTO> seances)`
- **`DistanceSalleDTO`** : `(Long id, SalleDTO salle1, SalleDTO salle2, Long distance)`
- **`EquipementDTO`** : `(Long id, String libelle)`
- **`EquipementSalleDTO`** : `(Long id, SalleDTO salle, EquipementDTO equipement)`
- **`PlageHoraireDTO`** : `(Long id, String libelle)`
- **`CreneauDTO`** : `(Long id, LocalDateTime debut, LocalDateTime fin, SemaineType semaineType, String typeClasse)`
- **`MatiereClasseConfigDTO`** : `(Long id, Long classeId, String classeNom, Long matiereId, String matiereNom, LocalDate dateDebut, LocalDate dateFin, Long volumeHorairePeriode)`
- **`SeanceDTO`** : `(Long id, LocalDateTime debut, LocalDateTime fin, String professeurNomComplet, String classeNom, String matiereNom, String salleCode)`
- **`SeanceSaveDTO`** : `(Long id, Long creneauId, Long professeurId, Long classeId, Long matiereId, Long salleId, String type)`
- **`PlanningDTO`** : `(Long id, String nom, LocalDateTime dateCreation, List<SeanceDTO> seances)`
- **`PlanningSaveDTO`** : `(Long id, String nom, LocalDateTime dateCreation, List<CreneauDTO> creneaux, List<SeanceSaveDTO> seances)`
- **`VacancesDTO`** : `(Long id, String nom, LocalDate dateDebut, LocalDate dateFin)`

---

## 5. 🌐 Endpoints REST (Base URL: `/planning-data`)

### 5.1 Classes (`/classes`)
- `GET /classes/list` : Liste toutes les classes avec présences et séances.
- `POST /classes/create` : Crée une classe (`ClasseDTO`).
- `PUT /classes/update` : Modifie une classe.
- `DELETE /classes/delete/{id}` : Supprime la classe (`204 No Content`).
- `POST /classes/import` : Import CSV (`file`). Format colonnes : `id`, `nom`, `presences` (ex: `01/09/2025:05/09/2025;15/09/2025:19/09/2025`).
- `GET /classes/export` : Export CSV (`classes.csv`).

### 5.2 Élèves (`/eleves`)
- `GET /eleves/list` : Liste tous les élèves avec `classeId`.
- `GET /eleves/list/{idClasse}` : Liste les élèves d'une classe.
- `POST /eleves/create?classeId={id}` : Crée un élève (`EleveDTO`). `classeId` peut être passé en query param ou dans le body.
- `PUT /eleves/update?classeId={id}` : Modifie un élève (`EleveDTO`).
- `DELETE /eleves/delete/{id}` : Supprime un élève (`204 No Content`).
- `POST /eleves/import` : Import CSV (`id`, `nom`, `prenom`, `classeId`).
- `GET /eleves/export` : Export CSV (`eleves.csv`).

### 5.3 Professeurs (`/professeurs`)
- `GET /professeurs/list` : Liste des professeurs.
- `POST /professeurs/create` / `PUT /professeurs/update` : Sauvegarde `ProfesseurDTO`.
- `DELETE /professeurs/delete/{id}` : Suppression.
- `POST /professeurs/import` : Import CSV (`id`, `nom`, `prenom`, `email`, `nb_heures`, `maxheuresparjour`, `maxheuresparsemaine`, `maxheuresparseance`).
- `GET /professeurs/export` : Export CSV (`professeurs.csv`).

### 5.4 Matières (`/matieres`)
- `GET /matieres/list`, `POST /matieres/create`, `PUT /matieres/update`, `DELETE /matieres/delete/{id}`.
- `POST /matieres/import` / `GET /matieres/export` (`id`, `nom`).

### 5.5 Salles (`/salles`)
- `GET /salles/list`, `POST /salles/create`, `PUT /salles/update`, `DELETE /salles/delete/{id}`.
- `POST /salles/import` / `GET /salles/export` (`id`, `code`, `capacite`, `type`).

### 5.6 Séances hors planning (`/seances`)
- `GET /seances/list` : Séances où `planning_id IS NULL`.
- `POST /seances/create` / `PUT /seances/update` (params optionnels: `professeurId`, `classeId`, `matiereId`, `salleId`).
- `DELETE /seances/delete/{id}` : Suppression séance.
- `POST /seances/import` / `GET /seances/export` (`id`, `professeurId`, `classeId`, `matiereId`, `salleId`, `debut`, `fin`, `creneauId`, `type`).

### 5.7 Créneaux (`/creneaux`)
- `GET /creneaux/list`, `POST /creneaux/create`, `PUT /creneaux/update`, `DELETE /creneaux/delete/{id}`.
- `POST /creneaux/import` / `GET /creneaux/export` (`id`, `debut`, `fin`, `semaine_type`, `type_classe`).

### 5.8 Configurations Matière-Classe (`/configs`)
- `GET /configs/list`, `POST /configs/create`, `PUT /configs/update`, `DELETE /configs/delete/{id}`.
- `POST /configs/import` / `GET /configs/export` (`id`, `classeId`, `matiereId`, `dateDebut`, `dateFin`, `volumeHorairePeriode`).

### 5.9 Plannings (`/plannings`)
- `GET /plannings/list` : Liste des plannings sauvegardés.
- `GET /plannings/{id}` : Détail d'un planning avec ses séances.
- `POST /plannings/save` : Sauvegarde complète d'un planning (`PlanningSaveDTO`). Déduplique/persiste les créneaux et associe les séances au planning.
- `DELETE /plannings/delete/{id}` : Supprime un planning et cascade sur ses séances.

### 5.10 Vacances (`/vacances`)
- `GET /vacances/list`, `POST /vacances/create`, `PUT /vacances/update`, `DELETE /vacances/delete/{id}`.

---

## 6. 📏 Règles Métier Spécifiques

1. **Validation des périodes de présence (`ClasseService.validatePresences`)** :
   - `dateFin >= dateDebut` requis.
   - La durée totale de chaque présence doit impérativement être de **1, 2 ou 3 semaines** :
     - 1 semaine : 5 à 7 jours inclus
     - 2 semaines : 12 à 14 jours inclus
     - 3 semaines : 19 à 21 jours inclus
2. **Validation des dates de configuration (`MatiereClasseConfigService.validateConfigDatesAgainstPresences`)** :
   - La période `[dateDebut, dateFin]` d'une configuration doit obligatoirement englober au moins une période de présence de la classe.
   - Si une présence chevauche la période de la configuration, la présence doit être **entièrement contenue** dans la période de la configuration (pas de débordement partiel).
3. **Calcul de "Vie de classe" (`Classe.needsVieDeClasse`)** :
   - Vérifie si une séance de type `VIE_DE_CLASSE` est nécessaire pour une période de présence donnée.
   - Prend en compte le dernier vendredi de la présence et la date de reprise après les dernières vacances.
4. **Import CSV robuste** :
   - Suppression du caractère invisible UTF-8 BOM (`\uFEFF`).
   - Tolérance sur les noms d'en-tête (minuscules, avec ou sans underscores, accents).
   - Formats de dates supportés : `d/M/yyyy`, `yyyy-MM-dd`, `yyyy-MM-dd'T'HH:mm:ss`.
   - Contrôle d'existence des IDs et clés étrangères avant insertion avec messages d'erreur clairs.

---

## 7. 🧪 Commandes Utiles de Développement

- **Compilation & Tests** :
  ```powershell
  mvn clean test
  ```
- **Lancement de l'application Spring Boot** :
  ```powershell
  mvn spring-boot:run
  ```
- **Documentation Swagger** (quand l'application tourne) :
  ```
  http://localhost:8081/planning-data/swagger-ui.html
  http://localhost:8081/planning-data/v3/api-docs
  ```
