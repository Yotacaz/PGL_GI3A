package fr.cy.model.agent.behaviour.agentActions;

import fr.cy.model.agent.Agent;
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
	public void perform(Agent agent) {
		// Placeholder: implement follow-agent behavior
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
