package fr.cy.model.simulation;


import fr.cy.model.agent.AgentManager;
import fr.cy.model.fire.FireService;
import fr.cy.model.graph.Graph;
import fr.cy.model.pathfinding.PathFinder;

public class Simulation {
    private final Graph graph;
    private final AgentManager agentManager;
    private final PathFinder pathFinder;
    private final FireService fireService;

    private int currentTick;
    private boolean running;

    public Simulation(Graph graph) {
        this.graph = graph;
        this.agentManager = new AgentManager();
        this.pathFinder = new PathFinder();
        this.fireService = new FireService();

        this.currentTick = 0;
        this.running = false;
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void moveAgents() {
        
    }

    public void spreadFire() {

    }

    public void updateStress() {

    }

    public void tick() {
        if (!running) {
            return;
        }

        fireService.updateFires(graph);
        moveAgents();
        updateStress();

        currentTick++;
    }

    public boolean isRunning() {
        return running;
    }

    // public List<Agent> getAgents() {
    //     return agents;
    // }

    public Graph getGraph() {
        return graph;
    }
}
