package fr.cy.model.pathfinding;

import java.util.List;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;

/**
 * Gestionnaire centralisé du pathfinding utilisant Min-Cost Max-Flow.
 *
 * Cette classe fournit une interface simple pour accéder au système de pathfinding
 * et gère la configuration de l'algorithme.
 *
 * @author GI3A
 * @version 1.0
 */
public class PathfindingManager {
    private final PathFinder pathFinder;
    private final FlowBasedPathfinding flowBasedPathfinding;
    private final FlowStateManager flowStateManager;

    /**
     * Constructeur du gestionnaire de pathfinding.
     *
     * @param graph le graphe d'agents
     */
    public PathfindingManager(Graph graph) {
        this.pathFinder = new PathFinder(graph);
        this.flowBasedPathfinding = new FlowBasedPathfinding(graph);
        this.flowStateManager = new FlowStateManager(graph);
    }

    /**
     * Calcule le chemin le plus court (avec coût minimum) entre deux nœuds.
     *
     * @param start le nœud de départ
     * @param goal  le nœud d'arrivée
     * @return liste des nœuds du chemin
     */
    public List<Node> findShortestPath(Node start, Node goal) {
        return pathFinder.shortestPath(start, goal);
    }

    /**
     * Calcule le chemin le plus court avec gestion du flux.
     *
     * @param start le nœud de départ
     * @param goal  le nœud d'arrivée
     * @return liste des nœuds du chemin
     */
    public List<Node> findFlowBasedPath(Node start, Node goal) {
        return flowBasedPathfinding.findPath(start, goal);
    }

    /**
     * Enregistre un flux sur une arête pour le suivi de la congestion.
     *
     * @param node le nœud
     * @param flow le flux à enregistrer
     */
    public void recordNodeFlow(Node node, double flow) {
        flowStateManager.addNodeFlow(node, flow);
    }

    /**
     * Retourne le gestionnaire d'état de flux.
     *
     * @return l'instance de FlowStateManager
     */
    public FlowStateManager getFlowStateManager() {
        return flowStateManager;
    }

    /**
     * Retourne le gestionnaire de pathfinding basé sur le flux.
     *
     * @return l'instance de FlowBasedPathfinding
     */
    public FlowBasedPathfinding getFlowBasedPathfinding() {
        return flowBasedPathfinding;
    }

    /**
     * Retourne le gestionnaire de pathfinding classique.
     *
     * @return l'instance de PathFinder
     */
    public PathFinder getPathFinder() {
        return pathFinder;
    }

    /**
     * Réinitialise l'état du flux pour une nouvelle itération de simulation.
     */
    public void resetFlowState() {
        flowStateManager.resetFlows();
    }

    /**
     * Retourne le coût total du flux dans le réseau.
     *
     * @return le coût total calculé
     */
    public double getTotalFlowCost() {
        return flowStateManager.calculateTotalCost();
    }
}
