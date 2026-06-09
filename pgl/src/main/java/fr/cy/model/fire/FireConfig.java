package fr.cy.model.fire;

/**
 * Centralized configuration for fire simulation parameters.
 */
public class FireConfig {
    public static final double BASE_SPREAD_PROBABILITY = 0.2;
    public static final double NARROW_CORRIDOR_PROB_BOOST = 0.2;
    public static final double LONG_CORRIDOR_PROB_PENALTY = 0.1;

    public static final double NARROW_CORRIDOR_WIDTH_THRESHOLD = 2.0;
    public static final double WIDE_CORRIDOR_WIDTH_THRESHOLD = 5.0;

    public static final double SPREAD_RATE_BOOST = 0.3;
    public static final double SPREAD_RATE_PENALTY = 0.1;
    public static final double MIN_SPREAD_RATE = 0.01;

    public static final double LONG_CORRIDOR_LENGTH_THRESHOLD = 20.0;
}