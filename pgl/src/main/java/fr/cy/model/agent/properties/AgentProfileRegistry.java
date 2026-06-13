package fr.cy.model.agent.properties;

import java.util.Map;
import java.util.WeakHashMap;

import fr.cy.model.agent.Agent;

/**
 * Lightweight registry that associates {@link Agent} instances to
 * {@link AgentProfile} values without modifying the {@code Agent} class.
 */
public final class AgentProfileRegistry {

    private static final Map<Agent, AgentProfile> REGISTRY = new WeakHashMap<>();

    private AgentProfileRegistry() {
        // utility
    }

    /**
     * Associate a profile to an agent. Passing {@code null} for profile sets the
     * {@link AgentProfile#DEFAULT} profile.
     *
     * @param agent   the agent to associate the profile with
     * @param profile the profile to assign, or {@code null} to use the default
     */
    public static void setProfile(Agent agent, AgentProfile profile) {
        if (agent == null)
            return;
        REGISTRY.put(agent, profile == null ? AgentProfile.DEFAULT : profile);
    }

    /**
     * Returns the profile associated to the given agent, or
     * {@link AgentProfile#DEFAULT} when none is registered.
     *
     * @param agent the agent whose profile is requested
     * @return the profile associated to the agent, or {@link AgentProfile#DEFAULT} if not found
     */
    public static AgentProfile getProfile(Agent agent) {
        if (agent == null)
            return AgentProfile.DEFAULT;
        AgentProfile p = REGISTRY.get(agent);
        return p == null ? AgentProfile.DEFAULT : p;
    }

    /**
     * Removes any registered profile for the given agent.
     *
     * @param agent the agent whose profile entry should be removed
     */
    public static void removeProfile(Agent agent) {
        if (agent == null)
            return;
        REGISTRY.remove(agent);
    }
}
