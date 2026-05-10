# Makefile pour le projet Jeu de plateau stratégique

ROOT_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
JAVAC := javac
JAVA := java
BIN_DIR := $(ROOT_DIR)bin
SRC_DIR := $(ROOT_DIR)src
DATA_DIR := $(ROOT_DIR)data

# Exclure le test JUnit explicite qui demande des dépendances externes
JAVA_SOURCES := $(shell find "$(SRC_DIR)" -path "$(SRC_DIR)/test/MoteurCoupsEtatTest.java" -prune -o -name "*.java" -print)

PYTHON := $(shell if [ -x "$(ROOT_DIR)venv_projet/bin/python3" ] ; then echo "$(ROOT_DIR)venv_projet/bin/python3" ; elif [ -x "$(ROOT_DIR)venv_projet/bin/python" ] ; then echo "$(ROOT_DIR)venv_projet/bin/python" ; else echo python3 ; fi)

.PHONY: all build run analyse graphs clean

all: build

build:
	@echo "-> Compilation des sources Java..."
	@mkdir -p "$(BIN_DIR)"
	$(JAVAC) -d "$(BIN_DIR)" $(JAVA_SOURCES)
	@echo "-> Compilation terminée. Classes dans $(BIN_DIR)"

run: build
	@echo "-> Lancement de l'application (interface graphique)..."
	$(JAVA) -cp "$(BIN_DIR)" architecture.MenuPrincipal

analyse: build
	@echo "-> Lancement de la campagne d'analyse (GenerateurRapport)..."
	$(JAVA) -cp "$(BIN_DIR)" ia.analyse.GenerateurRapport

graphs:
	@echo "-> Génération des graphiques (script Python)..."
	@cd "$(DATA_DIR)" && $(PYTHON) generateur_graphique.py

clean:
	@echo "-> Nettoyage : suppression du répertoire $(BIN_DIR)"
	@rm -rf "$(BIN_DIR)"
	@echo "-> Nettoyage des graphiques (ne supprime pas les CSV)"
	@rm -rf "$(DATA_DIR)/graphiques"/* || true
