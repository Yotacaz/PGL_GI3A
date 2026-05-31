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
            simulation.getAgentManager().generateRandomsAgents(30);
        }

        n1.setFire(new Fire(0, 1, 0.01));
        n4.setFire(new Fire(0, 0, 0));

        return simulation;
    }

    public static void main(String[] args) {
        FileManager.saveSimulation(buildDemoScenario());
    }
}