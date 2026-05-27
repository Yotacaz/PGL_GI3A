package fr.cy.model.simulation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import fr.cy.model.agent.AgentManager;
import fr.cy.model.fire.FireService;
import fr.cy.model.graph.Graph;
import fr.cy.model.pathfinding.PathFinder;

public class Simulation implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Graph graph;
    private transient AgentManager agentManager;
    private transient PathFinder pathFinder;
    private transient FireService fireService;

    private int currentTick;
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

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();

        this.pathFinder = new PathFinder(graph);
        this.fireService = new FireService();
        this.agentManager = new AgentManager();
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

    
    public int getCurrentTick() {
        return currentTick;
    }


}
