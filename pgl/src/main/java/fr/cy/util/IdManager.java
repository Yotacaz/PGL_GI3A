package fr.cy.util;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import java.io.Serializable;

/**
 * Manage the generation and recycling of unique identifiers for graph elements (nodes and edges).
 * <p>
 * Ids {@link #releaseId(int)} are reattributed with priority
 * before generating new ones with {@link #generateId()}.
 */
public class IdManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private int nextId;

    private Queue<Integer> freeIds;

    /**
     * Constructor for the IdManager class.
     */
    public IdManager() {
        this.nextId = 0;
        this.freeIds = new ArrayDeque<>();
    }

    /**
     * Generates a new unique identifier. If there are any previously released identifiers, they are reused.
     * <p>
     * If an identifier has been released, it is reused. Otherwise, a new
     * incremental identifier is created.
     *
     * @return a available identifier
     */
    public int generateId() {

        if (!freeIds.isEmpty()) {
            return freeIds.poll();
        }
        return nextId++;
    }

    /**
     * Releases an identifier for future reuse.
     *
     * @param id the identifier to release
     */
    public void releaseId(int id) {
        freeIds.add(id);
    }
}
