package fr.cy.model.agent.behaviour.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.graph.element.Edge;

/**
 * Action representing behavior where an agent follows another agent.
 */
public class FollowAgentAction extends AgentAction {

	private Agent targetAgent; 

	public FollowAgentAction(Agent agent, Agent targetAgent) {
		super(agent);
		this.targetAgent = targetAgent;
	}

	@Override
	public double perform(AgentSettings agentSettings) {
		// Placeholder: implement follow-agent behavior
		return 0.0;
	}

	@Override
	public boolean isCompleted() {
		// Placeholder: determine if the follow action is completed
		return false;
	}

	@Override
	public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
		//TODO
		return targetAgent.getCurrentEdgeOrNextEdgeIfOnNode();
	}
}
