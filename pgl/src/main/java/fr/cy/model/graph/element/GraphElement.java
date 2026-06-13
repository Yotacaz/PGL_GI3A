package fr.cy.model.graph.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.io.*;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.properties.AgentDecisionalProperties;
import fr.cy.model.agent.properties.AgentPhysicalProperties;
import fr.cy.model.fire.Fire;
import fr.cy.model.simulation.SimulationSettings;
import fr.cy.model.stress.StressInducing;

/**
 * Abstract class representing a graph element.
 * * This class serves as the base for all graph elements such as nodes and
 * edges.
 * It manages shared properties including a unique identifier, fire state,
 * congestion tracking, and stress metrics.
 * * @author GI3A
 * 
 * @version 1.0
 */
public sealed abstract class GraphElement implements StressInducing, Serializable permits Node,Edge {
    private static final long serialVersionUID = 1L;

    // Identity
    private final int id;

    // Agents & Capacity
    private double capacity;
    private List<Agent> agents;

    /**
     * * Transient map tracking the mandatory wait cycles (ticks) for each agent
     * stuck during a heavy congestion state. Not serialized.
     */
    private transient Map<Agent, Integer> congestionWaitTimes = new HashMap<>();

    // Fire state
    private Fire fire;
    private Fire initialFire;

    // Stress (Cache)
    private double cachedTotalStressInducedIncludingNeighbors = 0;
    private double cachedTotalStressInducedByThisElement = 0;

    // Global Statistics
    private int totalAgentsCount = 0;
    private int timesFull = 0;
    private double maxCongestion = 0;
    private double sumCongestion = 0;
    private int congestionMeasureCount = 0;

    /**
     * Constructs a graph element with a unique ID and physical capacity.
     *
     * @param id       The unique identifier for this element.
     * @param capacity The maximum safe capacity in square meters (m²).
     */
    protected GraphElement(int id, double capacity) {
        this.id = id;
        this.agents = new ArrayList<>();
        this.capacity = Math.max(0.1, capacity);
        removeFire();
    }

    /**
     * Retrieves the list of structurally connected neighboring elements.
     *
     * @return A list of neighboring {@link GraphElement} instances.
     */
    public abstract List<? extends GraphElement> getNeighbors();

