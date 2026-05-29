package fr.cy.model.graph;

import java.util.Comparator;
import java.util.List;

import fr.cy.model.graph.element.GraphElement;

public class CongestionStats<T extends GraphElement> {
    private double maxCongestionLevel;
    private T mostCongestedElement;
    private double minCongestionLevel;
    private T leastCongestedElement;
    private double averageCongestionLevel;
    private double totalCongestionLevel;
    private List<T> sortedByCongestion;
    private int count;

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

    /** A comparator for comparing graph elements based on their congestion levels.
     *  From the most to the least congested. */
    private static class CongestionsGraphElementComparator implements Comparator<GraphElement> {
        @Override
        public int compare(GraphElement e1, GraphElement e2) {
            return Double.compare(e1.getCongestion(), e2.getCongestion());
        }
    }

    /**
     * Computes congestion statistics for a list of graph elements, including maximum, minimum, average, and total congestion levels, as well as identifying the most and least congested elements.
     * @param graphElements the list of graph elements to analyze for congestion statistics **WARNING: the list will be sorted in place**.
     * @return the congestion statistics for the provided graph elements
     */
    public static <T extends GraphElement> CongestionStats<T> computeCongestionStats(List<T> graphElements) { 
        // sorted by congestion level, from the most to the least congested
        graphElements.sort(new CongestionsGraphElementComparator());
        double totalCongestionLevel = graphElements.stream().mapToDouble(T::getCongestion).sum();
        int count = graphElements.size();
        double maxCongestionLevel = count > 0 ? graphElements.get(0).getCongestion() : 0.0;
        double minCongestionLevel = count > 0 ? graphElements.get(count - 1).getCongestion() : 0.0;

        double averageCongestionLevel = count > 0 ? totalCongestionLevel / count : 0.0;
        return new CongestionStats<>(maxCongestionLevel, minCongestionLevel, averageCongestionLevel, totalCongestionLevel,
                graphElements.get(0), graphElements.get(count - 1), graphElements, count);
    }

    public double getAverageCongestionLevel() {
        return averageCongestionLevel;
    }

    public double getMaxCongestionLevel() {
        return maxCongestionLevel;
    }

    public int getCount() {
        return count;
    }

    public double getMinCongestionLevel() {
        return minCongestionLevel;
    }

    public double getTotalCongestionLevel() {
        return totalCongestionLevel;
    }

    public T getLeastCongestedElement() {
        return leastCongestedElement;
    }

    public T getMostCongestedElement() {
        return mostCongestedElement;
    }

    public List<T> getSortedByCongestion() {
        return sortedByCongestion;
    }
}
