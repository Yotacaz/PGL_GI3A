package fr.cy.util;

import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;

/**
 * Factory permettant de construire différents scénarios de test
 * avec des graphes spatiaux, des incendies et des agents pré-positionnés.
 */
public class ScenarioBuilder {

    public static Simulation buildDemoScenario() {
        Graph graph = new Graph();

        Node n1 = graph.createNode(150, 150);
        Node n2 = graph.createNode(400, 100);
        Node n3 = graph.createNode(600, 250);
        Node n4 = graph.createNode(400, 400);
        Node n5 = graph.createNode(150, 450);

        Node sortie1 = graph.createNode(700, 100);
        Node sortie2 = graph.createNode(700, 450);
        sortie1.setExit(true);
        sortie2.setExit(true);

        graph.createEdge(n1, n2);
        graph.createEdge(n2, n3);
        graph.createEdge(n3, n4);
        graph.createEdge(n4, n5);
        graph.createEdge(n5, n1);
        graph.createEdge(n2, n4);
        graph.createEdge(n3, sortie1);
        graph.createEdge(n4, sortie2);

        Simulation simulation = new Simulation("Demo", graph);

        if (simulation.getAgentManager() != null) {
            // simulation.getAgentManager().generateAgentOnNode("Agent1", n1); //for testing
            simulation.getAgentManager().generateRandomsAgents(30);
        }

        n1.setFire(new Fire(0, 1, 0.01));
        n4.setFire(new Fire(0, 0, 0));

        return simulation;
    }

    public static Simulation buildComplexScenario() {
        Graph graph = new Graph();

        // 1. Création des Nœuds (Hall, Bureaux, Couloirs)
        Node hall = graph.createNode(400, 300); // Centre
        Node bureauA = graph.createNode(100, 100);
        Node bureauB = graph.createNode(100, 500);
        Node corridorTop = graph.createNode(400, 100);
        Node corridorBottom = graph.createNode(400, 500);
        Node officeZone = graph.createNode(650, 300);

        // 2. Création des sorties de secours (aux extrémités)
        Node sortieNord = graph.createNode(400, 0);
        Node sortieSud = graph.createNode(400, 600);
        sortieNord.setExit(true);
        sortieSud.setExit(true);

        // 3. Création des arêtes (Le maillage du bâtiment)
        graph.createEdge(bureauA, corridorTop);
        graph.createEdge(bureauB, corridorBottom);
        graph.createEdge(corridorTop, hall);
        graph.createEdge(corridorBottom, hall);
        graph.createEdge(hall, officeZone);
        graph.createEdge(corridorTop, sortieNord); // Sortie Nord
        graph.createEdge(corridorBottom, sortieSud); // Sortie Sud
        graph.createEdge(hall, officeZone);

        Simulation simulation = new Simulation("Labyrinthe en Flammes", graph);

        // 4. Peuplement : beaucoup d'agents concentrés dans les bureaux
        if (simulation.getAgentManager() != null) {
            // simulation.getAgentManager().generateAgentsOnNode("Agent", bureauA, 1);
            simulation.getAgentManager().generateAgentsOnNode("Agent", bureauA, 40);
            simulation.getAgentManager().generateAgentsOnNode("Agent", bureauB, 40);
            simulation.getAgentManager().generateAgentsOnNode("Agent", officeZone, 20);
        }

        // 5. Scénario catastrophe :
        // Le hall principal prend feu, coupant la communication entre les bureaux et
        // les sorties !
        hall.setFire(new Fire(0, 0.8, 0.05)); // Feu intense et propagation rapide

        // Un petit feu débutant dans un couloir pour compliquer le pathfinding
        corridorTop.setFire(new Fire(0, 0.2, 0.02));

        return simulation;
    }

    public static void main(String[] args) {
        FileManager.saveSimulation(buildDemoScenario());
    }
}