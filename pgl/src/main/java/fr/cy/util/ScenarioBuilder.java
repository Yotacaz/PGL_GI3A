package fr.cy.util;

import fr.cy.model.agent.AgentManager;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;

/**
 * Factory permettant de construire différents scénarios de test
 * avec des graphes spatiaux, des incendies et des agents pré-positionnés.
 */
public class ScenarioBuilder {

    public static Simulation buildDemoScenario() {
        Graph graph = new Graph();

        Node n1 = graph.createNode(150, 150);
        Node n2 = graph.createNode(400, 100);
        Node n3 = graph.createNode(600, 250);
        Node n4 = graph.createNode(400, 400);
        Node n5 = graph.createNode(150, 450);

        Node sortie1 = graph.createNode(700, 100);
        Node sortie2 = graph.createNode(700, 450);
        sortie1.setExit(true);
        sortie2.setExit(true);

        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);
        graph.createEdge(n3, n4);
        graph.createEdge(n4, n5);
        graph.createEdge(n5, n1);
        graph.createEdge(n2, n4);
        graph.createEdge(n3, sortie1);
        graph.createEdge(n4, sortie2);

        Simulation simulation = new Simulation("Demo", graph);

        if (simulation.getAgentManager() != null) {
            // simulation.getAgentManager().generateAgentOnNode("Agent1", n1); //for testing
            simulation.getAgentManager().generateRandomsAgents(30);
        }

        n1.setFire(new Fire(0, 1, 0.01));
        n4.setFire(new Fire(0, 0, 0));

