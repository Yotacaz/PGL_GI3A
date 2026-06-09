package fr.cy.model.graph;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import fr.cy.model.graph.element.GraphElement;

/**
 * A data container for calculating and storing congestion metrics for a
 * collection
 * of {@link GraphElement} objects.
 * * @param <T> The specific type of {@link GraphElement} being analyzed.
 */
public class CongestionStats<T extends GraphElement> implements Serializable {
    private static final long serialVersionUID = 1L;

    private double maxCongestionLevel;
    private T mostCongestedElement;
    private double minCongestionLevel;
    private T leastCongestedElement;
    private double averageCongestionLevel;
    private double totalCongestionLevel;
    private List<T> sortedByCongestion;
    private int count;

    /**
     * Constructs a new {@code CongestionStats} object with calculated metrics.
     */
    private CongestionStats(double maxCongestionLevel, double minCongestionLevel, double averageCongestionLevel,
            double totalCongestionLevel, T mostCongestedElement, T leastCongestedElement, List<T> sortedByCongestion,
            int count) {
        this.maxCongestionLevel = maxCongestionLevel;
        this.minCongestionLevel = minCongestionLevel;
        this.averageCongestionLevel = averageCongestionLevel;
        this.totalCongestionLevel = totalCongestionLevel;
        this.mostCongestedElement = mostCongestedElement;
        this.leastCongestedElement = leastCongestedElement;
        this.sortedByCongestion = sortedByCongestion;
        this.count = count;
    }

    /**
     * A comparator for ordering graph elements based on their congestion levels
     * in ascending order.
     */
    private static class CongestionsGraphElementComparator implements Comparator<GraphElement> {
        @Override
        public int compare(GraphElement e1, GraphElement e2) {
            return Double.compare(e1.getCongestion(), e2.getCongestion());
        }
    }

    /**
     * Computes congestion statistics for a list of graph elements.
     * <p>
     * This method identifies the maximum, minimum, average, and total congestion,
     * as well as the most and least congested elements.
     * <b>Note:</b> The provided list is sorted in place using
     * {@link CongestionsGraphElementComparator}.
     * </p>
     * * @param graphElements The list of elements to analyze.
     * 
     * @return A {@code CongestionStats} instance containing the metrics,
     *         or {@code null} if the list is empty or null.
     */
    public static <T extends GraphElement> CongestionStats<T> computeCongestionStats(List<T> graphElements) {
        if (graphElements == null || graphElements.isEmpty()) {
            return null;
        }

        graphElements.sort(new CongestionsGraphElementComparator());

        double totalCongestionLevel = graphElements.stream().mapToDouble(T::getCongestion).sum();
        int count = graphElements.size();

        // After sorting, index 0 is the least congested, last index is most congested
        double minCongestionLevel = graphElements.get(0).getCongestion();
        double maxCongestionLevel = graphElements.get(count - 1).getCongestion();
        double averageCongestionLevel = totalCongestionLevel / count;

        return new CongestionStats<>(maxCongestionLevel, minCongestionLevel, averageCongestionLevel,
                totalCongestionLevel,
                graphElements.get(count - 1), graphElements.get(0), graphElements, count);
    }

    /** @return The average congestion level across all elements. */
    public double getAverageCongestionLevel() {
        return averageCongestionLevel;
    }

    /** @return The maximum congestion level found. */
    public double getMaxCongestionLevel() {
        return maxCongestionLevel;
    }

    /** @return The total number of elements analyzed. */
    public int getCount() {
        return count;
    }

    /** @return The minimum congestion level found. */
    public double getMinCongestionLevel() {
        return minCongestionLevel;
    }

    /** @return The sum of all congestion levels. */
    public double getTotalCongestionLevel() {
        return totalCongestionLevel;
    }

    /** @return The element with the lowest congestion level. */
    public T getLeastCongestedElement() {
        return leastCongestedElement;
    }

    /** @return The element with the highest congestion level. */
    public T getMostCongestedElement() {
        return mostCongestedElement;
    }

    /** @return The list of elements sorted by congestion (ascending). */
    public List<T> getSortedByCongestion() {
        return sortedByCongestion;
    }
}