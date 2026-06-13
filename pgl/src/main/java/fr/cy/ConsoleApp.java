package fr.cy;

import fr.cy.console.ConsoleUI;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.Simulation;

/**
 * Application console pour lancer la simulation sans GUI JavaFX.
 * Construit un scénario de test via Simulation et lance ConsoleUI.
 */
public class ConsoleApp {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     🌐 SIMULATEUR D'ÉVACUATION - VERSION CONSOLE 🌐           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        Simulation simulation = createTestSimulation();
        ConsoleUI console = new ConsoleUI(simulation);
        console.run();
    }

    private static Simulation createTestSimulation() {
        Graph graph = new Graph();

        // Nœuds
        Node n1 = graph.createNode(150, 150);
        Node n2 = graph.createNode(400, 100);
        Node n3 = graph.createNode(600, 250);
        Node n4 = graph.createNode(400, 400);
        Node n5 = graph.createNode(150, 450);

        Node sortie1 = graph.createNode(700, 100);
        Node sortie2 = graph.createNode(700, 450);
        sortie1.setExit(true);
        sortie2.setExit(true);

        // Arêtes
        Edge e1 = graph.createEdge(n1, n2);
        Edge e2 = graph.createEdge(n2, n3);
        Edge e4 = graph.createEdge(n4, n5);
        graph.createEdge(n5, n1);
        graph.createEdge(n2, n4);
        graph.createEdge(n3, sortie1);
        graph.createEdge(n4, sortie2);
        Edge e3 = graph.createEdge(n3, n4);

        System.out.println("✅ Graphe créé: " + graph.getNodes().size() + " nœuds, "
                + graph.getEdges().size() + " arêtes\n");

        Simulation simulation = new Simulation("Simulation Console Test", graph);

        // Agents sur les nœuds via AgentManager
        System.out.println("👥 Ajout d'agents via la simulation...");
        simulation.getAgentManager().generateAgentsOnNode("Agent_n1", n1, 8);
        System.out.println("   ➜ 8 agents sur n1 (congestion: " + String.format("%.0f%%", n1.getCongestion() * 100) + ")");
        simulation.getAgentManager().generateAgentsOnNode("Agent_n2", n2, 10);
        System.out.println("   ➜ 10 agents sur n2 (congestion: " + String.format("%.0f%%", n2.getCongestion() * 100) + ")");
        simulation.getAgentManager().generateAgentsOnNode("Agent_n3", n3, 7);
        System.out.println("   ➜ 7 agents sur n3 (congestion: " + String.format("%.0f%%", n3.getCongestion() * 100) + ")");
        simulation.getAgentManager().generateAgentsOnNode("Agent_n4", n4, 9);
        System.out.println("   ➜ 9 agents sur n4 (congestion: " + String.format("%.0f%%", n4.getCongestion() * 100) + ")");
        simulation.getAgentManager().generateAgentsOnNode("Agent_n5", n5, 6);
        System.out.println("   ➜ 6 agents sur n5 (congestion: " + String.format("%.0f%%", n5.getCongestion() * 100) + ")");

        // Agents sur les arêtes via AgentManager
        for (int i = 0; i < 12; i++) simulation.getAgentManager().generateAgentOnEdge("Agent_e1", e1, 0.5);
        System.out.println("   ➜ 12 agents sur e1 (congestion: " + String.format("%.0f%%", e1.getCongestion() * 100) + ")");
        for (int i = 0; i < 15; i++) simulation.getAgentManager().generateAgentOnEdge("Agent_e2", e2, 0.5);
        System.out.println("   ➜ 15 agents sur e2 (congestion: " + String.format("%.0f%%", e2.getCongestion() * 100) + ")");
        for (int i = 0; i < 10; i++) simulation.getAgentManager().generateAgentOnEdge("Agent_e3", e3, 0.5);
        System.out.println("   ➜ 10 agents sur e3 (congestion: " + String.format("%.0f%%", e3.getCongestion() * 100) + ")");
        for (int i = 0; i < 11; i++) simulation.getAgentManager().generateAgentOnEdge("Agent_e4", e4, 0.5);
        System.out.println("   ➜ 11 agents sur e4 (congestion: " + String.format("%.0f%%", e4.getCongestion() * 100) + ")");

        System.out.println("\n✅ Total: " + simulation.getAgentManager().getAgentsToEvacuate().size() + " agents\n");

        // Feux
        System.out.println("🔥 Mise en feu...");
        n3.setFire(new Fire(0.7, 0.5, 0.3));
        System.out.println("   Nœud #" + n3.getId() + " EN FEU (intensité: 0.70)");
        n4.setFire(new Fire(0.5, 0.4, 0.25));
        System.out.println("   Nœud #" + n4.getId() + " EN FEU (intensité: 0.50)");
        e3.setFire(new Fire(0.6, 0.3, 0.2));
        System.out.println("   Arête #" + e3.getId() + " EN FEU (intensité: 0.60)");
        e4.setFire(new Fire(0.4, 0.3, 0.15));
        System.out.println("   Arête #" + e4.getId() + " EN FEU (intensité: 0.40)\n");

        return simulation;
    }
}
