package fr.cy.model.pathfinding;

import java.util.List;

import fr.cy.model.graph.element.Node;

/**
 * Interface pour les stratégies de sélection de chemin.
 *
 * Permet aux agents d'avoir différents comportements lors du choix de leurs chemins,
 * notamment :
 * - Aversion pour les zones densément peuplées
 * - Recherche de zones aérées
 * - Prise de priorité sur les autres agents
 *
 * @author GI3A
 * @version 1.0
 */
public interface PathSelectionStrategy {

    /**
     * Sélectionne un chemin selon la stratégie de l'agent.
     *
     * @param startNode le nœud de départ
     * @param goalNode  le nœud d'arrivée
     * @param availablePaths liste des chemins possibles
     * @return le chemin sélectionné
     */
    List<Node> selectPath(Node startNode, Node goalNode, List<List<Node>> availablePaths);
}
