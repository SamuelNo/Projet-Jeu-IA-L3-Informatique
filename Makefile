# Makefile pour le projet Jeu de plateau stratégique

ROOT_DIR := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
JAVAC := javac
JAVA := java
BIN_DIR := $(ROOT_DIR)bin
SRC_DIR := $(ROOT_DIR)src
DATA_DIR := $(ROOT_DIR)data
empty :=
space := $(empty) $(empty)

ifeq ($(OS),Windows_NT)
CP_SEP := ;
MKDIR_P := cmd /c if not exist "$(BIN_DIR)" mkdir "$(BIN_DIR)"
RM_BIN := cmd /c if exist "$(BIN_DIR)" rmdir /s /q "$(BIN_DIR)"
RM_GRAPHS := cmd /c if exist "$(DATA_DIR)/graphiques" rmdir /s /q "$(DATA_DIR)/graphiques"
else
CP_SEP := :
MKDIR_P := mkdir -p "$(BIN_DIR)"
RM_BIN := rm -rf "$(BIN_DIR)"
RM_GRAPHS := rm -rf "$(DATA_DIR)/graphiques"/* || true
endif

rwildcard = $(foreach d,$(wildcard $1*),$(call rwildcard,$d/,$2) $(filter $(subst *,%,$2),$d))

# Exclure le test JUnit explicite qui demande des dépendances externes
JAVA_SOURCES := $(filter-out $(SRC_DIR)/test/MoteurCoupsEtatTest.java,$(call rwildcard,$(SRC_DIR)/,*.java))

CLASSPATH_ENTRIES := $(BIN_DIR)
JAVA_CLASSPATH := $(subst $(space),$(CP_SEP),$(CLASSPATH_ENTRIES))

ifeq ($(OS),Windows_NT)
PYTHON := $(firstword $(wildcard $(ROOT_DIR)venv_projet/Scripts/python.exe) $(wildcard $(ROOT_DIR)venv_projet/Scripts/python3.exe) $(wildcard $(ROOT_DIR)venv_projet/Scripts/python) python3 python)
else
PYTHON := $(shell if [ -x "$(ROOT_DIR)venv_projet/bin/python3" ] ; then echo "$(ROOT_DIR)venv_projet/bin/python3" ; elif [ -x "$(ROOT_DIR)venv_projet/bin/python" ] ; then echo "$(ROOT_DIR)venv_projet/bin/python" ; else echo python3 ; fi)
endif

.PHONY: all build run analyse graphs clean

all: build

build:
	@echo "-> Compilation des sources Java..."
	@$(MKDIR_P)
	$(JAVAC) -d "$(BIN_DIR)" $(JAVA_SOURCES)
	@echo "-> Compilation terminée. Classes dans $(BIN_DIR)"

run: build
	@echo "-> Lancement de l'application (interface graphique)..."
	$(JAVA) -cp "$(JAVA_CLASSPATH)" architecture.MenuPrincipal

analyse: build
	@echo "-> Lancement de la campagne d'analyse (GenerateurRapport)..."
	$(JAVA) -cp "$(JAVA_CLASSPATH)" ia.analyse.GenerateurRapport

graphs:
	@echo "-> Génération des graphiques (script Python)..."
	@cd "$(DATA_DIR)" && "$(PYTHON)" generateur_graphique.py

clean:
	@echo "-> Nettoyage : suppression du répertoire $(BIN_DIR)"
	@$(RM_BIN)
	@echo "-> Nettoyage des graphiques (ne supprime pas les CSV)"
	@$(RM_GRAPHS)
