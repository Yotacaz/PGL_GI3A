package fr.cy.example;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentGenerator;
import fr.cy.model.agent.AgentManager;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.behaviour.decisions.NodeDecisionContextProvider;
import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.simulation.SimulationSettings;

/**
 * Test class to demonstrate and verify the reset() and setInitialState() functionality.
 */
public class AgentManagerResetTest {

    public static void main(String[] args) {
        System.out.println("=== AgentManager Reset Functionality Test ===\n");

        // Create a simple graph with nodes and edges for testing
        Graph graph = new Graph();
        Node node1 = new Node(1, 0, 0, 10);
        Node node2 = new Node(2, 10, 0, 10);
        graph.addNode(node1);
        graph.addNode(node2);

        // Edge edge = new Edge(1, node1, node2, 10, false, 1.0);
        Edge edge = new Edge(1, node1, node2, false, 1.0, 10);
        graph.addEdge(edge);

        // Create AgentManager
        SimulationSettings simSettings = new SimulationSettings();
        AgentGenerator agentGenerator = new AgentGenerator(graph);
        NodeDecisionContextProvider contextProvider = null; // Would need proper implementation

        AgentManager agentManager = new AgentManager(contextProvider, agentGenerator, simSettings);

        // Modify agent settings
        AgentSettings settings = agentManager.getAgentSettings();
        double originalSpeed = settings.getWALKING_SPEED();
        System.out.println("Original WALKING_SPEED: " + originalSpeed);

        // Generate some test agents
        agentManager.generateAgentOnNode("TestAgent", node1);
        agentManager.generateAgentOnNode("TestAgent", node2);
        agentManager.generateAgentOnEdge("EdgeAgent", edge, 0.5);

        System.out.println("Initial agent count: " + agentManager.getAgentsToEvacuate().size());
        Agent agent1 = agentManager.getAgentsToEvacuate().get(0);
        Agent agent2 = agentManager.getAgentsToEvacuate().get(1);
        Agent agent3 = agentManager.getAgentsToEvacuate().get(2);

        System.out.println("Agent 1: " + agent1.getName() + " stress=" + agent1.getStressLevel());
        System.out.println("Agent 2: " + agent2.getName() + " stress=" + agent2.getStressLevel());
        System.out.println("Agent 3: " + agent3.getName() + " stress=" + agent3.getStressLevel());

        // Save initial state
        System.out.println("\n--- Calling setInitialState() ---");
        agentManager.setInitialState();

        // Modify the state
        System.out.println("\n--- Modifying state ---");
        settings.setWALKING_SPEED(2.0);
        System.out.println("Changed WALKING_SPEED to: " + settings.getWALKING_SPEED());

        agent1.addStress(0.5);
        agent1.incrementNodeVisited();
        agent1.incrementNodeVisited();
        agent1.incrementNodeVisited();
        System.out.println("Agent 1 new stress: " + agent1.getStressLevel());
        System.out.println("Agent 1 nodes visited: " + agent1.getnOfNodeVisited());

        agent2.decreaseHealth(20);
        System.out.println("Agent 2 new health: " + agent2.getHealth());

        // Remove one agent to test handling of dead agents
        agentManager.killAgent(agent3);
        System.out.println("Killed agent 3. Alive agents: " + agentManager.getAgentsToEvacuate().size());

        // Reset to initial state
        System.out.println("\n--- Calling reset() ---");
        agentManager.reset();

        System.out.println("\nAfter reset:");
        System.out.println("WALKING_SPEED restored: " + agentManager.getAgentSettings().getWALKING_SPEED());
        System.out.println("Agent count restored: " + agentManager.getAgentsToEvacuate().size());

        if (agentManager.getAgentsToEvacuate().size() > 0) {
            Agent restoredAgent1 = agentManager.getAgentsToEvacuate().get(0);
            Agent restoredAgent2 = agentManager.getAgentsToEvacuate().get(1);
            Agent restoredAgent3 = agentManager.getAgentsToEvacuate().get(2);

            System.out.println("Restored Agent 1: " + restoredAgent1.getName());
            System.out.println("  - Stress: " + restoredAgent1.getStressLevel() + " (should be 0)");
            System.out.println("  - Nodes visited: " + restoredAgent1.getnOfNodeVisited() + " (should be 0)");

            System.out.println("Restored Agent 2: " + restoredAgent2.getName());
            System.out.println("  - Health: " + restoredAgent2.getHealth() + " (should be restored)");

            System.out.println("Restored Agent 3: " + restoredAgent3.getName());
            System.out.println("  - Position: " + (restoredAgent3.isOnNode() ? "on node" : "on edge"));
            System.out.println("  - Current action still exists: " + (restoredAgent3.getCurrentAction() != null));
        }

        System.out.println("\n=== Test Complete ===");
    }
}
