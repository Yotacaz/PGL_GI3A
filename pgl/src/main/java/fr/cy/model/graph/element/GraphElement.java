package fr.cy.model.graph.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.io.*;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.fire.Fire;
import fr.cy.model.stress.StressInducing;

/**
 * Classe abstraite représentant un élément du graphe.
 * 
 * Cette classe est la base pour tous les éléments du graphe tels que
 * les nœuds et les arêtes. Elle gère les propriétés communes incluant
 * l'identifiant unique, l'état d'incendie et le niveau de congestion.
 * 
 * @author GI3A
 * @version 1.0
 */
public abstract class GraphElement implements StressInducing, Serializable {
    private static final long serialVersionUID = 1L;
    /** Identifiant unique de l'élément du graphe */
    private final int id;

    private final List<Agent> agents;
    protected double capacity;

    protected Fire initialFire = null;

    // STRESS :
    /**
     * Total stress induced by this element and its neighbors, it is a cached value
     * and should be updated each tick
     */
    private double cachedTotalStressInducedIncludingNeighbors = 0;
    /**
     * Total stress induced by this element alone (without neighbors), it is a
     * cached value and should be updated each tick
     */
    private double cachedTotalStressInducedByThisElement = 0;

    private Fire fire;

    // ===== STATISTIQUES =====
    /** Nombre total d'agents passés par cet élément */
    private int totalAgentsCount = 0;

    /** Nombre de fois où cet élément a été complètement rempli */
    private int timesFull = 0;

    /** Congestion maximale atteinte */
    private double maxCongestion = 0;

    /** Somme des congestions pour calculer la moyenne */
    private double sumCongestion = 0;

    /** Nombre de fois où la congestion a été mesurée */
    private int congestionMeasureCount = 0;

    /**
     * Constructeur initialisant un élément du graphe.
     * 
     * @param id l'identifiant unique de l'élément
     */
    protected GraphElement(int id, double capacity) {
        this.id = id;
        this.agents = new ArrayList<>();
        this.capacity = Math.max(0.1, capacity);
        removeFire();
    }

    public void setInitialState() {
        this.initialFire = this.getFire();
    }

