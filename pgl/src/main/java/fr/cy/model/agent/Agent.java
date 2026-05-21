package fr.cy.model.agent;

import java.util.Optional;

import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;

public class Agent {
    private int id;
    private String name;
    private int maxSpeed;
    private int currentSpeed;

    private int travelProgressInComponent;
    private int nOfNodeVisited;

    private float stressLevel = 0.0f;
    private float stressTolerance;

    private float crowdingTolerance;
    private GraphElement currentComponent;
    private Node previousNode = null;

    public Agent(int id, String name, int maxSpeed, float stressTolerance, float crowdingTolerance) {
        this.id = id;
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.currentSpeed = maxSpeed; // Start at max speed
        this.stressTolerance = stressTolerance;
        this.crowdingTolerance = crowdingTolerance;
    }

    public AgentState getState() {
        Optional<AgentState> optState = AgentState.fromFloat(stressLevel, stressTolerance);
        return optState.orElse(AgentState.PANICKING); // Default to PANICKING if no state matches
    }

    public boolean isPanicking() {
        return stressLevel >= stressTolerance;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getStressLevel() {
        return stressLevel;
    }

    public int getnOfNodeVisited() {
        return nOfNodeVisited;
    }

    public Node getPreviousNode() {
        return previousNode;
    }

    public float getCrowdingTolerance() {
        return crowdingTolerance;
    }

    public int getCurrentSpeed() {
        return currentSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public float getStressTolerance() {
        return stressTolerance;
    }

    public int getTravelProgressInComponent() {
        return travelProgressInComponent;
    }

}
