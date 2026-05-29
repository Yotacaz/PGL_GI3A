package fr.cy.model.pathfinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;

/**
 * Implémentation de l'algorithme Min-Cost Max-Flow utilisant l'approche
 * des chemins courts successifs (Successive Shortest Paths).
 *
 * Cet algorithme trouve le flux maximum dans un graphe tout en minimisant
 * le coût total. Il est idéal pour gérer la congestion et les coûts variables
 * du réseau d'agents.
 *
 * @author GI3A
 * @version 1.0
 */
public class MinCostMaxFlow {
    private final Graph graph;
    private final Map<Node, List<FlowEdge>> flowGraph;
    private final Map<Node, Double> potentials;

    /**
     * Constructeur du calcul Min-Cost Max-Flow.
     *
     * @param graph le graphe d'agents
     */
    public MinCostMaxFlow(Graph graph) {
        this.graph = graph;
        this.flowGraph = new HashMap<>();
        this.potentials = new HashMap<>();

        initializePotentials();
    }

    /**
     * Initialise les potentiels à zéro pour tous les nœuds.
     */
    private void initializePotentials() {
        for (Node node : graph.getNodes()) {
            potentials.put(node, 0.0);
            flowGraph.put(node, new ArrayList<>());
        }
    }

    /**
     * Ajoute une arête au graphe de flux et son inverse.
     *
     * @param from     nœud source
     * @param to       nœud destination
     * @param capacity capacité de l'arête
     * @param cost     coût unitaire
     */
    public void addFlowEdge(Node from, Node to, double capacity, double cost) {
        FlowEdge forward = new FlowEdge(from, to, capacity, cost);
        FlowEdge backward = new FlowEdge(to, from, 0, -cost); // Arête inverse avec capacité 0

        flowGraph.get(from).add(forward);
        flowGraph.get(to).add(backward);
    }

    /**
     * Construit le graphe de flux à partir du graphe réel avec congestion.
     *
     * @param startNode nœud de départ
     * @param goalNode  nœud d'arrivée
     * @param flowAmount montant de flux à envoyer
     */
    public void buildFlowGraph(Node startNode, Node goalNode, double flowAmount) {
        // Réinitialiser le graphe de flux
        flowGraph.clear();
        initializePotentials();

        // Ajouter les arêtes du graphe avec leurs capacités et coûts
        for (Node node : graph.getNodes()) {
            for (fr.cy.model.graph.element.Edge edge : graph.getAdjacentEdges(node)) {
                Node neighbor = edge.getOppositeNode(node);
                if (neighbor != null) {
                    double cost = computeEdgeCost(edge);
                    double capacity = computeEdgeCapacity(edge);
                    
                    addFlowEdge(node, neighbor, capacity, cost);
                }
            }
        }
    }

    /**
     * Calcule le coût d'une arête en fonction de sa longueur et son facteur de stress.
     *
     * @param edge l'arête du graphe
     * @return le coût de l'arête
     */
    private double computeEdgeCost(fr.cy.model.graph.element.Edge edge) {
        return PathfindingConfig.computeEdgeCost(edge.getLength(), edge.getStressInducingFactor());
    }

    /**
     * Calcule la capacité d'une arête en fonction de sa largeur et ses propriétés.
     *
     * @param edge l'arête du graphe
     * @return la capacité de l'arête
     */
    private double computeEdgeCapacity(fr.cy.model.graph.element.Edge edge) {
        return PathfindingConfig.computeEffectiveCapacity(edge.getWidth(), edge.isCongested());
    }

    /**
     * Trouve le chemin le moins coûteux du nœud source au nœud destination
     * en utilisant Dijkstra avec potentiels.
     *
     * @param source      nœud source
     * @param destination nœud destination
     * @return liste des nœuds du chemin, ou liste vide si aucun chemin
     */
    public List<Node> findMinCostPath(Node source, Node destination) {
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previous = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();

        // Initialiser les distances
        for (Node node : flowGraph.keySet()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);
        queue.add(new NodeDistance(source, 0));

        // Dijkstra avec potentiels
        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            Node u = current.node();
            double dist = current.distance();

            if (dist > distances.get(u)) {
                continue;
            }

            if (u.equals(destination)) {
                break;
            }

            // Vérifier tous les voisins
            if (flowGraph.containsKey(u)) {
                for (FlowEdge edge : flowGraph.get(u)) {
                    if (edge.getResidualCapacity() > 0) {
                        Node v = edge.getTo();
                        double weight = edge.getCost() + potentials.get(u) - potentials.get(v);
                        double newDist = distances.get(u) + weight;

                        if (newDist < distances.get(v)) {
                            distances.put(v, newDist);
                            previous.put(v, u);
                            queue.add(new NodeDistance(v, newDist));
                        }
                    }
                }
            }
        }

        // Reconstruire le chemin
        if (!distances.get(destination).isInfinite()) {
            // Mettre à jour les potentiels
            for (Node node : distances.keySet()) {
                potentials.put(node, potentials.get(node) + distances.get(node));
            }

            // Reconstruire le chemin
            List<Node> path = new ArrayList<>();
            Node current = destination;
            while (current != null) {
                path.add(current);
                current = previous.get(current);
            }
            Collections.reverse(path);
            return path;
        }

        return new ArrayList<>();
    }

    /**
     * Calcule le flux maximum avec coût minimum du source à la destination.
     *
     * @param source      nœud source
     * @param destination nœud destination
     * @param maxFlow     flux maximum à envoyer
     * @return liste des nœuds du chemin optimal
     */
    public List<Node> computeMinCostMaxFlow(Node source, Node destination, double maxFlow) {
        double totalFlow = 0;
        List<Node> lastPath = new ArrayList<>();

        while (totalFlow < maxFlow) {
            // Trouver le chemin le moins coûteux
            List<Node> path = findMinCostPath(source, destination);

            if (path.isEmpty()) {
                break;
            }

            // Trouver la capacité minimum le long du chemin
            double bottleneck = Double.POSITIVE_INFINITY;
            for (int i = 0; i < path.size() - 1; i++) {
                Node u = path.get(i);
                Node v = path.get(i + 1);

                for (FlowEdge edge : flowGraph.get(u)) {
                    if (edge.getTo().equals(v)) {
                        bottleneck = Math.min(bottleneck, edge.getResidualCapacity());
                        break;
                    }
                }
            }

            // Limiter le flux à envoyer
            double flowToSend = Math.min(bottleneck, maxFlow - totalFlow);

            // Augmenter le flux le long du chemin
            for (int i = 0; i < path.size() - 1; i++) {
                Node u = path.get(i);
                Node v = path.get(i + 1);

                // Trouver et mettre à jour l'arête forward
                for (FlowEdge edge : flowGraph.get(u)) {
                    if (edge.getTo().equals(v)) {
                        edge.addFlow(flowToSend);
                        break;
                    }
                }

                // Mettre à jour l'arête backward
                for (FlowEdge edge : flowGraph.get(v)) {
                    if (edge.getTo().equals(u)) {
                        edge.addFlow(-flowToSend);
                        break;
                    }
                }
            }

            totalFlow += flowToSend;
            lastPath = new ArrayList<>(path);
        }

        return lastPath;
    }

    /**
     * Retourne le graphe de flux.
     *
     * @return la carte du graphe de flux
     */
    public Map<Node, List<FlowEdge>> getFlowGraph() {
        return flowGraph;
    }

    /**
     * Retourne les potentiels calculés.
     *
     * @return la carte des potentiels par nœud
     */
    public Map<Node, Double> getPotentials() {
        return potentials;
    }
}
