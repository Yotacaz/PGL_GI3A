package fr.cy.model.simulation;

import java.util.ArrayList;
import java.util.List;

import fr.cy.model.agent.Agent;
import fr.cy.model.fire.FireService;
import fr.cy.model.graph.Graph;
import fr.cy.model.pathfinding.PathFinder;

public class Simulation {
    private final Graph graph;
    private final List<Agent> agents;
    private final PathFinder pathFinder;
    private final FireService fireService;

    private int currentTick;
    private boolean running;

    public Simulation(Graph graph) {
        this.graph = graph;
        this.agents = new ArrayList<>();
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
        for (Agent agent : agents) {
        }
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

    public List<Agent> getAgents() {
        return agents;
    }

    public Graph getGraph() {
        return graph;
    }
}
