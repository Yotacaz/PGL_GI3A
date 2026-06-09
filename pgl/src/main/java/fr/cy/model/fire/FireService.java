package fr.cy.model.fire;

import java.io.Serializable;
import java.util.Random;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;

/**
 * The {@code FireService} manages the fire life-cycle and propagation
 * logic throughout the simulation graph.
 */
public class FireService implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Random random;

    public FireService() {
        this.random = new Random();
    }

    /**
     * Checks if a graph element is currently affected by fire.
     * 
     * @param element The graph element to check.
     * @return True if the element is on fire.
     */
    public boolean isOnFire(GraphElement element) {
        return element.isOnFire();
    }

    /**
     * Updates the state of all fires in the graph, handling intensity
     * updates and propagation from nodes to edges and vice-versa.
     * * @param graph The simulation graph.
     * 
     * @param tickDuration The duration of the current simulation tick.
     */
    public void updateFires(Graph graph, double tickDuration) {

        // UPDATE NODES
        for (Node node : graph.getNodes()) {
            if (!node.isOnFire())
                continue;

            node.getFire().update(tickDuration);

            if (node.getFire().getSpreadRate() <= 0.0)
                continue;

            for (Edge edge : node.getEdges()) {
                double probability = computeSpreadProbability(edge) * tickDuration;

                if (!edge.isOnFire()) {
                    if (random.nextDouble() < probability) {
                        double dynamicSpreadRate = computeSpreadRate(edge, node.getFire().getSpreadRate());
                        edge.igniteFrom(node, new Fire(1.0, 1.0, dynamicSpreadRate));
                    }
                } else if (!edge.isFullyBurned()) {
                    boolean alreadyBurningFromHere = (node.equals(edge.getStart()) && edge.isBurningFromStart()) ||
                            (node.equals(edge.getEnd()) && edge.isBurningFromEnd());

                    if (!alreadyBurningFromHere && random.nextDouble() < probability) {
                        edge.igniteFrom(node, edge.getFire());
                    }
                }
            }
        }

        // UPDATE EDGES
        for (Edge edge : graph.getEdges()) {
            if (!edge.isOnFire())
                continue;

            edge.getFire().update(tickDuration);
            double distance = edge.getBurnedDistance();

            if (edge.isBurningFromStart() && distance >= edge.getLength()) {
                if (!edge.getEnd().isOnFire()) {
                    edge.getEnd().setFire(new Fire(1.0, 1.0, edge.getFire().getSpreadRate()));
                }
            }

            if (edge.isBurningFromEnd() && distance >= edge.getLength()) {
                if (!edge.getStart().isOnFire()) {
                    edge.getStart().setFire(new Fire(1.0, 1.0, edge.getFire().getSpreadRate()));
                }
            }
        }
    }

    /**
     * Calculates the probability of fire spreading to an edge based on geometry.
     */
    private double computeSpreadProbability(Edge edge) {
        double probability = FireConfig.BASE_SPREAD_PROBABILITY;

        if (edge.getWidth() < FireConfig.NARROW_CORRIDOR_WIDTH_THRESHOLD) {
            probability += FireConfig.NARROW_CORRIDOR_PROB_BOOST;
        }

        if (edge.getLength() > FireConfig.LONG_CORRIDOR_LENGTH_THRESHOLD) {
            probability -= FireConfig.LONG_CORRIDOR_PROB_PENALTY;
        }

        return Math.max(0, Math.min(1, probability));
    }

    /**
     * Calculates the spread rate of fire across an edge, applying environmental
     * modifiers.
     */
    private double computeSpreadRate(Edge edge, double baseSpreadRate) {
        if (baseSpreadRate <= 0.0)
            return 0.0;

        double rate = baseSpreadRate;

        if (edge.getWidth() < FireConfig.NARROW_CORRIDOR_WIDTH_THRESHOLD) {
            rate += FireConfig.SPREAD_RATE_BOOST;
        } else if (edge.getWidth() > FireConfig.WIDE_CORRIDOR_WIDTH_THRESHOLD) {
            rate -= FireConfig.SPREAD_RATE_PENALTY;
        }

        return Math.max(FireConfig.MIN_SPREAD_RATE, rate);
    }
}