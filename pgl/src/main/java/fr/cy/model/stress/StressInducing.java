package fr.cy.model.stress;

/**
 * Interface for elements that can induce stress in agents, such as fires or
 * other panicking agents.
 */
public interface StressInducing {
    /**
     * Calculate the stress-inducing factor of this element, which is a value
     * between 0 and 1 representing how much stress it induces in agents.
     * It does not include stress of neighboring elements.
     *
     * @return the stress-inducing factor of this element
     */
    public double getStressInducingFactor();
}
