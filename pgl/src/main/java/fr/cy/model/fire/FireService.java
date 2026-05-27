package fr.cy.model.fire;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;

/** TODO: VALEURS MAGIQUES: A RETRAVAILLER */
public class FireService {
    private final Random random;

    public FireService() {
        this.random = new Random();
    }

    public boolean isOnFire(GraphElement element) {
        return element.isOnFire();
    }

    public void updateFires(Graph graph) {
        /** Elements qui prendront feu */
        Set<GraphElement> newFires = new HashSet<>();

        /** Update des nodes */
        for (Node node : graph.getNodes()) {
            if (!node.isOnFire()) {
                continue;
            }

            node.getFire().update();

            /** Propagation aux arrêtes */
            for (Edge edge : graph.getAdjacentEdges(node)) {
                if (edge.isOnFire()) {
                    continue;
                }

                double probability = computeSpreadPropability(edge);

                if (random.nextDouble() < probability) {
                    newFires.add(edge);
                }

            }
        }

        /** Update des arrêtes */
        for (Edge edge : graph.getEdges()) {

            if (!edge.isOnFire()) {
                continue;
            }
            edge.getFire().update();

            /** Propagation aux nodes */
            Node start = edge.getStart();
            Node end = edge.getEnd();

            if (!start.isOnFire() &&
                    random.nextDouble() < edge.getFire().getSpreadRate()) {

                newFires.add(start);
            }

            if (!end.isOnFire()
                    &&
                    random.nextDouble() < edge.getFire()
                            .getSpreadRate()) {

                newFires.add(end);
            }
        }

        /** Application des nouveaux feux */
        for (GraphElement graphElement : newFires) {
            /** TEMPORAIRE: A paufiner */
            graphElement.setFire(new Fire(1, 1, 0.2));
        }
    }

    private double computeSpreadPropability(Edge edge) {
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
}
