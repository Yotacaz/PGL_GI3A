package fr.cy.model.agent.behaviour.decisions;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.GraphElement;

abstract class AbstractGraphElementContext<T extends GraphElement> implements GraphElementContext<T> {
    private static final long serialVersionUID = 1L;

    private final List<T> accessibleElements;
    private final CongestionStats<T> congestionStats;

    AbstractGraphElementContext(List<T> accessibleElements) {
        this.accessibleElements = Objects.requireNonNull(accessibleElements, "accessibleElements");
        this.congestionStats = CongestionStats.computeCongestionStats(accessibleElements);
    }

    @Override
    public List<T> getAccessibleElements() {
        return Collections.unmodifiableList(accessibleElements);
    }

    @Override
    public CongestionStats<T> getCongestionStatsForAccessibleElements() {
        return congestionStats;
    }

    @Override
    public List<T> getSortedAccessibleElementsByCongestion() {
        return Collections.unmodifiableList(congestionStats.getSortedByCongestion());
    }
}
