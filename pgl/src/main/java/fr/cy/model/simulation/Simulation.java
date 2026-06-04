package fr.cy.model.simulation;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentGenerator;
import fr.cy.model.agent.AgentManager;
import fr.cy.model.agent.behaviour.decisions.NodeContextProvider;
import fr.cy.model.fire.FireService;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.PathFinder;

import java.io.*;
import java.util.stream.Collectors;

public class Simulation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;

    private final Graph graph;
    private final AgentManager agentManager;
    private final PathFinder pathFinder;
    private final FireService fireService;
    private final SimulationSettings simulationSettings;

    private int currentTick;
    private boolean running;

    public Simulation(String name, Graph graph) {
        this(name, graph, SimulationSettings.getInstance());
    }

    public Simulation(String name, Graph graph, SimulationSettings simulationSettings) {
        this.graph = graph;
        this.simulationSettings = simulationSettings;
        this.name = name;

        this.pathFinder = new PathFinder(graph);
        this.fireService = new FireService();

        NodeContextProvider decisionContextProvider = new NodeContextProvider(graph, pathFinder);
        AgentGenerator agentGenerator = new AgentGenerator(graph);

        this.agentManager = new AgentManager(decisionContextProvider, agentGenerator, simulationSettings);
        // NO AGENT IS GENERATED YET

        this.currentTick = 0;
        this.running = false;
    }

    public String getName() {
        return name;
    }

    public void reset() {
        graph.reset();
        agentManager.reset();
        currentTick = 0;
        running = false;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void tick() {
        if (!running) {
            return;
        }

        double effectiveTickDuration = simulationSettings.getTickDuration() * simulationSettings.getSpeedMultiplier();
        fireService.updateFires(graph, effectiveTickDuration);
        graph.tick();
        agentManager.tick(effectiveTickDuration);
        currentTick++;
    }

    public void stepTick() {
        double effectiveTickDuration = simulationSettings.getTickDuration() * simulationSettings.getSpeedMultiplier();
        fireService.updateFires(graph, effectiveTickDuration);
        graph.tick();
        agentManager.tick(effectiveTickDuration);
        currentTick++;
    }

    public boolean isRunning() {
        return running;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public Graph getGraph() {
        return graph;
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public PathFinder getPathFinder() {
        return pathFinder;
    }

    public FireService getFireService() {
        return fireService;
    }

    public SimulationSettings getSimulationSettings() {
        return simulationSettings;
    }

    @Override
    public String toString() {
        String agentDetails = agentManager.getAgentsToEvacuate().stream().map(Agent::toString)
            .collect(Collectors.joining("\n"));
        return "=== SIMULATION STATUS ===\n" +
                "Current Tick: " + currentTick + "\n" +
                "Running: " + (running ? "Yes" : "No") + "\n" +
                "Active Agents: " + agentManager.getAgentsToEvacuate().size() + "\n" +
                // "Active Fires: " + fireService.getActiveFires().size() + "\n" +
                "========================" +
                "\nGraph:\n" + graph.toString() +
            "\nAgents:\n" + agentDetails + (agentDetails.isEmpty() ? "" : "\n");
    }

    public static void main(String[] args) {
        // test code
        Graph graph = new Graph();
        // List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                graph.createNode(i * 10, j * 10);
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Node currentNode = graph.getNodes().get(i * 3 + j);
                if (j < 2) {
                    Node rightNode = graph.getNodes().get(i * 3 + (j + 1));
                    graph.createEdge(currentNode, rightNode);
                }
                if (i < 2) {
                    Node downNode = graph.getNodes().get((i + 1) * 3 + j);
                    graph.createEdge(currentNode, downNode);
                }
            }
        }
        // Initialize graph with nodes and edges
        Simulation simulation = new Simulation("TEST", graph);
        simulation.getAgentManager().generateRandomsAgents(1); // Generate 1 agent
        simulation.start();

        // Run the simulation for a certain number of ticks
        for (int i = 0; i < 1000; i++) {
            simulation.tick();
            System.out.println("Tick: " + simulation.getCurrentTick() + ", Agents: "
                    + simulation.getAgentManager().getAgentsToEvacuate().size());
            System.out.println(simulation);
            System.out.println("--------------------------------------------------");
            System.out.println("press Enter to continue...");
            try {
                System.in.read();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
