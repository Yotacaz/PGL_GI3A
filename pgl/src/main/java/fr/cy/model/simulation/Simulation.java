package fr.cy.model.simulation;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentGenerator;
import fr.cy.model.agent.AgentManager;
import fr.cy.model.agent.behaviour.decisions.ContextProvider;
import fr.cy.model.fire.FireService;
import fr.cy.model.graph.Graph;
import fr.cy.model.pathfinding.PathFinder;

import java.io.Serializable;
import java.util.stream.Collectors;

/**
 * The {@code Simulation} class represents the central engine for an evacuation
 * scenario.
 * <p>
 * It orchestrates the lifecycle of the simulation, including the graph state,
 * agent behavior management, fire propagation, and pathfinding synchronization.
 * </p>
 */
public class Simulation implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Name of the simulation for identification. */
    private final String name;

    // --- CORE COMPONENTS ---
    private final Graph graph;
    private final AgentManager agentManager;
    private final PathFinder pathFinder;
    private final FireService fireService;
    private final SimulationSettings simulationSettings;

    /** Current time step of the simulation. */
    private int currentTick;

    /** Running state of the simulation. */
    private boolean running;

    /** Tracks the execution time of the last computed tick in milliseconds. */
    private double lastEngineLoadMs;

    /**
     * Constructs a simulation with a specific name and graph.
     * * @param name Simulation name.
     * 
     * @param graph Environment graph.
     */
    public Simulation(String name, Graph graph) {
        this(name, graph, SimulationSettings.getInstance());
    }

    /**
     * Constructs a simulation with generated graph and agents.
     * * @param name Simulation name.
     * 
     * @param nodeCount          Number of nodes.
     * @param edgeCount          Number of edges.
     * @param agentCount         Number of agents to generate.
     * @param simulationSettings Simulation configuration.
     */
    public Simulation(String name, int nodeCount, int edgeCount, int agentCount,
            SimulationSettings simulationSettings) {
        this(name, new Graph(nodeCount, edgeCount), simulationSettings);
        this.agentManager.generateRandomsAgents(agentCount);
    }

    /**
     * Base constructor for the Simulation.
     * * @param name Simulation name.
     * 
     * @param graph              Environment graph.
     * @param simulationSettings Simulation configuration.
     */
    public Simulation(String name, Graph graph, SimulationSettings simulationSettings) {
        this.graph = graph;
        this.simulationSettings = simulationSettings;
        this.name = name;

        this.pathFinder = new PathFinder(graph);
        this.fireService = new FireService();

        ContextProvider decisionContextProvider = new ContextProvider(graph, pathFinder);
        AgentGenerator agentGenerator = new AgentGenerator(graph);

        this.agentManager = new AgentManager(decisionContextProvider, agentGenerator, simulationSettings);

        this.currentTick = 0;
        this.running = false;
        this.lastEngineLoadMs = 0;
    }

    /**
     * Resets the simulation to its initial state, clearing agents and graph status.
     */
    public void reset() {
        graph.reset();
        agentManager.reset();
        currentTick = 0;
        running = false;
        lastEngineLoadMs = 0.0;
    }

    /** Starts the simulation execution. */
    public void start() {
        running = true;
    }

    /** Pauses the simulation execution. */
    public void stop() {
        running = false;
    }

    /**
     * Executes a single simulation step if running.
     */
    public void tick() {
        if (!running)
            return;

        long startTime = System.nanoTime();

        double effectiveTickDuration = simulationSettings.getTickDuration() * simulationSettings.getSpeedMultiplier();
        fireService.updateFires(graph, effectiveTickDuration);
        graph.tick();
        agentManager.tick(effectiveTickDuration);
        currentTick++;

        this.lastEngineLoadMs = (System.nanoTime() - startTime) / 1_000_000.0;
    }

    /**
     * Forces a single tick execution, regardless of the running state.
     */
    public void stepTick() {
        long startTime = System.nanoTime();

        double effectiveTickDuration = simulationSettings.getTickDuration() * simulationSettings.getSpeedMultiplier();
        fireService.updateFires(graph, effectiveTickDuration);
        graph.tick();
        agentManager.tick(effectiveTickDuration);
        currentTick++;

        this.lastEngineLoadMs = (System.nanoTime() - startTime) / 1_000_000.0;
    }

    // --- GETTERS ---
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

    public String getName() {
        return name;
    }

    /**
     * @return The time in milliseconds it took to compute the last tick.
     */
    public double getLastEngineLoadMs() {
        return lastEngineLoadMs;
    }

    @Override
    public String toString() {
        String agentDetails = agentManager.getAgentsToEvacuate().stream()
                .map(Agent::toString)
                .collect(Collectors.joining("\n"));
        return "=== SIMULATION STATUS ===\n" +
                "Current Tick: " + currentTick + "\n" +
                "Running: " + (running ? "Yes" : "No") + "\n" +
                "Active Agents: " + agentManager.getAgentsToEvacuate().size() + "\n" +
                "Last Engine Load: " + lastEngineLoadMs + " ms\n" +
                "========================\n" +
                "Graph:\n" + graph.toString() +
                "\nAgents:\n" + agentDetails;
    }
}