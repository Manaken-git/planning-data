# 🚀 Plannif Lycée - Pack Nomade & Déploiement Autonome

Bienvenue dans le **Pack Nomade de Plannif Lycée**, la solution conçue pour fonctionner sur n'importe quel ordinateur Windows (PC personnel, poste d'administration de lycée, clé USB) **sans aucun droit administrateur**, sans Docker et sans installation de base de données externe.

---

## 📁 Contenu du Pack

- `plannif-app.jar` : Application tout-en-un réunissant :
  - L'interface Web React / TypeScript précompilée
  - Les APIs REST de gestion scolaire
  - Le moteur de résolution d'emplois du temps Timefold Solver
  - La base de données locale autonome H2 (fichier embarqué)
  - Le moteur d'export PDF par lots
  - Le moteur d'importation Pronote intelligent
- `Lancer-Planning.bat` : Lance le serveur en arrière-plan et ouvre votre navigateur sur `http://localhost:8080/`.
- `Arreter-Planning.bat` : Arrête proprement le serveur local.
- `Sauvegarder-Donnees.bat` : Sauvegarde instantanée de vos données dans le sous-dossier `backups/`.
- `data/` : Dossier contenant le fichier de base de données locale (`planning.mv.db`).

---

## ⚡ Utilisation Rapide

1. **Double-cliquez sur `Lancer-Planning.bat`** :
   - Le serveur démarre en quelques secondes.
   - Votre navigateur par défaut s'ouvre automatiquement sur `http://localhost:8080/`.
2. **Utilisez l'application normalement** :
   - Saisie et consultation des classes, professeurs, salles, matières, créneaux.
   - Lancement de la génération automatisée d'emplois du temps.
3. **Pour quitter** :
   - Double-cliquez sur `Arreter-Planning.bat`.

---

## 📄 Export PDF (Par Classe, Par Prof, Livret Global, Archive ZIP)

Plusieurs formats de téléchargement direct sont disponibles :

- **PDF d'une classe** :  
  `GET http://localhost:8080/plannings/{id}/pdf/classe?nom={NomClasse}`
- **Livret PDF complet de toutes les classes (1 page par classe, format paysage A4)** :  
  `GET http://localhost:8080/plannings/{id}/pdf/classes`
- **PDF d'un professeur** :  
  `GET http://localhost:8080/plannings/{id}/pdf/prof?nom={NomProfesseur}`
- **Livret PDF complet de tous les professeurs** :  
  `GET http://localhost:8080/plannings/{id}/pdf/profs`
- **Archive ZIP complète ("Pack d'impression et d'envoi")** :  
  `GET http://localhost:8080/plannings/{id}/pdf/zip`  
  *Contient tous les PDF individuels de chaque classe et chaque prof dans des sous-dossiers, plus les livrets complets.*

---

## 📥 Importation Pronote

Vous pouvez injecter directement vos données exportées depuis le logiciel **Pronote** (formats texte/CSV avec séparateur `;`, `,` ou tabulation) :

- **Import universel avec auto-détection** :  
  `POST http://localhost:8080/api/pronote/import` (en envoyant le fichier CSV dans le champ multipart `file`).
- **Imports ciblés** :
  - `POST http://localhost:8080/api/pronote/import/profs` : Enseignants avec rattachement automatique aux matières.
  - `POST http://localhost:8080/api/pronote/import/classes` : Classes et effectifs.
  - `POST http://localhost:8080/api/pronote/import/salles` : Salles, capacités, bâtiments.
  - `POST http://localhost:8080/api/pronote/import/matieres` : Matières et disciplines.
  - `POST http://localhost:8080/api/pronote/import/presences` : Calendrier d'alternance et périodes de cours au lycée.

---

## 🔒 Confidentialité & RGPD

Toutes les données (noms d'élèves, professeurs, plannings) sont stockées localement sur votre disque ou clé USB dans le dossier `data/`. **Aucune donnée ne transite par Internet ni par un serveur tiers.**
