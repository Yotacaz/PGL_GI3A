package fr.cy.model.agent.properties;

/**
 * Defines visual/semantic profiles for agents used by the UI and generation
 * helpers.
 *
 * <p>Profiles are intentionally lightweight: they only provide a label and are
 * meant to be associated to {@code Agent} instances via a registry to avoid
 * changing the core {@code Agent} model.</p>
 */
public enum AgentProfile {
    /** Default profile — renders as the usual circular agent. */
    DEFAULT("default"),
    /** Elderly profile — rendered as a triangle. */
    ELDERLY("elderly"),
    /** Tourist profile — rendered as a square. */
    TOURIST("tourist");

    private final String label;

    AgentProfile(String label) {
        this.label = label;
    }

    /**
     * Returns the human-friendly label for this profile.
     *
     * @return the label string identifying this profile
     */
    public String getLabel() {
        return label;
    }
}
