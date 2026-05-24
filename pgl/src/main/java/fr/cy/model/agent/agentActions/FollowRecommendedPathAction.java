package fr.cy.model.agent.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.pathfinding.GraphPath;

/**
 * Action representing following a recommended path (from pathfinder or signage).
 */
public class FollowRecommendedPathAction extends AbstractFollowPathAction {
	public FollowRecommendedPathAction(GraphPath path) {
		super(path);
	}

	@Override
	public void perform(Agent agent) {
		// Placeholder: implement recommended-path behavior
	}
}
