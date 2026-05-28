package fr.cy.model.simulation;


import fr.cy.model.agent.AgentManager;
import fr.cy.model.agent.behaviour.decisions.DecisionContextProvider;
import fr.cy.model.fire.FireService;
import fr.cy.model.graph.Graph;
import fr.cy.model.pathfinding.PathFinder;

public class Simulation {
    private final Graph graph;
    private final AgentManager agentManager;
    private final PathFinder pathFinder;
    private final FireService fireService;

    private int currentTick;
    public static final double TICK_DURATION = 1.0; 
    private boolean running;

    public Simulation(Graph graph) {
        this.graph = graph;
        this.pathFinder = new PathFinder(graph);
        this.fireService = new FireService();
        // DecisionContextProvider decisionContextProvider = new DecisionContextProvider(graph, pathFinder);
        this.agentManager = new AgentManager();

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
        agentManager.tick();
        currentTick++;
    }

    public boolean isRunning() {
        return running;
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


}
