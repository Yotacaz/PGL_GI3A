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

        // UPDATE DES NŒUDS
        for (Node node : graph.getNodes()) {
            if (!node.isOnFire())
                continue;

            node.getFire().update();

            for (Edge edge : node.getEdges()) {
                // Calcul de la probabilité que le feu prenne sur cette arête
                double probability = computeSpreadProbability(edge);

                // Si l'arête est totalement intacte
                if (!edge.isOnFire()) {
                    if (random.nextDouble() < probability) {
                        // spreadRate dynamique
                        double dynamicSpreadRate = computeSpreadRate(edge);
                        edge.igniteFrom(node, new Fire(1.0, 1.0, dynamicSpreadRate));
                    }
                }
                // Si l'arête brûle déjà, mais qu'elle n'est pas consumée
                else if (!edge.isFullyBurned()) {
                    // On vérifie que le feu ne vient pas DÉJÀ de ce nœud précis
                    boolean alreadyBurningFromHere = (node.equals(edge.getStart()) && edge.isBurningFromStart()) ||
                            (node.equals(edge.getEnd()) && edge.isBurningFromEnd());

                    // Si le feu vient d'arriver sur ce nœud via un autre chemin,
                    // il "tente" d'allumer un 2ème front de flammes sur l'arête
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

            edge.getFire().update();

            // Si l'arête n'est pas encore 100% cramée, on regarde si un front atteint un
            if (!edge.isFullyBurned()) {
                double distance = edge.getBurnedDistance();

                // Le feu vient du Start et touche le End
                if (edge.isBurningFromStart() && distance >= edge.getLength()) {
                    if (!edge.getEnd().isOnFire()) {
                        edge.getEnd().setFire(new Fire(1.0, 1.0, 0.5));
                    }
                }

                // Le feu vient du End et touche le Start
                if (edge.isBurningFromEnd() && distance >= edge.getLength()) {
                    if (!edge.getStart().isOnFire()) {
                        edge.getStart().setFire(new Fire(1.0, 1.0, 0.5));
                    }
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

    private double computeSpreadRate(Edge edge) {
        // Vitesse de base (ex: 0.2 mètres par tick)
        double rate = 0.2;

        // Effet cheminée : les flammes accélèrent dans les endroits étroits
        if (edge.getWidth() < 2.0) {
            rate += 0.3;
        }
        // Dispersion : les flammes avancent moins vite en ligne droite dans les
        // endroits larges
        else if (edge.getWidth() > 5.0) {
            rate -= 0.1;
        }

        // On s'assure que le feu avance toujours un minimum
        return Math.max(0.1, rate);
    }
}
