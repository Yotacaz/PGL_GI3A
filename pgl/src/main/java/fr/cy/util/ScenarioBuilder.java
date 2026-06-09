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
            // simulation.getAgentManager().generateAgentsOnNode("Agent", bureauA, 1);
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

        // --- 1. CREATE NODES ---
        // Starting node (very large capacity to spawn everyone comfortably)
        Node startNode = graph.createNode(100, 300, 100.0);

        // The waiting room node (the central node where we want to see congestion)
        // Capacity of 20: it can hold a maximum of 20 agents at the same time
        Node waitingRoom = graph.createNode(400, 300, 20.0);

        // The exit Node
        Node exitNode = graph.createNode(700, 300, 50.0);
        exitNode.setExit(true);

        // --- 2. CREATE EDGES ---
        // The entrance highway : very wide, enormous flow.
        Edge entranceEdge = graph.createEdge(startNode, waitingRoom);
        entranceEdge.setWidth(5.0);

        // The absolute bottleneck :
        // Width of 0.4 = only 1 agent can pass per tick maximum.
        Edge exitEdge = graph.createEdge(waitingRoom, exitNode);
        exitEdge.setWidth(0.4);

        // --- 3. GENERATE AGENTS AND SIMULATION ---
        Simulation simulation = new Simulation("Test Ligne Droite", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // Spawn 50 agents on the starting node. There are 50 agents, but the exit edge can only
            am.generateAgentsOnNode("Agent_", startNode, 50);
        }

        return simulation;
    }

    public static Simulation setupMinimalistTest() {
        Graph graph = new Graph();

        // --- 1. The 2 Nodes ---
        // Starting node : Very large (Capacity 100), to spawn everyone comfortably
        Node startNode = graph.createNode(150, 300, 100.0);

        // Exit node : The bottleneck ! (Capacity 2)
        // Only 2 agents can be on the exit at the same time.
        Node exitNode = graph.createNode(650, 300, 2.0);
        exitNode.setExit(true);

        // --- 2. The Unique Edge ---
        // We connect the start to the exit.
        Edge corridor = graph.createEdge(startNode, exitNode);

        // We give it a width of 2.0.
        // If your default length is large (e.g., 100), its capacity will be 200.
        // To see the congestion explode quickly, we can force a small length:
        corridor.setLength(10.0); // Edge capacity = 2.0 * 10.0 = 20 agents max.
        corridor.setWidth(2.0);

        // --- 3. GENERATE AGENTS ---
        Simulation simulation = new Simulation("Test Minimaliste 1 Arête", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // Spawn 50 agents on the starting node
            // There are 50 agents, but the edge can only contain 20 !
            am.generateAgentsOnNode("Agent_", startNode, 50);
        }

        return simulation;
    }

    public static Simulation setupBypassTest() {
        Graph graph = new Graph();

        // REMINDER FOR THE SCALE: 1 unit = 10 pixels.
        // Agents (0.5) will be 10 pixels in diameter on the screen.

        // --- 1. CREATE NODES ---
        // Node de départ : Capacité 150 (Diamètre visuel généré : ~138 px)
        // It is large enough to comfortably hold the 60 agents without too much overlap.
        Node startNode = graph.createNode(100, 300);
        startNode.setCapacity(150.0);

        // Nœud intermédiaire direct : Capacité 20 (Diamètre visuel généré : ~50 px)
        // Le sas avant le piège. Assez grand pour recevoir l'arête de 30px (width 3.0),
        // mais assez petit pour que le bouchon se crée rapidement.
        Node waypointMain = graph.createNode(400, 300);
        waypointMain.setCapacity(20.0);

        // Nœud intermédiaire du détour : Capacité 60 (Diamètre visuel généré : ~87 px)
        // Placé plus haut en Y (50 au lieu de 100) pour que le détour soit visuellement
        // long.
        Node waypointTop = graph.createNode(400, 50);
        waypointTop.setCapacity(60.0);

        // Nœud de sortie : Capacité 200 (Diamètre visuel : ~160 px)
        Node exitNode = graph.createNode(700, 300);
        exitNode.setCapacity(200.0);
        exitNode.setExit(true);

        // --- 2. CRÉATION DES ARÊTES (Les chemins) ---

        // CHEMIN DIRECT (Ligne droite)
        Edge mainRoute1 = graph.createEdge(startNode, waypointMain);
        mainRoute1.setWidth(3.0); // Visuel : 30 pixels (Large accès)
        // La distance physique est de 300px (400 - 100). On met length = 30.0 pour être
        // à l'échelle parfaite.
        mainRoute1.setLength(30.0);

        Edge mainRoute2 = graph.createEdge(waypointMain, exitNode);
        mainRoute2.setWidth(0.4); // LE PIÈGE : Visuel 4 pixels (1 agent à la fois en file indienne serrée)
        mainRoute2.setLength(30.0); // Distance physique 300px = length 30.0

        // CHEMIN ALTERNATIF (Le grand pont)
        Edge altRoute1 = graph.createEdge(startNode, waypointTop);
        altRoute1.setWidth(2.5); // Visuel : 25 pixels (Accès correct)
        // Distance physique Diagonale ~390px. On gonfle un peu le length (50.0) pour
        // que l'algo A* / Dijkstra
        // le trouve repoussant au début.
        altRoute1.setLength(50.0);

        Edge altRoute2 = graph.createEdge(waypointTop, exitNode);
        altRoute2.setWidth(4.0); // Visuel : 40 pixels (Autoroute géante vers la sortie)
        altRoute2.setLength(50.0);

        // Les couloirs sont à sens unique pour forcer le flux
        mainRoute1.setDirected(true);
        mainRoute2.setDirected(true);
        altRoute1.setDirected(true);
        altRoute2.setDirected(true);

        // --- 3. GÉNÉRATION ---
        Simulation simulation = new Simulation("Test Contournement", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // On fait spawner 60 agents.
            // Au début, ils voudront tous prendre la ligne droite (length 60 vs length
            // 100),
            // mais l'embouteillage sur mainRoute2 va faire exploser le coût de mainRoute1,
            // forçant les agents suivants à dévier par le Nord.
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
        midIn.setWidth(5.0);
        midIn.setLength(60.0);
        midIn.setDirected(true);

        Edge midOut = graph.createEdge(midNode, exitNode);
        midOut.setWidth(0.4);
        midOut.setLength(60.0);
        midOut.setDirected(true); // L'enfer : 1 agent par tick maximum

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
}