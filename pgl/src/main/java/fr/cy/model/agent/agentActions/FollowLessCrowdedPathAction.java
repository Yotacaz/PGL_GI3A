package fr.cy.model.agent.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.pathfinding.GraphPath;

/**
 * Action representing a decision to follow a less crowded path.
 */
public class FollowLessCrowdedPathAction extends AbstractFollowPathAction {

	public FollowLessCrowdedPathAction(GraphPath path) {
		super(path);
	}

	@Override
	public void perform(Agent agent) {
		// Placeholder: implement less-crowded-path behavior
	}
}