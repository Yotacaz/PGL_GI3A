package fr.cy.model.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToDoubleFunction;

import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.Node;
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
    private double maxCongestionLevel;
    private T mostCongestedElement;
    private double minCongestionLevel;
    private T leastCongestedElement;
    private double averageCongestionLevel;
    private double totalCongestionLevel;
    private double firstQuartile;
    private double median;
    private double thirdQuartile;
    private int count;

    /**
     * Constructs a new {@code CongestionStats} object with calculated metrics.
     * 
     * @param maxCongestionLevel The maximum congestion level found among the analyzed elements.
     * @param minCongestionLevel The minimum congestion level found among the analyzed elements.
     * @param averageCongestionLevel The average congestion level across all analyzed elements.
     * @param totalCongestionLevel The total congestion level across all analyzed elements.
     * @param firstQuartile The first quartile (25th percentile) congestion level.
     * @param median The median (50th percentile) congestion level.
     * @param thirdQuartile The third quartile (75th percentile) congestion level.
     * @param mostCongestedElement The element with the highest congestion level.
     * @param leastCongestedElement The element with the lowest congestion level.
     * @param count The total number of elements analyzed to compute the congestion statistics.
     */
    protected CongestionStats(double maxCongestionLevel, double minCongestionLevel, double averageCongestionLevel,
            double totalCongestionLevel, double firstQuartile, double median, double thirdQuartile,
            T mostCongestedElement, T leastCongestedElement, int count) {
        this.maxCongestionLevel = maxCongestionLevel;
        this.minCongestionLevel = minCongestionLevel;
        this.averageCongestionLevel = averageCongestionLevel;
        this.totalCongestionLevel = totalCongestionLevel;
        this.firstQuartile = firstQuartile;
        this.median = median;
        this.thirdQuartile = thirdQuartile;
        this.mostCongestedElement = mostCongestedElement;
        this.leastCongestedElement = leastCongestedElement;
        this.count = count;
    }

    /**
     * Computes congestion statistics for a list of graph elements using a value extractor.
     * 
     * <p>
     * This method identifies the maximum, minimum, average, and total congestion,
     * as well as the most and least congested elements and quartile values.
     * </p>
     * 
     * @param graphElements The list of elements to analyze.
     * @param valueExtractor Function to extract the congestion value from each element.
     * 
     * @return A {@code CongestionStats} instance containing the metrics,
     *         or {@code null} if the list is empty or null.
     */
    private static <T extends GraphElement> CongestionStats<T> computeStats(List<T> graphElements, 
            ToDoubleFunction<T> valueExtractor) {
        if (graphElements == null || graphElements.isEmpty()) {
            return null;
        }

        int count = graphElements.size();
        double totalCongestionLevel = 0;
        double minCongestionLevel = Double.MAX_VALUE;
        double maxCongestionLevel = Double.MIN_VALUE;
        T leastCongestedElement = null;
        T mostCongestedElement = null;
        
        // First pass: calculate basic statistics
        for (T element : graphElements) {
            double congestionValue = valueExtractor.applyAsDouble(element);
            totalCongestionLevel += congestionValue;
            
            if (congestionValue < minCongestionLevel) {
                minCongestionLevel = congestionValue;
                leastCongestedElement = element;
            }
            
            if (congestionValue > maxCongestionLevel) {
                maxCongestionLevel = congestionValue;
                mostCongestedElement = element;
            }
        }

        double averageCongestionLevel = totalCongestionLevel / count;

        // Calculate quartiles
        List<Double> sortedValues = new ArrayList<>();
        for (T element : graphElements) {
            sortedValues.add(valueExtractor.applyAsDouble(element));
        }
        Collections.sort(sortedValues);
        
        double firstQuartile = calculateQuartile(sortedValues, 0.25);
        double median = calculateQuartile(sortedValues, 0.5);
        double thirdQuartile = calculateQuartile(sortedValues, 0.75);

        return new CongestionStats<>(maxCongestionLevel, minCongestionLevel, averageCongestionLevel,
                totalCongestionLevel, firstQuartile, median, thirdQuartile,
                mostCongestedElement, leastCongestedElement, count);
    }

    private static double calculateQuartile(List<Double> sortedValues, double quartile) {
        int n = sortedValues.size();
        double position = quartile * (n - 1);
        int lowerIndex = (int) Math.floor(position);
        int upperIndex = (int) Math.ceil(position);
        
        if (lowerIndex == upperIndex) {
            return sortedValues.get(lowerIndex);
        }
        
        double lowerValue = sortedValues.get(lowerIndex);
        double upperValue = sortedValues.get(upperIndex);
        double fraction = position - lowerIndex;
        
        return lowerValue + fraction * (upperValue - lowerValue);
    }

    /**
     * Computes congestion statistics for a list of graph elements.
     * 
     * <p>
     * This method identifies the maximum, minimum, average, and total congestion,
     * as well as the most and least congested elements and quartile values.
     * </p>
     * 
     * @param graphElements The list of elements to analyze.
     * 
     * @return A {@code CongestionStats} instance containing the metrics,
     *         or {@code null} if the list is empty or null.
     */
    public static <T extends GraphElement> CongestionStats<T> computeCongestionStats(List<T> graphElements) {
        return computeStats(graphElements, GraphElement::getCongestion);
    }

    public static  CongestionStats<Edge> computeCongestionStatsForEdge(List<Edge> graphElements, Node sourceNode) {
        return computeStats(graphElements, (Edge edge) -> {
            boolean forward = sourceNode.equals(edge.getStart());
            return edge.getDirectionalContributionToCongestion(forward);
        });
    }

    /** @return The average congestion level across all elements. */
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

    /** @return The first quartile (25th percentile) congestion level. */
    public double getFirstQuartile() {
        return firstQuartile;
    }

    /** @return The median (50th percentile) congestion level. */
    public double getMedian() {
        return median;
    }

    /** @return The third quartile (75th percentile) congestion level. */
    public double getThirdQuartile() {
        return thirdQuartile;
    }
}
