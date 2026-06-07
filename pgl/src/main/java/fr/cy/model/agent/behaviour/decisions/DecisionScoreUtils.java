package fr.cy.model.agent.behaviour.decisions;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.ToDoubleFunction;

import fr.cy.model.agent.exceptions.AgentBehaviourException;

final class DecisionScoreUtils {
    private static final Random RNG = new Random();

    private DecisionScoreUtils() {
    }

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
