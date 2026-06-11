package fr.cy.model.agent.behaviour.agentActions;

import java.io.Serializable;
import java.util.Objects;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Represents an action that an {@link Agent} can perform.
 * 
 * <p>This abstract class serves as the base for all agent actions in the simulation.
 * Each action encapsulates a specific behavior that an agent can execute during
 * a simulation tick. Actions track their progress and can be completed over
 * multiple ticks if needed.</p>
 * 
 * <p>Implementations should encapsulate a single, well-defined behavior and provide
 * clear information about the agent's intentions for conflict resolution and
 * visualization purposes.</p>
 */
public abstract class AgentAction implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/** The agent that is performing this action */
	protected final Agent agent;
	
	/**
	 * The progress of the action, between 0 and 1.
	 * A value of 0 indicates the action has just started,
	 * while 1 indicates the action is complete.
	 */
	protected double progress = 0.0;

	/**
	 * Creates a new AgentAction for the specified agent.
	 * 
	 * @param agent the agent that will perform this action (must not be null)
	 * @throws NullPointerException if the agent parameter is null
	 */
	public AgentAction(Agent agent) {
		this.agent = Objects.requireNonNull(agent);
	}

	/**
	 * Gets the agent performing this action.
	 * 
	 * @return the agent associated with this action
	 */
	protected Agent getAgent() {
		return agent;
	}

	/**
	 * Gets the progress of this action along the current edge.
	 * 
	 * <p>This method returns -1 by default, indicating that the action
	 * does not involve edge traversal. Subclasses that involve movement
	 * along edges should override this method to return the actual
	 * edge progress (0.0 to 1.0).</p>
	 * 
	 * @return the edge progress (0.0 to 1.0) or -1 if not applicable
	 */
	public double getEdgeProgress() {
		return -1.0; // Default implementation returns -1, meaning not applicable
	}

	/**
	 * Gets the overall progress of this action.
	 * 
	 * @return the action progress (0.0 to 1.0)
	 */
	public double getProgress() {
		return progress;
	}

	/**
	 * Sets the progress of this action.
	 * 
	 * @param newProgress the new progress value (must be >= 0.0, will be clamped to 1.0)
	 * @throws IllegalArgumentException if newProgress is negative
	 */
	protected void setProgress(double newProgress) {
		if (newProgress < 0) {
			throw new IllegalArgumentException("Progress must be positive");
		}
		progress = Math.min(newProgress, 1.0);
	}

	/**
	 * Executes this action with the given available time.
	 * 
	 * <p>This method is called during each simulation tick to allow the action
	 * to make progress. The action should consume some or all of the available
	 * time and return the actual time consumed.</p>
	 * 
	 * @param agentSettings the agent settings containing parameters that may affect action execution
	 * @param availableTime the time available for this action in the current tick (in simulation time units)
	 * @return the time effectively consumed by this action (should be less than or equal to {@code availableTime})
	 */
	public abstract double perform(AgentSettings agentSettings, double availableTime);

	/**
	 * Checks if this action has been completed.
	 * 
	 * @return true if the action progress has reached 1.0, false otherwise
	 */
	public boolean isCompleted() {
		return progress >= 1.0;
	}

	/**
	 * Gets the closest target edge that the agent is targeting with this action.
	 * 
	 * <p>This method is used for conflict resolution and should reflect the agent's
	 * immediate intentions regarding edge usage. It helps the simulation resolve
	 * conflicts when multiple agents attempt to use the same edge.</p>
	 * 
	 * @return the next edge that the agent is targeting, or null if this action does not involve edge targeting
	 */
	public abstract Edge getClosestTargetEdge();

	/**
	 * Gets the closest target node that the agent is targeting with this action.
	 * 
	 * <p>This method provides information about the agent's immediate destination node,
	 * which is used for path planning, conflict resolution, and visualization.</p>
	 * 
	 * @return the next node that the agent is targeting, or null if this action does not involve node targeting
	 */
	public abstract Node getClosestTargetNode();

	//TODO
	/** @return the final destination graph element of this action, or null if none. For visualization purposes.*/
	// public abstract GraphElement getCurrentTargetGraphElement();

	/**
	 * Computes the hash code for this action.
	 * 
	 * <p>The hash code is based on the agent's ID, the action's progress,
	 * and the action's class to ensure proper behavior in hash-based collections.</p>
	 * 
	 * @return the hash code for this action
	 */
	@Override
	public int hashCode() {
		return Objects.hash(agent == null ? null : agent.getId(), progress, this.getClass());
	}

	/**
	 * Checks if this action is equal to another object.
	 * 
	 * <p>Two actions are considered equal if they are of the same class,
	 * have the same progress, and are associated with the same agent.</p>
	 * 
	 * @param obj the object to compare with this action
	 * @return true if the objects are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		AgentAction other = (AgentAction) obj;
		return Double.compare(progress, other.progress) == 0
				&& Objects.equals(agent == null ? null : agent.getId(),
						other.agent == null ? null : other.agent.getId());
	}

	/**
	 * Returns a string representation of this action.
	 * 
	 * <p>The string includes the action's class name, the associated agent's ID,
	 * and the current progress, formatted for easy reading.</p>
	 * 
	 * @return a string representation of this action
	 */
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{" +
				"agentId=" + (agent == null ? "null" : agent.getId()) +
				", progress=" + String.format("%.3f", progress) +
				'}';
	}
}
