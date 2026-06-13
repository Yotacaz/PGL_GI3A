package fr.cy.model.fire;

/**
 * Centralized configuration for fire simulation parameters.
 */
public class FireConfig {

    /** Base probability of fire spreading to an adjacent element per simulation tick. */
    public static final double BASE_SPREAD_PROBABILITY = 0.2;

    /** Additional spread probability applied when a corridor is narrow. */
    public static final double NARROW_CORRIDOR_PROB_BOOST = 0.2;

    /** Spread probability reduction applied when a corridor is long. */
    public static final double LONG_CORRIDOR_PROB_PENALTY = 0.1;

    /** Width threshold in metres below which a corridor is considered narrow. */
    public static final double NARROW_CORRIDOR_WIDTH_THRESHOLD = 2.0;

    /** Width threshold in metres above which a corridor is considered wide. */
    public static final double WIDE_CORRIDOR_WIDTH_THRESHOLD = 5.0;

    /** Spread rate increase applied when the corridor is narrow. */
    public static final double SPREAD_RATE_BOOST = 0.3;

    /** Spread rate decrease applied when the corridor is wide. */
    public static final double SPREAD_RATE_PENALTY = 0.1;

    /** Minimum allowed spread rate after all modifiers are applied. */
    public static final double MIN_SPREAD_RATE = 0.01;

    /** Length threshold in metres above which a corridor is considered long. */
    public static final double LONG_CORRIDOR_LENGTH_THRESHOLD = 20.0;

    /** Utility class — do not instantiate. */
    private FireConfig() {
    }
}