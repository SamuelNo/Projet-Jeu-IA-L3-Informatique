# Makefile pour le projet Jeu de plateau stratégique

JAVAC := javac
JAVA := java
BIN := bin
SRC_DIR := src

# Exclure le test JUnit explicite qui demande des dépendances externes
JAVA_SOURCES := $(shell find $(SRC_DIR) -path "src/test/MoteurCoupsEtatTest.java" -prune -o -name "*.java" -print)

PYTHON := $(shell if [ -x venv_projet/bin/python3 ] ; then echo venv_projet/bin/python3 ; elif [ -x venv_projet/bin/python ] ; then echo venv_projet/bin/python ; else echo python3 ; fi)

.PHONY: all build run analyse graphs clean

all: build

build:
	@echo "-> Compilation des sources Java..."
	@mkdir -p $(BIN)
	$(JAVAC) -d $(BIN) $(JAVA_SOURCES)
	@echo "-> Compilation terminée. Classes dans $(BIN)/"

run: build
	@echo "-> Lancement de l'application (interface graphique)..."
	$(JAVA) -cp $(BIN) architecture.MenuPrincipal

analyse: build
	@echo "-> Lancement de la campagne d'analyse (GenerateurRapport)..."
	$(JAVA) -cp $(BIN) ia.analyse.GenerateurRapport

graphs:
	@echo "-> Génération des graphiques (script Python)..."
	@cd data && $(PYTHON) generateur_graphique.py

clean:
	@echo "-> Nettoyage : suppression du répertoire $(BIN)"
	@rm -rf $(BIN)
	@echo "-> Nettoyage des graphiques (ne supprime pas les CSV)"
	@rm -rf data/graphiques/* || true
