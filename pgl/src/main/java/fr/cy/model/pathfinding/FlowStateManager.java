package fr.cy.model.pathfinding;

import java.util.HashMap;
import java.util.Map;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Gestionnaire de l'état de flux dans le réseau pour le suivi de la congestion.
 *
 * Cette classe maintient les informations de flux pour chaque arête et nœud,
 * permettant au système de pathfinding d'adapter dynamiquement les chemins
 * en fonction de la congestion actuelle.
 *
 * @author GI3A
 * @version 1.0
 */
public class FlowStateManager {
    private final Graph graph;
    private final Map<Edge, Double> edgeFlows;
    private final Map<Node, Double> nodeFlows;

    /**
     * Constructeur du gestionnaire d'état de flux.
     *
     * @param graph le graphe d'agents
     */
    public FlowStateManager(Graph graph) {
        this.graph = graph;
        this.edgeFlows = new HashMap<>();
        this.nodeFlows = new HashMap<>();
        
        initializeFlows();
    }

    /**
     * Initialise les flux à zéro pour tous les nœuds et arêtes.
     */
    private void initializeFlows() {
        for (Edge edge : graph.getEdges()) {
            edgeFlows.put(edge, 0.0);
        }
        for (Node node : graph.getNodes()) {
            nodeFlows.put(node, 0.0);
        }
    }

    /**
     * Ajoute du flux à une arête.
     *
     * @param edge l'arête cible
     * @param flow montant de flux à ajouter
     */
    public void addEdgeFlow(Edge edge, double flow) {
        edgeFlows.put(edge, edgeFlows.getOrDefault(edge, 0.0) + flow);
    }

    /**
     * Ajoute du flux à un nœud.
     *
     * @param node le nœud cible
     * @param flow montant de flux à ajouter
     */
    public void addNodeFlow(Node node, double flow) {
        nodeFlows.put(node, nodeFlows.getOrDefault(node, 0.0) + flow);
    }

    /**
     * Retourne le flux actuel d'une arête.
     *
     * @param edge l'arête
     * @return le flux de l'arête
     */
    public double getEdgeFlow(Edge edge) {
        return edgeFlows.getOrDefault(edge, 0.0);
    }

    /**
     * Retourne le flux actuel d'un nœud.
     *
     * @param node le nœud
     * @return le flux du nœud
     */
    public double getNodeFlow(Node node) {
        return nodeFlows.getOrDefault(node, 0.0);
    }

    /**
     * Calcule le taux d'utilisation d'une arête (flux / capacité).
     *
     * @param edge l'arête
     * @return le taux d'utilisation entre 0 et 1
     */
    public double getEdgeUtilizationRate(Edge edge) {
        double capacity = edge.getWidth();
        if (capacity <= 0) {
            return 0.0;
        }
        double flow = edgeFlows.getOrDefault(edge, 0.0);
        return Math.min(flow / capacity, 1.0);
    }

    /**
     * Calcule le taux d'utilisation d'un nœud (flux / capacité).
     *
     * @param node le nœud
     * @return le taux d'utilisation entre 0 et 1
     */
    public double getNodeUtilizationRate(Node node) {
        double capacity = node.getCapacity();
        if (capacity <= 0) {
            return 0.0;
        }
        double flow = nodeFlows.getOrDefault(node, 0.0);
        return Math.min(flow / capacity, 1.0);
    }

    /**
     * Réinitialise tous les flux à zéro.
     */
    public void resetFlows() {
        initializeFlows();
    }

    /**
     * Retourne tous les flux d'arête.
     *
     * @return la carte des flux d'arête
     */
    public Map<Edge, Double> getEdgeFlows() {
        return new HashMap<>(edgeFlows);
    }

    /**
     * Retourne tous les flux de nœud.
     *
     * @return la carte des flux de nœud
     */
    public Map<Node, Double> getNodeFlows() {
        return new HashMap<>(nodeFlows);
    }

    /**
     * Calcule le coût total du flux dans le réseau.
     *
     * @return le coût total
     */
    public double calculateTotalCost() {
        double totalCost = 0.0;

        for (Map.Entry<Edge, Double> entry : edgeFlows.entrySet()) {
            Edge edge = entry.getKey();
            double flow = entry.getValue();
            double cost = edge.getLength() + edge.getStressInducingImpact() * 100;
            totalCost += flow * cost;
        }

        return totalCost;
    }
}
