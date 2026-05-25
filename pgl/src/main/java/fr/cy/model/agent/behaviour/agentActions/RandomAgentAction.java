package fr.cy.model.agent.behaviour.agentActions;

import java.util.List;

import fr.cy.model.agent.Agent;
import fr.cy.model.graph.element.Edge;

/**
 * Action representing a random choice or movement performed by an agent.
 */
public class RandomAgentAction extends AgentAction {
	private Edge chosenEdge = null;
	public RandomAgentAction(Agent agent) {
		super(agent);
	}

	@Override
	public void perform(Agent agent) {
		// Placeholder: implement random behavior
	}

	@Override
	public boolean isCompleted() {
		// Placeholder: determine if the random action is completed
		return false;	
	}


	@Override
	public Edge getCurrentEdgeOrNextEdgeIfOnNode() {
		Agent agent = getAgent();
		if (!agent.isOnNode()) {
			return chosenEdge; // Random action does not specify a particular edge when on a node
		}
		List<Edge> outgoingEdges = agent.getPreviousNode().getOutGoingEdges();
		return null; //TODO
		
		
	}
}
