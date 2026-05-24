package fr.cy.model.agent.agentActions;

import fr.cy.model.agent.Agent;

/**
 * Represents an action that an {@link Agent} can perform.
 * Implementations should encapsulate a single, well-named behavior.
 */
public interface AgentAction {
	/**
	 * Execute the action for the given agent.
	 * @param agent the agent performing the action
	 */
	void perform(Agent agent);

	boolean isCompleted();
}
