package fr.cy.model.agent.behaviour.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Edge;

/**
 * Represents an action that an {@link Agent} can perform.
 * Implementations should encapsulate a single, well-named behavior.
 */
public abstract class AgentAction {
	private Agent agent;

	public AgentAction(Agent agent) {
		this.agent = agent;
	}

	protected Agent getAgent() {
		return agent;
	}

	/**
	 * Execute the action for the given agent.
	 * @param agent the agent performing the action
	 */
	public abstract void perform(Agent agent);

	public abstract boolean isCompleted();

	public abstract Edge getCurrentEdgeOrNextEdgeIfOnNode();
}
