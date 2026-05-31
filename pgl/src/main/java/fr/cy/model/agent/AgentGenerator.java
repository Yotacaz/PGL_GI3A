package fr.cy.model.agent;

import java.util.Locale;
import java.util.Random;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
import fr.cy.model.agent.behaviour.agentActions.FollowSingleEdgeAction;

/**
 * Factory class to generate agents with random attributes. Useful for
 * prototypes and tests. The generated values are intentionally simple and
 * should be replaced by a configurable generator if needed.
 */
public class AgentGenerator {

	private final Random RNG = new Random();
	private final Graph graph;

	public AgentGenerator(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Generate a simple agent with randomized parameters and place it on the
	 * provided node.
	 *
	 * @param baseName base name to use for the agent (a unique id is appended)
	 * @param startingNode node where the agent should start, or {@code null} to
	 *                     leave it unplaced
	 */
	public Agent generateAgent(String baseName, Node startingNode) {
		return createRandomAgent(baseName, startingNode);
	}

	/**
	 * Generate a simple agent with randomized parameters and place it on the
	 * provided edge.
	 *
	 * <p>
	 * If the edge is directed, the agent is initialized from the edge start node.
	 * Otherwise, the starting node is selected randomly from the two endpoints.
	 * The created action is initialized with the provided edge progress.
	 * </p>
	 *
	 * @param baseName base name to use for the agent (a unique id is appended)
	 * @param edge edge where the agent should start
	 * @param edgeProgress initial progress on the edge, between 0 and 1 starting from the starting node
	 * @throws IllegalArgumentException if edgeProgress is not between 0 and 1
	 * @return a new agent placed on the given edge
	 */
	public Agent generateAgent(String baseName, Edge edge, double edgeProgress) {
		if (edgeProgress < 0 || edgeProgress > 1) {
			throw new IllegalArgumentException("edgeProgress must be between 0 and 1");
		}
		boolean chooseStartNode = edge.isDirected() || RNG.nextBoolean();
		Node startingNode = null;
		if (chooseStartNode) {
			startingNode = edge.getStart();
		} else {
			edgeProgress = 1.0 - edgeProgress; // if starting from the end node, invert progress to reflect distance from that node
			startingNode = edge.getEnd();
		}
		Agent agent = createRandomAgent(baseName, startingNode);
		agent.putOnEdge(edge);
		agent.setCurrentAction(new FollowSingleEdgeAction(agent, edge, edgeProgress));
		return agent;
	}

	private Agent createRandomAgent(String baseName, Node startingNode) {
		String name = String.format(Locale.ROOT, "%s-%d", baseName, RNG.nextInt(1_000_000));
		double maxSpeed = 1 + RNG.nextDouble() * 4; // 1..5 units
		double stressTolerance = RNG.nextDouble();
		double crowdingTolerance = RNG.nextDouble();
		double repeatLastDecisionTendency = 0.875 + RNG.nextDouble(); // Between 0.875 and 1.875
		double baseOwnDecisionMakingFactor = RNG.nextDouble(); // Between 0 and 1
		int health = 75 + RNG.nextInt(26); // Between 75 and 100
		double surfaceAreaTakenByAgent = 0.5 + RNG.nextDouble(); // Between 0.5 and 1.5

		return new Agent(name, startingNode, maxSpeed, stressTolerance, crowdingTolerance,
				baseOwnDecisionMakingFactor, repeatLastDecisionTendency, health, surfaceAreaTakenByAgent);
	}

	/**
	 * Generate a simple agent with randomized parameters.
	 *
	 * @param baseName base name to use for the agent (a unique id is appended)
	 */
	public Agent generateRandomAgentOnRandomNode(String baseName) {
		if (graph != null && !graph.getNodes().isEmpty()) {
			int randomNodeIndex = RNG.nextInt(graph.getNodes().size());
			return generateAgent(baseName, graph.getNodes().get(randomNodeIndex));
		}
		return generateAgent(baseName, (Node) null);
	}

	@Override
	public String toString() {
		return "AgentGenerator{}";
	}
}
