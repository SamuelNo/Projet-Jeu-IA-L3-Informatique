import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

# Configuration visuelle
sns.set_theme(style="whitegrid")

def graph_a_explosion_combinatoire():
    if not os.path.exists('csv/performances_profondeur.csv'): return
    df = pd.read_csv('csv/performances_profondeur.csv')
    plt.figure(figsize=(10, 6))
    sns.lineplot(data=df, x='Profondeur', y='TempsMoyenCoupMS', hue='Heuristique', marker='o')
    plt.yscale('log')
    plt.title('Graphique A : Temps de reflexion vs Profondeur (Echelle Log)')
    plt.ylabel('Temps moyen par coup (ms)')
    plt.xticks([1, 2, 3, 4])
    plt.savefig('graphiques/Graph_A_Explosion_Temps.png')
    plt.close()
    print("✅ Graphique A genere.")

def graph_b_noeuds_visites():
    if not os.path.exists('csv/performances_profondeur.csv'): return
    df = pd.read_csv('csv/performances_profondeur.csv')
    plt.figure(figsize=(10, 6))
    sns.lineplot(data=df, x='Profondeur', y='NoeudsVisitesMoyen', hue='Heuristique', marker='s')
    plt.yscale('log')
    plt.title('Graphique B : Nombre de noeuds visites vs Profondeur (Echelle Log)')
    plt.ylabel('Noeuds visites (moyenne)')
    plt.xticks([1, 2, 3, 4])
    plt.savefig('graphiques/Graph_B_Noeuds_Visites.png')
    plt.close()
    print("✅ Graphique B genere.")

def graph_c_classement_ia():
    if not os.path.exists('csv/asymetrique_swaps.csv'): return
    df = pd.read_csv('csv/asymetrique_swaps.csv')
    df_classique = df[~df['Scenario_Type'].str.contains('P2|P4')]
    records = []
    for _, row in df_classique.iterrows():
        gagnant = row['IA_J1'] if row['Vainqueur'] == 'J1' else (row['IA_J2'] if row['Vainqueur'] == 'J2' else 'NUL')
        records.append({'Scenario': row['Scenario_Type'], 'Gagnant': gagnant})
    df_wins = pd.DataFrame(records)
    plt.figure(figsize=(12, 6))
    sns.countplot(data=df_wins, x='Scenario', hue='Gagnant')
    plt.title('Graphique C : Force des Heuristiques (Profondeur Standard)')
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()
    plt.savefig('graphiques/Graph_C_Classement_IA.png')
    plt.close()
    print("✅ Graphique C genere.")
def graph_f_qualite_vs_quantite():
    """Analyse Heuristique vs Profondeur (Expérience 4) - Séparé par Swap/Constant"""
    if os.path.exists('csv/impact_profondeur_croise.csv'):
        df = pd.read_csv('csv/impact_profondeur_croise.csv')
        
        # 1. SWAP
        scenarios_swap = ['DIFF_P2_VS_MOY_P4_SWAP', 'DIFF_P4_VS_MOY_P2_SWAP']
        df_sub_swap = df[df['Scenario_Type'].isin(scenarios_swap)].copy()
        if not df_sub_swap.empty:
            df_sub_swap['Winner_Name'] = df_sub_swap.apply(lambda r: r['IA_J1'] if r['Vainqueur'] == 'J1' else (r['IA_J2'] if r['Vainqueur'] == 'J2' else 'NUL'), axis=1)
            plt.figure(figsize=(12, 6))
            sns.countplot(data=df_sub_swap, x='Scenario_Type', hue='Winner_Name')
            plt.title('Graphique F1 : Duel Cerveau vs Muscles (Mode SWAP)')
            plt.ylabel('Victoires')
            plt.tight_layout()
            plt.savefig('graphiques/Graph_F1_Qualite_Vs_Quantite_Swap.png')
            plt.close()
            print("✅ Graphique F1 régénéré.")

        # 2. CONSTANT 
        scenarios_const = ['DIFF_P2_VS_MOY_P4_CONSTANT', 'DIFF_P4_VS_MOY_P2_CONSTANT']
        df_sub_const = df[df['Scenario_Type'].isin(scenarios_const)].copy()
        if not df_sub_const.empty:
            # On crée une colonne pour afficher qui est J1 et qui est J2 dynamiquement
            df_sub_const['Winner_Label'] = df_sub_const.apply(lambda r: f"J1 ({r['IA_J1']})" if r['Vainqueur'] == 'J1' else (f"J2 ({r['IA_J2']})" if r['Vainqueur'] == 'J2' else 'NUL'), axis=1)
            plt.figure(figsize=(12, 6))
            sns.countplot(data=df_sub_const, x='Scenario_Type', hue='Winner_Label')
            plt.title('Graphique F2 : Duel Cerveau vs Muscles (Mode CONSTANT)')
            plt.ylabel('Victoires')
            plt.legend(title='Vainqueur Réel')
            plt.tight_layout()
            plt.savefig('graphiques/Graph_F2_Qualite_Vs_Quantite_Constant.png')
            plt.close()
            print("✅ Graphique F2 généré.")

