package fr.cy.model.agent;

import java.io.Serializable;
import java.util.Locale;
import java.util.Random;

import fr.cy.model.graph.Graph;
import fr.cy.model.graph.element.Node;

/**
 * Factory class to generate agents with random attributes. Useful for
 * prototypes and tests. The generated values are intentionally simple and
 * should be replaced by a configurable generator if needed.
 */
public class AgentGenerator implements Serializable {

	private static final long serialVersionUID = 1L;

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
		double maxSpeed = 1 + RNG.nextDouble() * 4; // 1..5 units
		double stressTolerance = RNG.nextDouble();
		double crowdingTolerance = RNG.nextDouble();
		double repeatLastDecisionTendency = 0.875 + RNG.nextDouble(); // Between 0.875 and 1.875
		double baseOwnDecisionMakingFactor = RNG.nextDouble(); // Between 0 and 1
		int health = 75 + RNG.nextInt(26); // Between 75 and 100
		double surfaceAreaTakenByAgent = 0.5 + RNG.nextDouble(); // Between 0.5 and 1.5
		// get a random node from the graph and put the agent on it (or null if no node
		// is available)
		Node randomNode = null;
		if (graph != null && !graph.getNodes().isEmpty()) {
			int randomNodeIndex = RNG.nextInt(graph.getNodes().size());
			randomNode = graph.getNodes().get(randomNodeIndex);
		}

		Agent agent = new Agent(name, randomNode, maxSpeed, stressTolerance, crowdingTolerance,
				baseOwnDecisionMakingFactor, repeatLastDecisionTendency, health, surfaceAreaTakenByAgent);
		return agent;
	}

	@Override
	public String toString() {
		return "AgentGenerator{}";
	}
}
