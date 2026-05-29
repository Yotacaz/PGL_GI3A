package fr.cy.model.agent;

import java.util.Locale;
import java.util.Random;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;

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
	 * Generate a simple agent with randomized parameters.
	 *
	 * @param baseName base name to use for the agent (a unique id is appended)
	 */
	public Agent generateRandomAgent(String baseName) {
		String name = String.format(Locale.ROOT, "%s-%d", baseName, RNG.nextInt(1_000_000));
		int maxSpeed = 1 + RNG.nextInt(5); // 1..5 units
		double stressTolerance = RNG.nextDouble();
		double crowdingTolerance = RNG.nextDouble();
		//get a random node from the graph and put the agent on it (or null if no node is available)
		Node randomNode = null;
		if (graph != null && !graph.getNodes().isEmpty()) {
			int randomNodeIndex = RNG.nextInt(graph.getNodes().size());
			randomNode = graph.getNodes().get(randomNodeIndex);
		}

		Agent agent = new Agent(name, maxSpeed, stressTolerance, crowdingTolerance);
		agent.putOnNode(randomNode);
		return agent;
	}

	@Override
	public String toString() {
		return "AgentGenerator{}";
	}
}
