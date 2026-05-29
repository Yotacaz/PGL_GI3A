package fr.cy.model.pathfinding;

/**
 * Configuration des paramètres de pathfinding avec Min-Cost Max-Flow.
 *
 * Cette classe permet de configurer les comportements et les coefficients
 * utilisés par l'algorithme de pathfinding.
 *
 * @author GI3A
 * @version 1.0
 */
public class PathfindingConfig {
    
    /** Coefficient de stress appliqué au calcul du coût des arêtes */
    public static final double STRESS_COST_COEFFICIENT = 100.0;

    /** Coefficient de réduction de capacité en cas de congestion */
    public static final double CONGESTION_CAPACITY_REDUCTION = 0.5;

    /** Capacité minimale d'une arête */
    public static final double MIN_EDGE_CAPACITY = 1.0;

    /** Tolérance pour considérer que deux distances sont égales */
    public static final double DISTANCE_EPSILON = 1e-9;

    /** Flux unitaire par défaut pour une demande de chemin */
    public static final double DEFAULT_FLOW_AMOUNT = 1.0;

    /** Pénalité de détour (multiplicateur appliqué au coût en cas de congestion) */
    public static final double DETOUR_PENALTY = 1.2;

    /**
     * Calcule le coût d'une arête en fonction de ses propriétés.
     *
     * @param edgeLength longueur de l'arête
     * @param stressFactor facteur de stress de l'arête
     * @return le coût calculé
     */
    public static double computeEdgeCost(double edgeLength, double stressFactor) {
        return edgeLength + stressFactor * STRESS_COST_COEFFICIENT;
    }

    /**
     * Calcule la capacité effective d'une arête en fonction de sa congestion.
     *
     * @param baseCapacity capacité de base de l'arête
     * @param isCongested true si l'arête est en congestion
     * @return la capacité effective
     */
    public static double computeEffectiveCapacity(double baseCapacity, boolean isCongested) {
        double effectiveCapacity = baseCapacity;
        if (isCongested) {
            effectiveCapacity *= CONGESTION_CAPACITY_REDUCTION;
        }
        return Math.max(effectiveCapacity, MIN_EDGE_CAPACITY);
    }

    /**
     * Applique une pénalité de détour au coût d'un chemin en fonction
     * de la congestion rencontrée.
     *
     * @param baseCost coût de base du chemin
     * @param congestionFactor facteur de congestion (0 à 1)
     * @return le coût avec pénalité appliquée
     */
    public static double applyDetourPenalty(double baseCost, double congestionFactor) {
        return baseCost * (1.0 + (DETOUR_PENALTY - 1.0) * congestionFactor);
    }
}
