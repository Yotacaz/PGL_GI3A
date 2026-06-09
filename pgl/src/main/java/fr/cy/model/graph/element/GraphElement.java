package fr.cy.model.graph.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.Serializable;

import java.io.*;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.behaviour.properties.AgentDecisionalProperties;
import fr.cy.model.fire.Fire;
import fr.cy.model.simulation.SimulationSettings;
import fr.cy.model.stress.StressInducing;

/**
 * Abstract class representing a graph element.
 * 
 * This class is the base for all graph elements such as
 * nodes and edges. It manages shared properties including
 * unique identifier, fire state and congestion level.
 * 
 * @author GI3A
 * @version 1.0
 */
public abstract class GraphElement implements StressInducing, Serializable {
    private static final long serialVersionUID = 1L;
    /** Identifiant unique de l'élément du graphe */
    private final int id;

    private transient List<Agent> agents;
    private double capacity;
    private boolean isForcedCongested = false;
    private int forcedCongestionTicks = 0;
    private transient Fire initialFire; // To store the initial fire state for reset

    // STRESS:
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

    // ===== STATISTICS =====
    /** Total number of agents that passed through this element */
    private int totalAgentsCount = 0;

    /** Number of times this element became fully occupied */
    private int timesFull = 0;

    /** Maximum congestion reached */
    private double maxCongestion = 0;

    /** Sum of congestion values used to calculate the average */
    private double sumCongestion = 0;

    /** Number of times congestion was measured */
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

    /***
     * Determines the list of neighboring elements
     * 
     * @return list of neighbors
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

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GraphElement element = (GraphElement) o;
        return getId() == element.getId();
    }

    /**
     * Returns the unique identifier of this graph element.
     * 
     * @return the identifier of the element
     */
    public int getId() {
        return id;
    }

    /**
     * Checks whether this element is currently on fire.
     * 
     * @return {@code true} if the element is on fire, {@code false} otherwise
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

    /**@return the list of all agents on the element */
    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Adds an agent if capacity allows.
     * 
     * @param a Agent
     */
    public void addAgent(Agent a) {
        if (!isFull()) {
            agents.add(a);
            // Update statistics
            incrementTotalAgentsCount();
            recordCongestionMeasure();

            // Check if the element has just become full
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
     * Sums the stress levels of all agents present on this element (a single
     * agent has a value between 0 and 1).
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

    public void setCapacity(double capacity) {
        this.capacity = Math.max(0.1, capacity);
    }

    /**
     * Determines the occupied space by agents.
     * 
     * @return occupied area
     */
    public double getOccupiedSpace() {
        double occupied = 0;
        for (Agent agent : agents) {
            occupied += agent.getSurfaceAreaTakenByAgent();
        }

        return occupied;
    }

    /**
     * Calculates congestion between 0 and 1 based on the present agents.
     * 
     * @return congestion
     */
    public double getCongestion() {
        return Math.min(getOccupiedSpace() / getCapacity(), 1);
    }

    /**
     * Determines if the element is full.
     * 
     * @return true if yes
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

    // ===== STATISTICS =====

    /**
     * Increments the count of agents that passed through.
     */
    public void incrementTotalAgentsCount() {
        this.totalAgentsCount++;
    }

    /**
     * @return the total number of agents that passed through this element
     */
    public int getTotalAgentsCount() {
        return totalAgentsCount;
    }

    /**
     * Increments the count of times the element was filled.
     */
    public void incrementTimesFull() {
        this.timesFull++;
    }

    /**
     * @return the number of times the element was fully filled
     */
    public int getTimesFull() {
        return timesFull;
    }

    /**
     * Records a congestion measurement.
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
     * @return the maximum congestion reached
     */
    public double getMaxCongestion() {
        return maxCongestion;
    }

    /**
     * @return the average congestion
     */
    public double getAverageCongestion() {
        if (congestionMeasureCount == 0) {
            return 0;
        }
        return sumCongestion / congestionMeasureCount;
    }

    /**
     * @return the number of congestion measurements recorded
     */
    public int getCongestionMeasureCount() {
        return congestionMeasureCount;
    }

    /**
     * @return the damage caused by this element for a whole tick, based on its properties such as fire intensity
     */
    public double getDamage() {
        double duration = SimulationSettings.getInstance().getTickDuration();
        return getDamage(duration);
    }

    /**
     * @return the damage caused by this element, based on its properties such as fire intensity
     */
    public double getDamage(double duration) { //FIXME: TEMPORATY 
        double damage = (getCongestion() > 1 ? 1 : 0) * duration;
        if (isOnFire()) {
            assert getFire() != null;
            return damage + getFire().getDamage(duration);
        }
        return damage;
    }

    public double getMaxAgentSpeed() {
        // We prevent mathematical congestion from exceeding 0.9 (90%) in the calculation
        // so that the crowd can always trample very slowly.
        double effectiveCongestion = Math.min(getCongestion(), 0.9);
        double congestionFactor = 1.0 - effectiveCongestion;

        double calculatedSpeed = AgentSettings.getInstance().getMAX_RUNNING_SPEED() * congestionFactor;

        if (isOnFire()) {
            return calculatedSpeed * 1.5;
        }

        // We guarantee a microscopic survival speed (0.1) instead of 0.0
        return Math.max(calculatedSpeed, 0.1);
    }

    public void setInitialState() {
        if (this.getFire() != null) {
            this.initialFire = getFire();
        } else {
            this.initialFire = null;
        }
    }

    public void reset() {
        agents.clear();

        if (this.initialFire != null) {
            // On recrée une copie propre pour la nouvelle simulation
            this.setFire(initialFire);
        } else {
            this.setFire(null);
        }

        maxCongestion = 0;
        sumCongestion = 0;
        congestionMeasureCount = 0;
        timesFull = 0;
        totalAgentsCount = 0;
        cachedTotalStressInducedByThisElement = 0;
        cachedTotalStressInducedIncludingNeighbors = 0;
    }

    public void setForcedCongestion(boolean congested) {
        this.isForcedCongested = congested;
        if (congested) {
            this.forcedCongestionTicks = 0; // Reset the counter
        }
    }

    public boolean isForcedCongested() {
        return isForcedCongested;
    }

    /**
     * Called each tick to decrement the forced congestion counter.
     */
    public void updateForcedCongestion() {
        if (isForcedCongested) {
            forcedCongestionTicks++;
            if (forcedCongestionTicks >= 2) { // 2 cycles de simulation
                isForcedCongested = false;
                forcedCongestionTicks = 0;
            }
        }
    }
}