        return simulation;
    }

    public static Simulation buildComplexScenario() {
        Graph graph = new Graph();

        // 1. Création des Nœuds (Hall, Bureaux, Couloirs)
        Node hall = graph.createNode(400, 300); // Centre
        Node bureauA = graph.createNode(100, 100);
        Node bureauB = graph.createNode(100, 500);
        Node corridorTop = graph.createNode(400, 100);
        Node corridorBottom = graph.createNode(400, 500);
        Node officeZone = graph.createNode(650, 300);

        // 2. Création des sorties de secours (aux extrémités)
        Node sortieNord = graph.createNode(400, 0);
        Node sortieSud = graph.createNode(400, 600);
        sortieNord.setExit(true);
        sortieSud.setExit(true);

        // 3. Création des arêtes (Le maillage du bâtiment)
        graph.createEdge(bureauA, corridorTop);
        graph.createEdge(bureauB, corridorBottom);
        graph.createEdge(corridorTop, hall);
        graph.createEdge(corridorBottom, hall);
        graph.createEdge(hall, officeZone);
        graph.createEdge(corridorTop, sortieNord); // Sortie Nord
        graph.createEdge(corridorBottom, sortieSud); // Sortie Sud
        graph.createEdge(hall, officeZone);

        Simulation simulation = new Simulation("Labyrinthe en Flammes", graph);

        // 4. Peuplement : beaucoup d'agents concentrés dans les bureaux
        if (simulation.getAgentManager() != null) {
            simulation.getAgentManager().generateAgentsOnNode("Agent", bureauA, 40);
            simulation.getAgentManager().generateAgentsOnNode("Agent", bureauB, 40);
            simulation.getAgentManager().generateAgentsOnNode("Agent", officeZone, 20);
        }

        // 5. Scénario catastrophe :
        // Le hall principal prend feu, coupant la communication entre les bureaux et
        // les sorties !
        hall.setFire(new Fire(0, 0.8, 0.05)); // Feu intense et propagation rapide

        // Un petit feu débutant dans un couloir pour compliquer le pathfinding
        corridorTop.setFire(new Fire(0, 0.2, 0.02));

        return simulation;
    }

    public static Simulation setupSimplePipelineTest() {
        Graph graph = new Graph();

        // --- 1. CRÉATION DES NŒUDS ---
        // Nœud de départ (très grande capacité pour faire spawner tout le monde)
        Node startNode = graph.createNode(100, 300, 100.0);

        // La "Salle d'attente" (le nœud central où on veut voir la congestion)
        // Capacité de 20 : il peut stocker 20 agents maximum en même temps
        Node waitingRoom = graph.createNode(400, 300, 20.0);

        // Le nœud de sortie (qui détruit les agents)
        Node exitNode = graph.createNode(700, 300, 50.0);
        exitNode.setExit(true);

        // --- 2. CRÉATION DES ARÊTES ---
        // L'autoroute d'entrée : très large, débit énorme.
        Edge entranceEdge = graph.createEdge(startNode, waitingRoom);
        entranceEdge.setWidth(5.0);

        // Le goulot d'étranglement absolu :
        // Largeur de 0.4 = seulement 1 agent passera par tick maximum.
        Edge exitEdge = graph.createEdge(waitingRoom, exitNode);
        exitEdge.setWidth(0.4);

        // --- 3. GÉNÉRATION DES AGENTS ET DE LA SIMULATION ---
        Simulation simulation = new Simulation("Test Ligne Droite", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // On fait spawner 50 agents sur le nœud de départ
            am.generateAgentsOnNode("Agent_", startNode, 50);
        }

        return simulation;
    }

    public static Simulation setupMinimalistTest() {
        Graph graph = new Graph();

        // --- 1. LES 2 NŒUDS ---
        // Nœud de départ : Très grand (Capacité 100), pour faire spawner tout le monde
        // confortablement
        Node startNode = graph.createNode(150, 300, 100.0);

        // Nœud de sortie : Le goulot d'étranglement ! (Capacité 2)
        // Seulement 2 agents peuvent se tenir sur la sortie en même temps.
        Node exitNode = graph.createNode(650, 300, 2.0);
        exitNode.setExit(true);

        // --- 2. L'UNIQUE ARÊTE ---
        // On relie le départ à la sortie.
        Edge corridor = graph.createEdge(startNode, exitNode);

        // On lui donne une largeur de 2.0.
        // Si ta longueur par défaut est grande (ex: 100), sa capacité sera de 200.
        // Pour voir la congestion exploser vite, on peut forcer une petite longueur :
        corridor.setLength(10.0); // Capacité de l'arête = 2.0 * 10.0 = 20 agents max.
        corridor.setWidth(2.0);

        // --- 3. GÉNÉRATION ---
        Simulation simulation = new Simulation("Test Minimaliste 1 Arête", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // On génère 50 agents sur le nœud de départ
            // Il y a 50 agents, mais l'arête ne peut en contenir que 20 !
            am.generateAgentsOnNode("Agent_", startNode, 50);
        }

        return simulation;
    }

    public static Simulation setupBypassTest() {
        Graph graph = new Graph();

        // --- 1. CRÉATION DES NŒUDS ---
        // Nœud de départ (Capacité 100, pour accueillir tout le monde)
        Node startNode = graph.createNode(100, 300, 100.0);

        // Nœud intermédiaire du chemin direct (Le sas avant le piège, Capacité 10)
        Node waypointMain = graph.createNode(400, 300, 10.0);

        // Nœud intermédiaire du long détour (Capacité 40)
        Node waypointTop = graph.createNode(400, 100, 40.0);

        // Nœud de sortie
        Node exitNode = graph.createNode(700, 300, 100.0);
        exitNode.setExit(true);

        // --- 2. CRÉATION DES ARÊTES (Les chemins) ---

        // CHEMIN DIRECT (Court mais dangereux)
        Edge mainRoute1 = graph.createEdge(startNode, waypointMain);
        mainRoute1.setWidth(3.0); // Large accès
        mainRoute1.setLength(30.0); // Très court

        Edge mainRoute2 = graph.createEdge(waypointMain, exitNode);
        mainRoute2.setWidth(0.4); // LE PIÈGE : Porte minuscule (1 agent par tick)
        mainRoute2.setLength(10.0);

        // CHEMIN ALTERNATIF (Le grand détour)
        Edge altRoute1 = graph.createEdge(startNode, waypointTop);
        altRoute1.setWidth(2.5); // Accès correct
        altRoute1.setLength(100.0); // Très long détour ! (Poids de base énorme)

        Edge altRoute2 = graph.createEdge(waypointTop, exitNode);
        altRoute2.setWidth(4.0); // Boulevard géant vers la sortie
        altRoute2.setLength(80.0);

        // --- 3. GÉNÉRATION ---
        Simulation simulation = new Simulation("Test IA Contournement", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // On fait spawner 60 agents.
            // C'est beaucoup trop pour le chemin principal, ce qui va forcer le
            // contournement.
            am.generateAgentsOnNode("Agent_", startNode, 60);
        }

        return simulation;
    }

    public static Simulation setupFireDilemmaTest() {
        Graph graph = new Graph();

        // --- 1. CRÉATION DES NŒUDS ---
        Node startNode = graph.createNode(100, 300, 150.0); // Le grand hall de départ

        // Les 3 points de passage pour séparer les chemins
        Node topNode = graph.createNode(400, 100, 30.0);
        Node midNode = graph.createNode(400, 300, 5.0); // Petit sas central
        Node botNode = graph.createNode(400, 600, 80.0); // Grand espace en bas

        // La sortie
        Node exitNode = graph.createNode(800, 300, 150.0);
        exitNode.setExit(true);

        // --- 2. CRÉATION DES ARÊTES ---

        // 🔴 CHEMIN NORD : Très court, mais EN FEU !
        Edge topIn = graph.createEdge(startNode, topNode);
        topIn.setWidth(3.0);
        topIn.setLength(40.0);

        Edge topOut = graph.createEdge(topNode, exitNode);
        topOut.setWidth(3.0);
        topOut.setLength(40.0);

        topIn.igniteFrom(topNode, new Fire(10, 0, 0.1));

        // 🟠 CHEMIN CENTRAL : Distance moyenne, mais GOULOT D'ÉTRANGLEMENT
        Edge midIn = graph.createEdge(startNode, midNode);
        midIn.setWidth(3.0);
        midIn.setLength(60.0);

        Edge midOut = graph.createEdge(midNode, exitNode);
        midOut.setWidth(0.4);
        midOut.setLength(60.0); // L'enfer : 1 agent par tick maximum

        // 🟢 CHEMIN SUD : Sécurisé, large, mais DÉTOUR IMMENSE
        Edge botIn = graph.createEdge(startNode, botNode);
        botIn.setWidth(5.0);
        botIn.setLength(150.0); // Distance énorme

        Edge botOut = graph.createEdge(botNode, exitNode);
        botOut.setWidth(5.0);
        botOut.setLength(150.0); // Distance énorme

        botIn.igniteFrom(botNode, new Fire(10, 0, 0.1));

        // --- 3. GÉNÉRATION ---
        Simulation simulation = new Simulation("Test Dilemme : Feu vs Congestion", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // On fait spawner une foule massive (100 agents) pour forcer la prise de
            // décision
            am.generateAgentsOnNode("Agent_", startNode, 100);
        }

        return simulation;
    }

    public static void main(String[] args) {
        FileManager.saveSimulation(buildDemoScenario());
    }
}