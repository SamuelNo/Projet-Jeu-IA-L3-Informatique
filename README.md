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

## Prérequis
- Java Development Kit (JDK) 17 ou version supérieure.
- Python 3.10+ pour la partie analyse.
- Bibliothèques Python : pandas, matplotlib et seaborn.

Installation des dépendances Python, si nécessaire :
```bash
python3 -m pip install pandas matplotlib seaborn
```

## Compilation et Exécution
Le projet ne fournit pas de Makefile dans l’état actuel du dépôt. La compilation peut être réalisée directement avec javac depuis la racine du projet :

```bash
mkdir -p bin
javac -d bin $(find src -path "src/test/MoteurCoupsEtatTest.java" -prune -o -name "*.java" -print)
```

Cette commande compile l’application et les utilitaires de simulation présents dans src/test/. Le fichier src/test/MoteurCoupsEtatTest.java utilise JUnit 5 et nécessite d’ajouter la dépendance correspondante au classpath si vous souhaitez le compiler ou l’exécuter.

Lancement du programme principal :

```bash
java -cp bin architecture.MenuPrincipal
```

### Utilisation du Makefile
Un `Makefile` a été ajouté pour simplifier les tâches courantes. Les cibles principales :

- `make build` : compile les sources (exclut le test JUnit explicite).
- `make run` : compile puis lance l'interface graphique (`architecture.MenuPrincipal`).
- `make analyse` : compile puis exécute `ia.analyse.GenerateurRapport` pour lancer la campagne automatisée.
- `make graphs` : exécute le script Python `data/generateur_graphique.py` (utilise `venv_projet/bin/python3` si présent, sinon `python3`).
- `make clean` : supprime `bin/` et efface `data/graphiques/`.

Exemples d'utilisation :

```bash
# Compiler
make build

# Lancer l'interface
make run

# Lancer la campagne d'analyse
make analyse

# Générer les graphiques (après analyse)
make graphs

# Nettoyer
make clean
```

## Mode Analyse
La campagne de tests automatisée est pilotée par la classe ia.analyse.GenerateurRapport. Elle exécute une série de matchs, met à jour les fichiers CSV dans data/csv/ et alimente les journaux dans data/txt/.

Depuis la racine du projet :

```bash
java -cp bin ia.analyse.GenerateurRapport
```

Une fois les CSV générés, les graphiques peuvent être produits avec le script Python situé dans data/ :

```bash
cd data
python3 generateur_graphique.py
```

Les figures sont enregistrées dans data/graphiques/.

## Auteurs
- Binôme : NOEL Samuel
- Binôme : BEN NAJEM Brahim