def graph_g_miroirs_profondeur():
    """Impact pur de la profondeur (Miroirs P2 vs P4)"""
    if os.path.exists('csv/impact_profondeur_croise.csv'):
        df = pd.read_csv('csv/impact_profondeur_croise.csv')
        
        # 1. SWAP
        scenarios_swap = ['DIFF_P2_VS_DIFF_P4_SWAP', 'MOY_P2_VS_MOY_P4_SWAP']
        df_sub_swap = df[df['Scenario_Type'].isin(scenarios_swap)].copy()
        if not df_sub_swap.empty:
            def get_depth(r):
                w = r['IA_J1'] if r['Vainqueur'] == 'J1' else r['IA_J2']
                return 'P4' if 'P4' in str(w) else ('P2' if 'P2' in str(w) else 'NUL')
            df_sub_swap['Winner_Depth'] = df_sub_swap.apply(get_depth, axis=1)
            plt.figure(figsize=(10, 6))
            sns.countplot(data=df_sub_swap, x='Scenario_Type', hue='Winner_Depth', palette="viridis")
            plt.title('Graphique G1 : Impact Profondeur (Mode SWAP)')
            plt.savefig('graphiques/Graph_G1_Miroir_Swap.png')
            plt.close()
            print("✅ Graphique G1 régénéré.")

        # 2. CONSTANT 
        scenarios_const = ['DIFF_P2_VS_DIFF_P4_CONSTANT', 'MOY_P2_VS_MOY_P4_CONSTANT']
        df_sub_const = df[df['Scenario_Type'].isin(scenarios_const)].copy()
        if not df_sub_const.empty:
            df_sub_const['Winner_Label'] = df_sub_const.apply(lambda r: f"J1 ({r['IA_J1']})" if r['Vainqueur'] == 'J1' else (f"J2 ({r['IA_J2']})" if r['Vainqueur'] == 'J2' else 'NUL'), axis=1)
            plt.figure(figsize=(10, 6))
            sns.countplot(data=df_sub_const, x='Scenario_Type', hue='Winner_Label', palette="magma")
            plt.title('Graphique G2 : Résistance Profondeur (Mode CONSTANT)')
            plt.ylabel('Nombre de victoires')
            plt.legend(title='Vainqueur Réel')
            plt.savefig('graphiques/Graph_G2_Miroir_Constant.png')
            plt.close()
            print("✅ Graphique G2 généré.") 

def graph_d_impact_initiative():
    if not os.path.exists('csv/analyse_premier_joueur.csv'): return
    df = pd.read_csv('csv/analyse_premier_joueur.csv')
    counts = df['VainqueurID'].value_counts()
    if counts.empty: return
    plt.figure(figsize=(8, 6))
    plt.bar(counts.index.astype(str), counts.values, color=['#3498db', '#e74c3c'])
    plt.title('Graphique D : Equite du Jeu (J1 vs J2)')
    plt.savefig('graphiques/Graph_D_Impact_Initiative.png')
    plt.close()
    print("✅ Graphique D genere.")

def graph_e_distribution_tours():
    if not os.path.exists('csv/asymetrique_swaps.csv'): return
    df = pd.read_csv('csv/asymetrique_swaps.csv')
    plt.figure(figsize=(12, 6))
    sns.boxplot(data=df, x='Scenario_Type', y='NbTours')
    plt.title('Graphique E : Distribution du nombre de tours')
    plt.xticks(rotation=45, ha='right')
    plt.tight_layout()
    plt.savefig('graphiques/Graph_E_Distribution_Tours.png')
    plt.close()
    print("✅ Graphique E genere.")

if __name__ == '__main__':
    os.makedirs('graphiques', exist_ok=True)
    print("🚀 Debut de generation...")
    graph_a_explosion_combinatoire()
    graph_b_noeuds_visites()
    graph_c_classement_ia()
    graph_d_impact_initiative()
    graph_e_distribution_tours()
    graph_f_qualite_vs_quantite()
    graph_g_miroirs_profondeur()
    print("\n✨ Termine !")