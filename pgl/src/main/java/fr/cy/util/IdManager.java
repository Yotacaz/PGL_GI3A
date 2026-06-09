package fr.cy.util;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Queue;

import java.io.Serializable;

/**
 * Gère la génération et la réutilisation d'identifiants entiers.
 * <p>
 * Les identifiants libérés via {@link #releaseId(int)} sont réattribués en
 * priorité
 * avant de créer de nouveaux identifiants.
 */
public class IdManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private int nextId;

    private Queue<Integer> freeIds;

    /**
     * Crée un gestionnaire d'identifiants initialisé à 0.
     */
    public IdManager() {
        this.nextId = 0;
        this.freeIds = new ArrayDeque<>();
    }

    /**
     * Génère un identifiant disponible.
     * <p>
     * Si un identifiant a été libéré, il est réutilisé. Sinon, un nouvel
     * identifiant incrémental est créé.
     *
     * @return un identifiant disponible
     */
    public int generateId() {

        if (!freeIds.isEmpty()) {
            return freeIds.poll();
        }
        return nextId++;
    }

    /**
     * Libère un identifiant pour permettre sa réutilisation ultérieure.
     *
     * @param id identifiant à remettre dans la file des identifiants libres
     */
    public void releaseId(int id) {
        freeIds.add(id);
    }
}
