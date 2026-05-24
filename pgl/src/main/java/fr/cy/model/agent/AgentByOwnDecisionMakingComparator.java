
package fr.cy.model.agent;

import java.util.Comparator;

/**
 * A comparator for ordering agents based on their own decision-making factors.
 */
public class AgentByOwnDecisionMakingComparator implements Comparator<Agent> {

    @Override
    public int compare(Agent a1, Agent a2) {
        // Higher decision-making score means higher priority (comes first)
        return Double.compare(a2.getCurrentOwnDecisionMakingFactor(), a1.getCurrentOwnDecisionMakingFactor());
    }

}