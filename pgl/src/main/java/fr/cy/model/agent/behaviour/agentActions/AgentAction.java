package fr.cy.model.agent.behaviour.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;

/**
 * Represents an action that an {@link Agent} can perform.
 * Implementations should encapsulate a single, well-named behavior.
 */
public abstract class AgentAction {
	private Agent agent;
	/** The progress of the action, between 0 and 1 */
	private double progress = 0.0;

	public AgentAction(Agent agent) {
		this.agent = agent;
	}

	protected Agent getAgent() {
		return agent;
	}

	public double getProgress() {
		return progress;
	}

	protected void setProgress(double newProgress) {
		if (newProgress < 0) {
			throw new IllegalArgumentException("Progress must be positive");
		}
		progress = Math.min(newProgress, 1.0);
	}

	protected void incrementProgress(double delta) {
		setProgress(progress + delta);
	}

	/**
	 * Execute the action for the given agent.
	 * @return the consumed time after performing the action for this tick, 0 if a whole tick is left to be consumed.
	 */
	public abstract double perform(AgentSettings agentSettings);

	public boolean isCompleted() {
		return progress >= 1.0;
	}

	public abstract Edge getCurrentEdgeOrNextEdgeIfOnNode();
}
