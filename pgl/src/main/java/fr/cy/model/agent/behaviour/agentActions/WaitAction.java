package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Represents an action where an agent waits in place for a specified duration.
 * 
 * <p>This action causes the agent to remain stationary at its current position
 * (either on a node or edge) for the configured wait time. The action completes
 * when the total wait time has elapsed.</p>
 * <b> should only be used when an agent is on a node as being on an edge require to have some kind of move action. </b>
 */
public class WaitAction extends AgentAction {
    private static final long serialVersionUID = 1L;
    private double totalTimeToWait;
    private double timeWaitedSoFar = 0.0;

    /**
     * Creates a new WaitAction with the specified agent and wait duration.
     * 
     * @param agent the agent that will perform the waiting action
     * @param totalTimeToWait the total duration to wait (must be positive)
     * @throws IllegalArgumentException if totalTimeToWait is not positive
     */
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
