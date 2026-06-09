package fr.cy.model.agent.behaviour.agentActions;

import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Represents an action where an agent waits for a specified duration and then
 * automatically transitions to another action.
 * 
 * <p>This class extends {@link WaitAction} to add the capability of chaining actions.
 * After the wait period completes, the agent automatically starts the configured
 * next action.</p>
 */
public class WaitBeforeOtherAction extends WaitAction {
    private static final long serialVersionUID = 1L;

    /** The action to be performed after the wait period completes */
    private final AgentAction nextAction;

    /**
     * Creates a new WaitBeforeOtherAction that will wait for the specified duration
     * and then execute the next action.
     * 
     * @param agent the agent that will perform this action
     * @param totalTimeToWait the duration to wait before starting the next action
     * @param nextAction the action to perform after waiting (can be null)
     */
    public WaitBeforeOtherAction(Agent agent, double totalTimeToWait, AgentAction nextAction) {
        super(agent, totalTimeToWait);
        this.nextAction = Objects.requireNonNull(nextAction);
    }

    /**
     * Executes this action for the given available time.
     * 
     * <p>If the wait period completes during this execution, the next action
     * is automatically set as the agent's current action.</p>
     * 
     * @param agentSettings the agent settings to use during execution
     * @param availableTime the time available for this action execution
     * @return the time actually consumed by this action
     */
    @Override
    public double perform(AgentSettings agentSettings, double availableTime) {
        double timeWaitedThisTick = super.perform(agentSettings, availableTime);
        if (waitingIsFinished()) {
            getAgent().setCurrentAction(nextAction);
        }
        return timeWaitedThisTick;
    }

    /**
    * Checks if the waiting period has finished.
    * 
    * @return true if the wait time has completed, false otherwise
    */
    public boolean waitingIsFinished() {
        return super.isCompleted();
    }

    /**
     * Checks if this action is completed.
     * 
     * <p>The action is considered completed only when both the wait period
     * has finished and the next action (if any) has also completed.</p>
     * <b> This means that in this case isCompleted will always return false
     *  because once the wait is finished, the next action will be set as current action </b>
     * 
     * @return true if both the wait action and next action are completed, false otherwise
     */
    @Override
    public boolean isCompleted() {
        return waitingIsFinished() && (nextAction == null || nextAction.isCompleted());
    }

    /** @return the next action that will be executed after the wait period */
    public AgentAction getNextAction() {
        return nextAction;
    }

    @Override
    public Edge getClosestTargetEdge() {
        return nextAction.getClosestTargetEdge();
    }

    @Override
    public Node getClosestTargetNode() {
        return nextAction.getClosestTargetNode();
    }
}
