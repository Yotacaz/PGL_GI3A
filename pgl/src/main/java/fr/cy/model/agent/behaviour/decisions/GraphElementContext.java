package fr.cy.model.agent.behaviour.decisions;

import java.io.Serializable;
import java.util.List;

import fr.cy.model.graph.CongestionStats;
import fr.cy.model.graph.element.GraphElement;

public interface GraphElementContext<T extends GraphElement> extends Serializable {

    List<T> getAccessibleElements();

    CongestionStats<T> getCongestionStatsForAccessibleElements();

    List<T> getSortedAccessibleElementsByCongestion();
}
