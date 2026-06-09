package fr.cy.model.fire;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;

/** TODO: MAGIC VALUES: REWORK */
public class FireService implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Random random;

    public FireService() {
        this.random = new Random();
    }

    public boolean isOnFire(GraphElement element) {
        return element.isOnFire();
    }

    public void updateFires(Graph graph, double tickDuration) {

        // UPDATE NODES
        for (Node node : graph.getNodes()) {
            if (!node.isOnFire())
                continue;

            // 1. Update the local fire intensity even if it does not spread
            node.getFire().update(tickDuration);

            // 2. Short-circuit: if the fire has no spread strength, stop here
            // for this node
            if (node.getFire().getSpreadRate() <= 0.0) {
                continue;
            }

            for (Edge edge : node.getEdges()) {
                // Weight the probability according to tickDuration (60 FPS smoothing)
                double probability = computeSpreadProbability(edge) * tickDuration;

                // If the edge is completely intact
                if (!edge.isOnFire()) {
                    if (random.nextDouble() < probability) {
                        double dynamicSpreadRate = computeSpreadRate(edge, node.getFire().getSpreadRate());
                        edge.igniteFrom(node, new Fire(1.0, 1.0, dynamicSpreadRate));
                    }
                }
                // If the edge is already burning but not fully consumed
                else if (!edge.isFullyBurned()) {
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

            // 1. Fire advances along the corridor
            edge.getFire().update(tickDuration);

            double distance = edge.getBurnedDistance();

            // 2. Flame front coming from Start reaches End
            if (edge.isBurningFromStart() && distance >= edge.getLength()) {
                if (!edge.getEnd().isOnFire()) {
                    edge.getEnd().setFire(new Fire(1.0, 1.0, edge.getFire().getSpreadRate()));
                }
            }

            // 3. Flame front coming from End reaches Start
            if (edge.isBurningFromEnd() && distance >= edge.getLength()) {
                if (!edge.getStart().isOnFire()) {
                    edge.getStart().setFire(new Fire(1.0, 1.0, edge.getFire().getSpreadRate()));
                }
            }
        }
    }

    private double computeSpreadProbability(Edge edge) {
        double probability = 0.2;

        /** Narrow corridors */
        if (edge.getWidth() < 2) {
            probability += 0.2;
        }

        /** Long corridors */
        if (edge.getLength() > 20) {
            probability -= 0.1;
        }

        return Math.max(0, Math.min(1, probability));
    }

    private double computeSpreadRate(Edge edge, double baseSpreadRate) {
        if (baseSpreadRate <= 0.0) {
            return 0.0;
        }

        double rate = baseSpreadRate;

        // Chimney effect: flames speed up in narrow areas
        if (edge.getWidth() < 2.0) {
            rate += 0.3;
        }
        // Dispersion: flames advance more slowly in wide areas
        else if (edge.getWidth() > 5.0) {
            rate -= 0.1;
        }

        // Only apply a floor (0.01) if the fire was supposed to advance
        return Math.max(0.01, rate);
    }
}
