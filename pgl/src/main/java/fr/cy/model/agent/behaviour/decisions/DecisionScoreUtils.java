package fr.cy.model.agent.behaviour.decisions;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.ToDoubleFunction;

import fr.cy.model.agent.exceptions.AgentBehaviourException;

/**
 * Utility class for computing and selecting decisions based on scores.
 * 
 * <p>This class provides static methods for calculating decision scores and
 * selecting options based on weighted probabilities. It handles the mathematical
 * operations needed for agent decision-making processes.</p>
 * 
 * <p>The class uses a utility pattern and is not meant to be instantiated.</p>
 */
final class DecisionScoreUtils {
    /** Random number generator used for probabilistic decision selection */
    private static final Random RNG = new Random();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DecisionScoreUtils() {
    }

    /**
     * Selects an element from a map based on weighted scores.
     * 
     * <p>This method implements a weighted random selection algorithm where elements
     * with higher scores have a proportionally higher chance of being selected. The
     * selection is based on the total score and individual element scores.</p>
     * 
     * @param <T> the type of elements to select from
     * @param elementScores a map of elements to their associated scores
     * @param totalScore the sum of all element scores
     * @return the selected element
     * @throws AgentBehaviourException if no elements are available or scores are inconsistent
     */
    static <T> T selectElementBasedOnScores(Map<T, Double> elementScores, double totalScore) {
        if (elementScores.isEmpty()) {
            throw new AgentBehaviourException("No graph element to choose from.");
        }
        if (totalScore <= 0) {
            return elementScores.keySet().iterator().next();    //select first eleme,t
        }
        double randomValue = RNG.nextDouble() * totalScore;
        for (Map.Entry<T, Double> entry : elementScores.entrySet()) {
            randomValue -= entry.getValue();
            if (randomValue <= 0) {
                return entry.getKey();
            }
        }
        throw new AgentBehaviourException(
                "No graph element selected. The total score is inconsistent with non-negative element scores.");
    }

    /**
     * Computes scores for a list of elements using a scoring function and multipliers.
     * 
     * <p>This method calculates weighted scores for each element in the list by applying
     * a scoring function and then multiplying by element-specific multipliers. The results
     * are stored in the provided map and the total score is returned.</p>
     * 
     * @param <T> the type of elements to score
     * @param elements the list of elements to score
     * @param scorer the scoring function that calculates base scores for elements
     * @param preferredElements the map to store element scores (will be populated by this method)
     * @param scoreMultipliers multipliers to apply to each element's base score
     * @return the total score (sum of all individual element scores)
     * @throws IllegalArgumentException if the elements and multipliers lists have different sizes
     */
    static <T> double computeElementScores(List<T> elements, ToDoubleFunction<T> scorer,
            Map<T, Double> preferredElements, List<Double> scoreMultipliers) {
        if (elements.size() != scoreMultipliers.size()) {
            throw new IllegalArgumentException(
                    "Score multipliers list must have the same size as the element list");
        }

        double totalScore = 0.0;
        for (int i = 0; i < elements.size(); i++) {
            T element = elements.get(i);
            double score = Math.max(0.0, scorer.applyAsDouble(element));
            score *= scoreMultipliers.get(i);
            preferredElements.put(element, score);
            totalScore += score;
        }
        return totalScore;
    }
}
