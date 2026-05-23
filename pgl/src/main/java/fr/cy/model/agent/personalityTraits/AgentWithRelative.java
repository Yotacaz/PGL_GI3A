package fr.cy.model.agent.personalityTraits;

import java.util.List;

import fr.cy.model.agent.Agent;

/**
 * Personality trait for agents that care about family members. The trait may
 * influence routing/decisions so that the agent stays close to its relatives.
 */
public class AgentWithRelative extends AgentPersonalityTrait {

    /** Level of attachment to family members, between 0 and 1 */
    private double attachmentLevel;

    private List<Agent> familyMembers;

    public AgentWithRelative(List<Agent> familyMembers) {
        this.familyMembers = familyMembers;
    }

    public boolean addRelative(Agent agent) {
        return familyMembers.add(agent);
    }
}
