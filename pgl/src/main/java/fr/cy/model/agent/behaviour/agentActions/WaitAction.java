package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

public class WaitAction extends AgentAction {
    private static final long serialVersionUID = 1L;
    private double totalTimeToWait;
    private double timeWaitedSoFar = 0.0;

    public WaitAction(Agent agent, double totalTimeToWait) {
        super(agent);
        if (totalTimeToWait <= 1e-16) {
            throw new IllegalArgumentException("Total time to wait must be positive got " + totalTimeToWait);
        }
        this.totalTimeToWait = totalTimeToWait;
    }

    @Override
    public Edge getClosestTargetEdge() {
        return getAgent().getCurrentEdge(); // The agent is waiting in place, so the closest target edge is its current edge if it's on an edge (or null)
    }

    @Override
    public Node getClosestTargetNode() {
        return Objects.requireNonNull(getAgent().getPreviousOrCurrentNode());
    }

    @Override
    public double perform(AgentSettings agentSettings, double availableTime) {
        double timeToWaitThisTick = Math.max(Math.min(availableTime, totalTimeToWait - timeWaitedSoFar), 0.0);
        timeWaitedSoFar += timeToWaitThisTick;
        setProgress(timeWaitedSoFar / totalTimeToWait);
        return timeToWaitThisTick;
    }

}
