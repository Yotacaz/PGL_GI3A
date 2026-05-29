package fr.cy.model.agent;

import java.util.Locale;
import java.util.Random;

/**
 * Factory class to generate agents with random attributes. Useful for
 * prototypes and tests. The generated values are intentionally simple and
 * should be replaced by a configurable generator if needed.
 */
public class AgentGenerator {

	private static final Random RNG = new Random();

	/**
	 * Generate a simple agent with randomized parameters.
	 *
	 * @param baseName base name to use for the agent (a unique id is appended)
	 */
	public static Agent generateRandomAgent(String baseName) {
		String name = String.format(Locale.ROOT, "%s-%d", baseName, RNG.nextInt(1_000_000));
		int maxSpeed = 1 + RNG.nextInt(5); // 1..5 units
		double stressTolerance = RNG.nextDouble();
		double crowdingTolerance = RNG.nextDouble();
		return new Agent(name, maxSpeed, stressTolerance, crowdingTolerance);
	}

	@Override
	public String toString() {
		return "AgentGenerator{}";
	}
}
