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

        // 1. Creation of Nodes 
        Node hall = graph.createNode(400, 300); // Center of the building
        Node officeA = graph.createNode(100, 100);
        Node officeB = graph.createNode(100, 500);
        Node corridorTop = graph.createNode(400, 100);
        Node corridorBottom = graph.createNode(400, 500);
        Node officeZone = graph.createNode(650, 300);

        // 2. Creation of exits
        Node northExit = graph.createNode(400, 0);
        Node southExit = graph.createNode(400, 600);
        northExit.setExit(true);
        southExit.setExit(true);

        // 3. Creation of the edges (The building's layout)
        graph.createEdge(officeA, corridorTop);
        graph.createEdge(officeB, corridorBottom);
        graph.createEdge(corridorTop, hall);
        graph.createEdge(corridorBottom, hall);
        graph.createEdge(hall, officeZone);
        graph.createEdge(corridorTop, northExit); // Sortie Nord
        graph.createEdge(corridorBottom, southExit); // Sortie Sud
        graph.createEdge(hall, officeZone);

        Simulation simulation = new Simulation("Labyrinthe en Flammes", graph);

        // 4. Population: many agents concentrated in the offices
        if (simulation.getAgentManager() != null) {
            // simulation.getAgentManager().generateAgentsOnNode("Agent", officeA, 1);
            simulation.getAgentManager().generateAgentsOnNode("Agent", officeA, 40);
            simulation.getAgentManager().generateAgentsOnNode("Agent", officeB, 40);
            simulation.getAgentManager().generateAgentsOnNode("Agent", officeZone, 20);
        }

        //  5. Catastrophe Scenario: The main hall catches fire, cutting off 
        // communication between the offices and the exits!
       
        hall.setFire(new Fire(0, 0.8, 0.05)); // Intense fire and fast spreading

        // Small starting fire in the corridor to create a dilemma for agents in officeA
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
        // Starting node: Capacity 150 (Visual diameter generated: ~138 px)
        // It is large enough to comfortably hold the 60 agents without too much overlap.
        Node startNode = graph.createNode(100, 300);
        startNode.setCapacity(150.0);

        // Intermediate node : Capacity 20 (Visual diameter generated: ~50 px)
        // The sas before the bottleneck. Large enough to hold the 20 agents 
        // that can be on the edge at the same time, but small enough to create a bottleneck quickly.
        
        Node waypointMain = graph.createNode(400, 300);
        waypointMain.setCapacity(20.0);

        // Intermediate node for the bypass route : Capacity 60 (Visual diameter generated: ~87 px)
        // Located higher in Y (50 instead of 100) to make the bypass visually more 
        // distinct and less tempting at the beginning due to the longer distance.
        Node waypointTop = graph.createNode(400, 50);
        waypointTop.setCapacity(60.0);

        // Exit Node : Capacity 200 (Visual diameter generated: ~160 px)
        Node exitNode = graph.createNode(700, 300);
        exitNode.setCapacity(200.0);
        exitNode.setExit(true);

        // --- 2. Creation of Edges  ---

        // DIRECT PATH
        Edge mainRoute1 = graph.createEdge(startNode, waypointMain);
        mainRoute1.setWidth(3.0); // Visuel : 30 pixels (Large accès)
        // Physical distance: 300px but length is 30.0 to be perfectly scaled.
        mainRoute1.setLength(30.0);

        Edge mainRoute2 = graph.createEdge(waypointMain, exitNode);
        mainRoute2.setWidth(0.4); // THE TRAP: Visual 4 pixels (1 agent at a time in a tight single file)       
        mainRoute2.setLength(30.0); // Distance physique 300px = length 30.0

        // Alternative PATH (the bypass)
        Edge altRoute1 = graph.createEdge(startNode, waypointTop);
        altRoute1.setWidth(2.5); 
        altRoute1.setLength(50.0);

        Edge altRoute2 = graph.createEdge(waypointTop, exitNode);
        altRoute2.setWidth(4.0); 
        altRoute2.setLength(50.0);

        // One way corridors to force the flow
        mainRoute1.setDirected(true);
        mainRoute2.setDirected(true);
        altRoute1.setDirected(true);
        altRoute2.setDirected(true);

        // --- 3. GENERATION ---
        Simulation simulation = new Simulation("Test Contournement", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // Spawn 60 agents
            // At the start, they will all want to take the straight line (length 60 vs length 100),
            // but the congestion on mainRoute2 will make the cost of mainRoute1 explode,
            // forcing the following agents to deviate via the North.
            am.generateAgentsOnNode("Agent_", startNode, 60);
        }

        return simulation;
    }

    public static Simulation setupFireDilemmaTest() {
        Graph graph = new Graph();

        // --- 1. Creation of Nodes ---
        Node startNode = graph.createNode(100, 300, 150.0); // Starting Hall

        // Les 3 points de passage pour séparer les chemins
        Node topNode = graph.createNode(400, 100, 30.0);
        Node midNode = graph.createNode(400, 300, 5.0); // Small central sas
        Node botNode = graph.createNode(400, 600, 80.0); // Small place at the bottom

        // The exit
        Node exitNode = graph.createNode(800, 300, 150.0);
        exitNode.setExit(true);

        // --- 2. Creation of Edges ---

        // 🔴 North path : Very short, but ON FIRE !
        Edge topIn = graph.createEdge(startNode, topNode);
        topIn.setWidth(3.0);
        topIn.setLength(40.0);

        Edge topOut = graph.createEdge(topNode, exitNode);
        topOut.setWidth(3.0);
        topOut.setLength(40.0);

        topIn.igniteFrom(topNode, new Fire(10, 0, 0.1));

        // 🟠 CENTRAL PATH : Medium lenght, but BOTTLENECK
        Edge midIn = graph.createEdge(startNode, midNode);
        midIn.setWidth(5.0);
        midIn.setLength(60.0);
        midIn.setDirected(true);

        Edge midOut = graph.createEdge(midNode, exitNode);
        midOut.setWidth(0.4);
        midOut.setLength(60.0);
        midOut.setDirected(true); 

        // 🟢 SOUTH PATH : Secure, wide, but LONG DETOUR
        Edge botIn = graph.createEdge(startNode, botNode);
        botIn.setWidth(5.0);
        botIn.setLength(150.0); 

        Edge botOut = graph.createEdge(botNode, exitNode);
        botOut.setWidth(5.0);
        botOut.setLength(150.0); 

        botIn.igniteFrom(botNode, new Fire(10, 0, 0.1));

        // --- 3. GENERATION ---
        Simulation simulation = new Simulation("Test Dilemme : Feu vs Congestion", graph);
        AgentManager am = simulation.getAgentManager();

        if (am != null) {
            // Spawn a lot of agents in the starting hall. 
            // They will have to choose between:
            // - The North path, which is the shortest but on fire (high risk of getting stuck and burned)
            // - The Central path, which is a medium length but has a severe bottleneck (
            //   high risk of getting stuck in the congestion and suffocating)
            // - The South path, which is long but wide and secure (low risk, but
            //   many agents will want to avoid it at the beginning due to the long distance, creating a self-fulfilling prophecy of congestion on the other paths)
            am.generateAgentsOnNode("Agent_", startNode, 100);
        }

        return simulation;
    }
}