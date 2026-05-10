# Jeu de plateau stratégique - Intelligence Artificielle

# Projet IA - GLADIUS
**Université :** Université Paris Cité
**Professeur :** Elise BONZON
**Binôme :** Samuel NOEL & Brahim BEN NAJEM 

## Présentation du Projet
Ce projet universitaire implémente un jeu de plateau stratégique, déterministe et à connaissances parfaites, développé en Java. Le système propose une interface de jeu complète ainsi que trois niveaux d’intelligence artificielle, permettant d’opposer un joueur humain à une IA Facile, Moyenne ou Difficile.

Le projet inclut également une chaîne d’analyse expérimentale pour comparer les heuristiques, mesurer les performances et produire des graphiques à partir des campagnes automatisées.

## Structure du Projet
```text
.
├── src/
│   ├── architecture/        # Interface, fenêtre principale et lancement du jeu
│   ├── attaques/            # Logique des attaques et interactions de combat
│   ├── entite/              # Entités du jeu : personnages, armes, positions
│   ├── exception/           # Exceptions métier
│   ├── ia/                  # IA, moteur de coups et analyse
│   │   └── analyse/         # Génération de rapports et campagnes de tests
│   └── test/                # Programmes de test et de simulation
├── bin/                     # Classes compilées Java
├── data/
│   ├── csv/                 # Données d’analyse exportées en CSV
│   ├── graphiques/          # Graphiques générés par Python
│   ├── txt/                 # Journaux et résultats de tournoi
│   └── generateur_graphique.py
├── venv_projet/             # Environnement Python local pour l’analyse
└── README.md
```

## Installation et Compatibilité
Le projet se lance depuis la racine du dépôt avec `make`. La logique de compilation et d’exécution a été rendue portable pour fonctionner sur macOS, Linux et Windows, à condition d’utiliser un environnement compatible avec GNU Make.

### Prérequis communs
- Java Development Kit (JDK) 17 ou version supérieure.
- Python 3 pour la partie analyse et la génération des graphiques.
- `make` installé sur la machine.
- Les bibliothèques Python `pandas`, `matplotlib` et `seaborn` si elles ne sont pas déjà présentes.

### macOS
- Support natif.
- Les commandes `make build`, `make run`, `make analyse`, `make graphs` et `make clean` fonctionnent directement depuis un terminal standard.

### Linux
- Support natif.
- Les mêmes commandes sont identiques à celles de macOS, sans adaptation particulière, avec Java 17+ et Python 3 installés.

### Windows
- Le plus fiable est d’utiliser Git Bash ou WSL2 afin de conserver le flux de travail basé sur `make`.
- PowerShell peut également fonctionner si GNU Make est installé, mais Git Bash ou WSL2 évite les différences de chemins et de commandes shell.
- Les mêmes commandes sont utilisées qu’ailleurs : `make build`, `make run`, `make analyse`, `make graphs` et `make clean`.

### Installation des dépendances Python
```bash
python3 -m pip install pandas matplotlib seaborn
```

### Guide rapide des commandes
Toutes les commandes ci-dessous sont identiques sur macOS, Linux et Windows, tant que l’environnement permet d’exécuter `make`.

```bash
make build
make run
make analyse
make graphs
make clean
```

### Justification technique
La portabilité a été assurée en s’appuyant sur des chemins relatifs dans le projet, une détection automatique du système d’exploitation dans le `Makefile`, et une gestion explicite des séparateurs de classpath selon la plateforme. L’encodage UTF-8 a été conservé pour garantir l’affichage correct des accents et des noms français sur macOS, Linux et Windows, tandis que les commandes de nettoyage et la recherche des sources ont été réécrites pour éviter les dépendances à des outils propres à un seul système.

## Mode Analyse
La campagne de tests automatisée est pilotée par la classe `ia.analyse.GenerateurRapport`. Elle exécute une série de matchs, met à jour les fichiers CSV dans `data/csv/` et alimente les journaux dans `data/txt/`.

Depuis la racine du projet :

```bash
make analyse
```

Une fois les CSV générés, les graphiques peuvent être produits avec :

```bash
make graphs
```

Les figures sont enregistrées dans `data/graphiques/`.

## Auteurs
- Binôme : NOEL Samuel
- Binôme : BEN NAJEM Brahim
