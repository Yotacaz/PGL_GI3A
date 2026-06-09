package fr.cy.util;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Manages the generation and recycling of integer identifiers.
 * <p>
 * This class optimizes ID usage by prioritizing the reuse of identifiers
 * previously released via {@link #releaseId(int)} before generating new
 * incremental identifiers.
 * </p>
 */
public class IdManager implements Serializable {

    /** Serialization identifier. */
    private static final long serialVersionUID = 1L;

    /** The next available incremental ID if the free queue is empty. */
    private int nextId;

    /** A queue storing IDs that have been released and are available for reuse. */
    private final Queue<Integer> freeIds;

    /**
     * Constructs a new {@code IdManager} initialized to start generating IDs from
     * 0.
     */
    public IdManager() {
        this.nextId = 0;
        this.freeIds = new ArrayDeque<>();
    }

    /**
     * Generates an available identifier.
     * <p>
     * If there are identifiers in the {@code freeIds} queue, the method
     * retrieves and returns the head of that queue. Otherwise, it returns
     * the current {@code nextId} and increments it.
     * </p>
     *
     * @return An available unique integer identifier.
     */
    public int generateId() {
        if (!freeIds.isEmpty()) {
            return freeIds.poll();
        }
        return nextId++;
    }

    /**
     * Releases an identifier, making it available for future reuse.
     *
     * @param id The identifier to be returned to the pool of free IDs.
     */
    public void releaseId(int id) {
        freeIds.add(id);
    }
}