    /**
     * Returns the unique identifier of this element.
     *
     * @return The unique identifier of this element.
     */
    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GraphElement element = (GraphElement) o;
        return getId() == element.getId();
    }

    // =========================================================================
    // 4. AGENT MANAGEMENT (ENTRY / EXIT)
    // =========================================================================

    /**
     * Returns the list of agents currently located on this element.
     *
     * @return The list of agents currently located on this element.
     */
    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Internal helper to handle the common logic of adding an agent and updating
     * statistics.
     *
     * @param a The agent entering the element.
     */
    protected void registerAgentAddition(Agent a) {
        agents.add(a);
        incrementTotalAgentsCount();
        recordCongestionMeasure();
        if (isFull()) {
            incrementTimesFull();
        }
    }

    /**
     * Safely attempts to add an agent to this element if the physical capacity
     * permits.
     *
     * @param a The agent attempting to enter.
     * @return {@code true} if the agent was successfully added, {@code false} if
     *         the element is full.
     */
    public boolean addAgent(Agent a) {
        if (!isFull()) {
            registerAgentAddition(a);
            return true;
        }
        return false;
    }

    /**
     * Forces the addition of an agent into this element regardless of the maximum
     * capacity limit.
     * <p>
     * This method is exclusively used during emergency graph modifications (e.g.,
     * node deletions)
     * where agents must be relocated immediately. Using this may trigger a "Heavy
     * Congestion" state.
     * </p>
     *
     * @param a The agent to forcefully inject into the element.
     * @return Always returns {@code true}.
     */
    public boolean forceAddAgent(Agent a) {
        registerAgentAddition(a);
        return true;
    }

    /**
     * Removes an agent from this element and cleans up any active congestion
     * penalties linked to them.
     *
     * @param a The agent to remove.
     */
    public boolean removeAgent(Agent a) {
        if (agents.remove(a)) {
            recordCongestionMeasure();
            if (congestionWaitTimes != null) {
                congestionWaitTimes.remove(a);
            }
            return true;
        }
        return false;
    }

    // =========================================================================
    // 5. CAPACITY & CONGESTION LOGIC
    // =========================================================================

    /**
     * Returns the maximum safe capacity of this element in square meters.
     *
     * @return the capacity in m²
     */
    public double getCapacity() {
        return capacity;
    }

    /**
     * Sets the maximum safe capacity of this element.
     *
     * @param capacity the new capacity in m² (clamped to a minimum of 0.1)
     */
    public void setCapacity(double capacity) {
        this.capacity = Math.max(0.1, capacity);
    }

    /**
     * Returns the total surface area occupied by all agents currently on this element.
     *
     * @return The total sum of surface areas occupied by all agents currently on
     *         this element.
     */
    public double getOccupiedSpace() {
        double occupied = 0;
        for (Agent agent : agents) {
            occupied += agent.getSurfaceAreaTakenByAgent();
        }
        return occupied;
    }

    /**
     * * Returns the current congestion ratio.
     * Note: This value can exceed 1.0 if the element is heavily congested.
     *
     * @return The ratio of occupied space over total capacity.
     */
    public double getCongestion() {
        return Math.min(getOccupiedSpace() / getCapacity(), 1);
    }

    /**
     * Checks whether this element's capacity limit has been reached.
     *
     * @return {@code true} if the element's capacity limit has been reached or
     *         exceeded.
     */
    public boolean isFull() {
        return getOccupiedSpace() >= capacity;
    }

    /**
     * Checks whether this element is considered congested (occupancy above 70%).
     *
     * @return {@code true} if the current spatial congestion exceeds 70%.
     */
    public boolean isCongested() {
        return getCongestion() > 0.7;
    }

    /**
     * Determines if the element is in a state of "Heavy Congestion".
     * <p>
     * This crisis state occurs when emergency relocations force the local agent
     * population to strictly exceed the infrastructure's maximum physical capacity.
     * </p>
     *
     * @return {@code true} if the currently occupied space is strictly greater than
     *         the capacity.
     */
    public boolean isHeavilyCongested() {
        return getOccupiedSpace() > capacity;
    }

    /**
     * Updates the penalty wait counters for all agents present on this element.
     * <p>
     * If the element is heavily congested, this method increments the wait timer
     * for every agent. If the congestion resolves (falls back below capacity),
     * the penalty trackers are entirely cleared. This should be called every
     * simulation tick.
     * </p>
     */
    public void updateCongestionDelays() {
        if (congestionWaitTimes == null) {
            congestionWaitTimes = new HashMap<>();
        }

        if (isHeavilyCongested()) {
            for (Agent a : agents) {
                congestionWaitTimes.put(a, congestionWaitTimes.getOrDefault(a, 0) + 1);
            }
        } else {
            congestionWaitTimes.clear();
        }
    }


    /**
     * Verifies if a specific agent is authorized to leave this element based on
     * congestion rules.
     * <p>
     * If the element is heavily congested, agents suffer a movement penalty and
     * must wait
     * for a minimum of 2 simulation cycles before they are allowed to transition to
     * another element.
     * </p>
     *
     * @param a The agent attempting to leave.
     * @return {@code true} if the agent can leave, {@code false} if they are
     *         blocked by congestion penalties.
     */
    public boolean canAgentLeave(Agent a) {
        if (isHeavilyCongested()) {
            return congestionWaitTimes.getOrDefault(a, 0) >= 2;
        }
        return true;
    }

    // =========================================================================
    // 6. FIRE MECHANICS
    // =========================================================================

    /**
     * Checks whether this element is currently affected by fire.
     *
     * @return {@code true} if the element is currently affected by fire.
     */
    public boolean isOnFire() {
        return fire != null;
    }

    /**
     * Returns the fire currently affecting this element.
     *
     * @return the {@link Fire} instance, or {@code null} if not on fire
     */
    public Fire getFire() {
        return fire;
    }

    /**
     * Sets the fire affecting this element.
     *
     * @param fire the fire to set, or {@code null} to clear it
     */
    public void setFire(Fire fire) {
        this.fire = fire;
    }

    /**
     * Removes the fire from this element.
     */
    public void removeFire() {
        this.fire = null;
    }

    // =========================================================================
    // 7. STRESS SYSTEM & DECISIONS
    // =========================================================================

    /**
     * Calculates an attractiveness multiplier for an agent based on local
     * conditions.
     *
     * @param agentState Agent decision properties.
     * @param agentPhysicalProperties Agent physical properties.
     * @return Score multiplier (values &lt; 1 decrease attractiveness).
     */
    public double getScoreMultiplierForAgent(AgentDecisionalProperties agentState, AgentPhysicalProperties agentPhysicalProperties) {
        double scoreMult = 1.0;
        if (isOnFire()) {
            Fire f = getFire();
            scoreMult *= 0.1 / (1.0 + (f.getIntensity() + f.getSmokeLevel() + f.getSpreadRate())*100+ (1-agentPhysicalProperties.getHealthPercentage())*10);
        }
        double congestion = getCongestion();
        if (isCongested()) {
            scoreMult *= 0.4 * (1.0 + congestion - agentState.getCongestionTolerance());
        }
        double stress = getStressInducingImpact();
        if (stress > 0.90) {
            scoreMult *= 0.4 * (1.0 + stress - agentState.getStressTolerance());
        }
        return scoreMult;
    }

    /**
     * Computes and caches stress generated by this element alone.
     *
     * @param tickDuration The duration of the simulation step in seconds.
     * @return Cached stress value.
     */
    public double updateStressGeneratedByThisElement(double tickDuration) {
        double stress = 0;
        double congestion = getCongestion();
        stress += congestion * congestion * 0.4;

        double meanStressFromAgents = getAgentsTotalStress() / Math.max(1, getAgents().size());
        stress += meanStressFromAgents * meanStressFromAgents * 0.4;

        if (isOnFire()) {
            stress += 0.2 * getFire().getIntensity();
        }

        cachedTotalStressInducedByThisElement = Math.min(stress, 1.0) * tickDuration / 0.016;
        return cachedTotalStressInducedByThisElement;
    }

    /**
     * Computes and caches stress including neighborhood impact.
     *
     * @return The total stress value.
     */
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
     * Returns the sum of all stress levels for agents currently on this element.
     *
     * @return Sum of stress levels for all agents on this element.
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
     * Returns the cached stress induced by this element alone (excluding neighbors).
     *
     * @return the cached stress value for this element only
     */
    public double getCachedTotalStressInducedByThisElement() {
        return cachedTotalStressInducedByThisElement;
    }

    /**
     * Returns the cached stress induced by this element including neighborhood stress.
     *
     * @return the total cached stress value including neighbors
     */
    public double getCachedTotalStressInducedIncludingNeighbors() {
        return cachedTotalStressInducedIncludingNeighbors;
    }

    /**
     * Returns the damage dealt to an agent for one simulation tick at the default tick duration.
     *
     * @param agent the agent receiving the damage
     * @return the damage amount for this tick
     */
    public double getDamageForAgent(Agent agent) {
        return getDamageForAgent(agent, SimulationSettings.getInstance().getTickDuration());
    }

    /**
     * Returns the damage dealt to an agent over the given duration.
     *
     * @param agent    the agent receiving the damage
     * @param duration the time duration in seconds
     * @return the damage amount for the given duration
     */
    public double getDamageForAgent(Agent agent, double duration) {
        double damage = (getCongestion() > 1 ? 1 : 0) * duration;
        if (isOnFire()) {
            return damage + getFire().getDamageForAgent(duration);
        }
        return damage;
    }

    /**
     * Returns the maximum speed allowed for agents on this element, reduced by congestion.
     *
     * @return the maximum agent speed in m/s
     */
    public double getMaxAgentSpeed() {
        // Capped effective congestion for speed calculation to prevent negative speeds
        double effectiveCongestion = Math.min(getCongestion(), 0.9);
        double congestionFactor = 1.0 - effectiveCongestion;
        double calculatedSpeed = AgentSettings.getInstance().getMAX_RUNNING_SPEED() * congestionFactor;
        return isOnFire() ? calculatedSpeed * 1.5 : Math.max(calculatedSpeed, 0.1);
    }

    // =========================================================================
    // 8. STATISTICS & LIFECYCLE
    // =========================================================================

    /**
     * Increments the total number of agents that have ever passed through this element.
     */
    public void incrementTotalAgentsCount() {
        this.totalAgentsCount++;
    }

    /**
     * Returns the total number of agents that have ever entered this element.
     *
     * @return the total agents count
     */
    public int getTotalAgentsCount() {
        return totalAgentsCount;
    }

    /**
     * Increments the count of times this element has been at full capacity.
     */
    public void incrementTimesFull() {
        this.timesFull++;
    }

    /**
     * Returns the number of times this element has reached full capacity.
     *
     * @return the times-full count
     */
    public int getTimesFull() {
        return timesFull;
    }

    /**
     * Records the current congestion level for statistical purposes.
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
     * Returns the highest congestion level ever recorded for this element.
     *
     * @return the maximum congestion ratio observed
     */
    public double getMaxCongestion() {
        return maxCongestion;
    }

    /**
     * Returns the average congestion level across all recorded measurements.
     *
     * @return the average congestion ratio, or 0 if no measurements have been taken
     */
    public double getAverageCongestion() {
        return (congestionMeasureCount == 0) ? 0 : sumCongestion / congestionMeasureCount;
    }

    /**
     * Returns the total number of congestion measurements taken for this element.
     *
     * @return the congestion measurement count
     */
    public int getCongestionMeasureCount() {
        return congestionMeasureCount;
    }

    /**
     * Saves the current fire state as the initial state to be restored on reset.
     */
    public void setInitialState() {
        this.initialFire = (getFire() != null) ? getFire() : null;
    }

    /**
     * Resets this element to its initial state, clearing agents and statistics.
     */
    public void reset() {
        agents.clear();
        setFire((initialFire != null) ? initialFire : null);
        maxCongestion = 0;
        sumCongestion = 0;
        congestionMeasureCount = 0;
        timesFull = 0;
        totalAgentsCount = 0;
        cachedTotalStressInducedByThisElement = 0;
        cachedTotalStressInducedIncludingNeighbors = 0;
    }

    // =========================================================================
    // 9. SERIALIZATION
    // =========================================================================

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (agents == null)
            agents = new ArrayList<>();
        if (congestionWaitTimes == null)
            congestionWaitTimes = new HashMap<>();
    }
}