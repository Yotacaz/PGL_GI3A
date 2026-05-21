package fr.cy.model;

/**
 * Interface for elements that can induce stress in agents, such as fires or
 * other panicking agents.
 */
public interface StressInducing {
    public float getStressInducingFactor();
}
