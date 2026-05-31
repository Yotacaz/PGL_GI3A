package fr.cy.model.simulation;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentGenerator;
import fr.cy.model.agent.AgentManager;
import fr.cy.model.agent.behaviour.decisions.DecisionContextProvider;
import fr.cy.model.fire.FireService;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.pathfinding.PathFinder;

public class Simulation {
    private final Graph graph;
    private final AgentManager agentManager;
    private final PathFinder pathFinder;
    private final FireService fireService;
    private final SimulationSettings simulationSettings;

    private int currentTick;
    private boolean running;

    public Simulation(Graph graph) {
        this(graph, new SimulationSettings());
    }

    public Simulation(Graph graph, SimulationSettings simulationSettings) {
        this.graph = graph;
        this.simulationSettings = simulationSettings;
        this.pathFinder = new PathFinder(graph);
        this.fireService = new FireService();
        DecisionContextProvider decisionContextProvider = new DecisionContextProvider(graph, pathFinder);
        AgentGenerator agentGenerator = new AgentGenerator(graph);
        this.agentManager = new AgentManager(decisionContextProvider, agentGenerator, simulationSettings); //NO AGENT IS GENERATED YET

        this.currentTick = 0;
        this.running = false;
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

        fireService.updateFires(graph);
        graph.tick();
        agentManager.tick(simulationSettings.getTickDuration());
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
        return "=== SIMULATION STATUS ===\n" +
                "Current Tick: " + currentTick + "\n" +
                "Running: " + (running ? "Yes" : "No") + "\n" +
                "Active Agents: " + agentManager.getAgents().size() + "\n" +
                // "Active Fires: " + fireService.getActiveFires().size() + "\n" +
                "========================" +
                "\nGraph:\n" + graph.toString() +
                "\nAgents:\n"
                + agentManager.getAgents().stream().map(Agent::toString).reduce("", (a, b) -> a + b + "\n");
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
        Simulation simulation = new Simulation(graph);
        simulation.getAgentManager().generateRandomsAgents(1); // Generate 1 agent
        simulation.start();

        // Run the simulation for a certain number of ticks
        for (int i = 0; i < 1000; i++) {
            simulation.tick();
            System.out.println("Tick: " + simulation.getCurrentTick() + ", Agents: "
                    + simulation.getAgentManager().getAgents().size());
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