    /***
     * Determine la liste des elements voisins
     * 
     * @return liste de vosions
     */
    public abstract List<GraphElement> getNeighbors();

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof GraphElement)) {
            return false;
        }

        GraphElement element = (GraphElement) o;

        return getId() == element.getId();
    }

    /**
     * Récupère l'identifiant unique de cet élément du graphe.
     * 
     * @return l'identifiant de l'élément
     */
    public int getId() {
        return id;
    }

    /**
     * Vérifie si cet élément est actuellement en feu.
     * 
     * @return {@code true} si l'élément est en feu, {@code false} sinon
     */
    public boolean isOnFire() {
        return fire != null;
    }

    public Fire getFire() {
        return fire;
    }

    public void setFire(Fire fire) {
        this.fire = fire;
    }

    public void removeFire() {
        this.fire = null;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Ajoute un agent si la capacité le permet
     * 
     * @param a Agent
     */
    public void addAgent(Agent a) {
        if (!isFull()) {
            agents.add(a);
            // Mettre à jour les statistiques
            incrementTotalAgentsCount();
            recordCongestionMeasure();

            // Vérifier si on vient de remplir l'élément
            if (isFull()) {
                incrementTimesFull();
            }
        }
    }

    public boolean isCongested() {
        return getCongestion() > 0.7;
    }

    /**
     * Evaluate a score multiplier for an agent on this element, based on its
     * properties and the agent's properties.
     * 
     * @param agentState the properties of the agent for which we want to evaluate
     *                   the score multiplier
     * @return a score multiplier for an agent on this element, based on its
     *         properties and the agent's properties,
     *         where a value < 1 means that the element is less attractive, and > 1
     *         means that it is more attractive
     */
    public double getScoreMultiplierForAgent(AgentDecisionalProperties agentState) {
        // penalize graph elements on fire
        double scoreMult = 1.0;
        if (isOnFire()) {
            Fire fire = getFire();
            assert fire != null;
            scoreMult *= 0.1 / (1.0 + fire.getIntensity() + fire.getSmokeLevel() + fire.getSpreadRate());
        }
        // penalize very congested graph elements
        double congestion = getCongestion();
        if (isCongested()) {
            scoreMult *= 0.4 * (1.0 + congestion - agentState.getCongestionTolerance());
        }
        double stressInducedByThisElement = getStressInducingImpact();
        if (stressInducedByThisElement > 0.90) {
            scoreMult *= 0.4 * (1.0 + stressInducedByThisElement - agentState.getStressTolerance());
        }
        return scoreMult;
    }

    /**
     * Update the total stress induced by this element alone (without neighbors),
     * which is a value between 0 and 1, and cache it.
     * 
     * @return the total stress induced by this element alone (without neighbors)
     *         after update
     */
    public double updateStressGeneratedByThisElement() {
        // If we want differents calculation for node and edge, we could use protected
        // constants like CONGESTION_STRESS_FACTOR and AGENT_STRESS_FACTOR ...
        double stress = 0;
        double congestion = getCongestion();
        stress += congestion * congestion * 0.4;
        double meanStressFromAgents = getAgentsTotalStress() / Math.max(1, getAgents().size());
        stress += meanStressFromAgents * meanStressFromAgents * 0.4;
        if (isOnFire()) {
            stress += 0.2 * getFire().getIntensity();
        }

        cachedTotalStressInducedByThisElement = Math.min(stress, 1.0);
        return cachedTotalStressInducedByThisElement;
    }

    /**
     * Get the total stress induced by this element alone (without neighbors), which
     * is a value between 0 and 1.
     * This is a cached value that should be updated each tick.
     * 
     * @return the total stress induced by this element alone (without neighbors)
     */
    public double getCachedTotalStressInducedByThisElement() {
        return cachedTotalStressInducedByThisElement;
    }

    public double updateCachedTotalStressInducedIncludingNeighbors() {
        double neighbouringStress = 0;
        for (GraphElement neighbor : getNeighbors()) {
            neighbouringStress += neighbor.getCachedTotalStressInducedByThisElement();
        }
        double thisElementStress = getCachedTotalStressInducedByThisElement();
        double totalStress = Math.min(thisElementStress + neighbouringStress * neighbouringStress * 0.4, 1.0);
        cachedTotalStressInducedIncludingNeighbors = totalStress;
        return totalStress;
    }

    /**
     * Sommate the stress level of all agents present on this element, (a single
     * agent has a value between 0 and 1)
     * 
     * @return the total stress level of agents on this element
     */
    public double getAgentsTotalStress() {
        double totalStress = 0;
        for (Agent agent : agents) {
            totalStress += agent.getStressLevel();
        }
        return totalStress;
    }

    @Override
    public double getStressInducingImpact() {
        return getCachedTotalStressInducedIncludingNeighbors();
    }

    /**
     * Enleve un agent
     * 
     * @param a agent
     */
    public void removeAgent(Agent a) {
        // remove only if present, then record congestion measure
        if (agents.remove(a)) {
            recordCongestionMeasure();
        }
    }

    public double getCapacity() {
        return capacity;
    }

    /**
     * Determine espace occupée par les agents
     * 
     * @return surface occupée
     */
    public double getOccupiedSpace() {
        double occupied = 0;
        for (Agent agent : agents) {
            occupied += agent.getSurfaceAreaTakenByAgent();
        }

        return occupied;
    }

    /**
     * Calcule la congestion entre 0 et 1 selon les agenrs présents
     * 
     * @return congestion
     */
    public double getCongestion() {
        return Math.min(getOccupiedSpace() / getCapacity(), 1);
    }

    /**
     * Determine si l'élement est full
     * 
     * @return true si oui
     */
    public boolean isFull() {
        return getOccupiedSpace() >= capacity;
    }

    /**
     * @return the total stress induced by this element and its neighbors, which is
     *         a value between 0 and 1
     */
    public double getCachedTotalStressInducedIncludingNeighbors() {
        return cachedTotalStressInducedIncludingNeighbors;
    }

    // ===== STATISTIQUES =====

    /**
     * Incrémente le compteur d'agents passés.
     */
    public void incrementTotalAgentsCount() {
        this.totalAgentsCount++;
    }

    /**
     * @return le nombre total d'agents passés par cet élément
     */
    public int getTotalAgentsCount() {
        return totalAgentsCount;
    }

    /**
     * Incrémente le compteur de fois où l'élément a été rempli.
     */
    public void incrementTimesFull() {
        this.timesFull++;
    }

    /**
     * @return le nombre de fois où l'élément a été complètement rempli
     */
    public int getTimesFull() {
        return timesFull;
    }

    /**
     * Enregistre une mesure de congestion.
     */
    public void recordCongestionMeasure() {
        double currentCongestion = getCongestion();
        sumCongestion += currentCongestion;
        congestionMeasureCount++;

        if (currentCongestion > maxCongestion) {
            maxCongestion = currentCongestion;
        }
    }

    /**
     * @return la congestion maximale atteinte
     */
    public double getMaxCongestion() {
        return maxCongestion;
    }

    /**
     * @return la congestion moyenne
     */
    public double getAverageCongestion() {
        if (congestionMeasureCount == 0) {
            return 0;
        }
        return sumCongestion / congestionMeasureCount;
    }

    /**
     * @return le nombre de mesures de congestion enregistrées
     */
    public int getCongestionMeasureCount() {
        return congestionMeasureCount;
    }

    public void reset() {
        agents.clear();
        this.setFire(initialFire);
        maxCongestion = 0;
        sumCongestion = 0;
        congestionMeasureCount = 0;
        timesFull = 0;
        totalAgentsCount = 0;
        cachedTotalStressInducedByThisElement = 0;
        cachedTotalStressInducedIncludingNeighbors = 0;
    }
}
