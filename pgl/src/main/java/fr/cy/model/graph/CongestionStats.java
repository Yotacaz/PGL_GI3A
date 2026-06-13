package fr.cy.model.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fr.cy.model.graph.element.GraphElement;

/**
 * A data container for calculating and storing congestion metrics for a
 * collection
 * of {@link GraphElement} objects.
 *
 * @param <T> The specific type of {@link GraphElement} being analyzed.
 */
public class CongestionStats<T extends GraphElement> implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final CongestionsGraphElementComparator COMPARATOR = new CongestionsGraphElementComparator();
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
     *
     * <p>
     * This method identifies the maximum, minimum, average, and total congestion,
     * as well as the most and least congested elements.
     * <b>Note:</b> The provided list is sorted in place using
     * {@link CongestionsGraphElementComparator}.
     * </p>
     *
     * @param <T>           the type of graph element to analyze
     * @param graphElements the list of elements to analyze
     * @return A {@code CongestionStats} instance containing the metrics,
     *         or {@code null} if the list is empty or null.
     */
    public static <T extends GraphElement> CongestionStats<T> computeCongestionStats(List<T> graphElements) {
        if (graphElements == null || graphElements.isEmpty()) {
            return null;
        }
        List<T> sortedElements = new ArrayList<>(graphElements);
        sortedElements.sort(COMPARATOR);

        double totalCongestionLevel = sortedElements.stream().mapToDouble(T::getCongestion).sum();
        int count = sortedElements.size();

        // After sorting, index 0 is the least congested, last index is most congested
        double minCongestionLevel = sortedElements.get(0).getCongestion();
        double maxCongestionLevel = sortedElements.get(count - 1).getCongestion();
        double averageCongestionLevel = totalCongestionLevel / count;

        return new CongestionStats<>(maxCongestionLevel, minCongestionLevel, averageCongestionLevel,
                totalCongestionLevel,
                sortedElements.get(count - 1), sortedElements.get(0), sortedElements, count);
    }

    /**
     * Returns the average congestion level across all analyzed elements.
     *
     * @return The average congestion level across all elements.
     */
    public double getAverageCongestionLevel() {
        return averageCongestionLevel;
    }

    /**
     * Returns the maximum congestion level found among the analyzed elements.
     * 
     * @return The maximum congestion level found.
     */
    public double getMaxCongestionLevel() {
        return maxCongestionLevel;
    }

    /**
     * Returns the total number of elements that were analyzed to compute the
     * congestion statistics.
     * 
     * @return The total number of elements analyzed.
     */
    public int getCount() {
        return count;
    }

    /**
     * Returns the minimum congestion level found among the analyzed elements.
     *
     * @return The minimum congestion level found.
     */
    public double getMinCongestionLevel() {
        return minCongestionLevel;
    }

    /**
     * Returns the sum of all congestion levels across all analyzed elements.
     *
     * @return The sum of all congestion levels.
     */
    public double getTotalCongestionLevel() {
        return totalCongestionLevel;
    }

    /**
     * Returns the element with the lowest congestion level among those analyzed.
     *
     * @return The element with the lowest congestion level.
     */
    public T getLeastCongestedElement() {
        return leastCongestedElement;
    }

    /**
     * Returns the element with the highest congestion level among those analyzed.
     *
     * @return The element with the highest congestion level.
     */
    public T getMostCongestedElement() {
        return mostCongestedElement;
    }

    /**
     * Returns the list of analyzed elements sorted by congestion level in ascending order.
     *
     * @return The list of elements sorted by congestion (ascending).
     */
    public List<T> getSortedByCongestion() {
        return sortedByCongestion;
    }
}