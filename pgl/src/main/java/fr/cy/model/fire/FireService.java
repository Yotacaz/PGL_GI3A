package fr.cy.model.fire;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;

/** TODO: VALEURS MAGIQUES: A RETRAVAILLER */
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

        // UPDATE DES NŒUDS
        for (Node node : graph.getNodes()) {
            if (!node.isOnFire())
                continue;

            // 1. On met à jour l'intensité locale du feu, même s'il ne se propage pas
            node.getFire().update(tickDuration);

            // 2. Coupe-circuit : si le feu n'a pas de force de propagation, on s'arrête là
            // pour ce nœud
            if (node.getFire().getSpreadRate() <= 0.0) {
                continue;
            }

            for (Edge edge : node.getEdges()) {
                // On pondère la probabilité en fonction du tickDuration (Lissage 60 FPS)
                double probability = computeSpreadProbability(edge) * tickDuration;

                // Si l'arête est totalement intacte
                if (!edge.isOnFire()) {
                    if (random.nextDouble() < probability) {
                        double dynamicSpreadRate = computeSpreadRate(edge, node.getFire().getSpreadRate());
                        edge.igniteFrom(node, new Fire(1.0, 1.0, dynamicSpreadRate));
                    }
                }
                // Si l'arête brûle déjà, mais qu'elle n'est pas consumée
                else if (!edge.isFullyBurned()) {
                    boolean alreadyBurningFromHere = (node.equals(edge.getStart()) && edge.isBurningFromStart()) ||
                            (node.equals(edge.getEnd()) && edge.isBurningFromEnd());

                    if (!alreadyBurningFromHere && random.nextDouble() < probability) {
                        edge.igniteFrom(node, edge.getFire());
                    }
                }
            }
        }

        // UPDATE DES ARÊTES
        for (Edge edge : graph.getEdges()) {
            if (!edge.isOnFire())
                continue;

            // 1. Le feu avance sur le couloir
            edge.getFire().update(tickDuration);

            double distance = edge.getBurnedDistance();

            // 2. Le front de flamme venant du Start atteint le End
            if (edge.isBurningFromStart() && distance >= edge.getLength()) {
                if (!edge.getEnd().isOnFire()) {
                    edge.getEnd().setFire(new Fire(1.0, 1.0, edge.getFire().getSpreadRate()));
                }
            }

            // 3. Le front de flamme venant du End atteint le Start
            if (edge.isBurningFromEnd() && distance >= edge.getLength()) {
                if (!edge.getStart().isOnFire()) {
                    edge.getStart().setFire(new Fire(1.0, 1.0, edge.getFire().getSpreadRate()));
                }
            }
        }
    }

    private double computeSpreadProbability(Edge edge) {
        double probability = 0.2;

        /** Couloirs étroits */
        if (edge.getWidth() < 2) {
            probability += 0.2;
        }

        /** Couloirs longs */
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

        // Effet cheminée : les flammes accélèrent dans les endroits étroits
        if (edge.getWidth() < 2.0) {
            rate += 0.3;
        }
        // Dispersion : les flammes avancent moins vite dans les endroits larges
        else if (edge.getWidth() > 5.0) {
            rate -= 0.1;
        }

        // On ne met un plancher (0.01) QUE si le feu était censé avancer
        return Math.max(0.01, rate);
    }
}
