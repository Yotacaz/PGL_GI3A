package fr.cy;

import fr.cy.console.ConsoleUI;
import fr.cy.model.agent.Agent;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;

/**
 * Application console pour visualiser le graphe sans GUI JavaFX.
 * Lance une interface interactive en ligne de commande.
 */
public class ConsoleApp {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║       🌐 VISUALISEUR DE GRAPHE - VERSION CONSOLE 🌐           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Créer un graphe de test
        Graph graph = createTestGraph();

        // Lancer l'interface console
        ConsoleUI console = new ConsoleUI(graph);
        console.run();
    }

    /**
     * Crée un graphe de test avec des nœuds et des arêtes.
     */
    private static Graph createTestGraph() {
        Graph graph = new Graph();

        // Créer les nœuds
        Node n1 = graph.createNode(150, 150);
        Node n2 = graph.createNode(400, 100);
        Node n3 = graph.createNode(600, 250);
        Node n4 = graph.createNode(400, 400);
        Node n5 = graph.createNode(150, 450);

        Node sortie1 = graph.createNode(700, 100);
        Node sortie2 = graph.createNode(700, 450);
        sortie1.setExit(true);
        sortie2.setExit(true);

        // Créer les arêtes
        Edge e1 = graph.createEdge(n1, n2);
        Edge e2 = graph.createEdge(n2, n3);
        Edge e4 = graph.createEdge(n4, n5);
        graph.createEdge(n5, n1);
        graph.createEdge(n2, n4);
        graph.createEdge(n3, sortie1);
        graph.createEdge(n4, sortie2);
        Edge e3 = graph.createEdge(n3, n4);

        System.out.println("✅ Graphe de test créé avec " + graph.getNodes().size() + " nœuds et "
                + graph.getEdges().size() + " arêtes\n");

        // ===== AJOUTER BEAUCOUP D'AGENTS =====
        System.out.println("👥 Ajout d'agents pour créer de la congestion...\n");

        int agentCount = 0;

        // Agents sur les nœuds (beaucoup de congestion)
        System.out.println("   Nœud n1 (150, 150):");
        for (int i = 0; i < 8; i++) {
            Agent agent = new Agent("Agent_n1_" + i, 5, 0.5, 0.5);
            n1.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 8 agents ajoutés (congestion: " + String.format("%.0f%%", n1.getCongestion() * 100) + ")");

        System.out.println("   Nœud n2 (400, 100):");
        for (int i = 0; i < 10; i++) {
            Agent agent = new Agent("Agent_n2_" + i, 6, 0.4, 0.6);
            n2.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 10 agents ajoutés (congestion: " + String.format("%.0f%%", n2.getCongestion() * 100) + ")");

        System.out.println("   Nœud n3 (600, 250):");
        for (int i = 0; i < 7; i++) {
            Agent agent = new Agent("Agent_n3_" + i, 5, 0.3, 0.7);
            n3.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 7 agents ajoutés (congestion: " + String.format("%.0f%%", n3.getCongestion() * 100) + ")");

        System.out.println("   Nœud n4 (400, 400):");
        for (int i = 0; i < 9; i++) {
            Agent agent = new Agent("Agent_n4_" + i, 4, 0.6, 0.4);
            n4.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 9 agents ajoutés (congestion: " + String.format("%.0f%%", n4.getCongestion() * 100) + ")");

        System.out.println("   Nœud n5 (150, 450):");
        for (int i = 0; i < 6; i++) {
            Agent agent = new Agent("Agent_n5_" + i, 5, 0.5, 0.5);
            n5.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 6 agents ajoutés (congestion: " + String.format("%.0f%%", n5.getCongestion() * 100) + ")");

        // Agents sur les arêtes
        System.out.println("\n   Arête e1 (n1 → n2):");
        for (int i = 0; i < 12; i++) {
            Agent agent = new Agent("Agent_e1_" + i, 5, 0.5, 0.5);
            e1.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 12 agents ajoutés (congestion: " + String.format("%.0f%%", e1.getCongestion() * 100) + ")");

        System.out.println("   Arête e2 (n2 → n3):");
        for (int i = 0; i < 15; i++) {
            Agent agent = new Agent("Agent_e2_" + i, 6, 0.4, 0.6);
            e2.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 15 agents ajoutés (congestion: " + String.format("%.0f%%", e2.getCongestion() * 100) + ")");

        System.out.println("   Arête e3 (n3 → n4):");
        for (int i = 0; i < 10; i++) {
            Agent agent = new Agent("Agent_e3_" + i, 5, 0.3, 0.7);
            e3.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 10 agents ajoutés (congestion: " + String.format("%.0f%%", e3.getCongestion() * 100) + ")");

        System.out.println("   Arête e4 (n4 → n5):");
        for (int i = 0; i < 11; i++) {
            Agent agent = new Agent("Agent_e4_" + i, 4, 0.6, 0.4);
            e4.addAgent(agent);
            agentCount++;
        }
        System.out.println(
                "      ➜ 11 agents ajoutés (congestion: " + String.format("%.0f%%", e4.getCongestion() * 100) + ")");

        System.out.println("\n✅ Total: " + agentCount + " agents créés!\n");

        // ===== AJOUTER DU FEU =====
        System.out.println("🔥 Mise en feu...");

        // Feu sur le nœud n3
        Fire fire1 = new Fire(0.7, 0.5, 0.3);
        n3.setFire(fire1);
        System.out.println(
                "   Nœud #" + n3.getId() + " EN FEU (intensité: " + String.format("%.2f", fire1.getIntensity()) + ")");

        // Feu sur le nœud n4
        Fire fire2 = new Fire(0.5, 0.4, 0.25);
        n4.setFire(fire2);
        System.out.println(
                "   Nœud #" + n4.getId() + " EN FEU (intensité: " + String.format("%.2f", fire2.getIntensity()) + ")");

        // Feu sur l'arête e3 (n3 -> n4)
        Fire fire3 = new Fire(0.6, 0.3, 0.2);
        e3.setFire(fire3);
        System.out.println("   Arête #" + e3.getId() + " (" + e3.getStart().getId() + " → " + e3.getEnd().getId()
                + ") EN FEU (intensité: " + String.format("%.2f", fire3.getIntensity()) + ")");

        // Feu sur l'arête e4 (n4 -> n5)
        Fire fire4 = new Fire(0.4, 0.3, 0.15);
        e4.setFire(fire4);
        System.out.println("   Arête #" + e4.getId() + " (" + e4.getStart().getId() + " → " + e4.getEnd().getId()
                + ") EN FEU (intensité: " + String.format("%.2f", fire4.getIntensity()) + ")\n");

        return graph;
    }
}
