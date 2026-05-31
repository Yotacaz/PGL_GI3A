package fr.cy.controller;

import fr.cy.model.simulation.Simulation;
import fr.cy.view.GraphCanvas;
import fr.cy.view.GraphRenderer;
import javafx.animation.AnimationTimer;

/**
 * Gère la boucle en temps réel à la manière d'un Game Loop de ~60FPS.
 */
public class SimulationController {

    private final Simulation simulation;
    private final GraphRenderer renderer;
    private final GraphCanvas canvas;
    private AnimationTimer timer;
    private boolean isRunning = false;

    public SimulationController(Simulation simulation, GraphCanvas canvas) {
        this.simulation = simulation;
        simulation.getSimulationSettings().setTickDuration(0.016); // 60fps
        this.canvas = canvas;
        this.renderer = new GraphRenderer(canvas.getGraphicsContext2D());

        initGameLoop();
    }

    private void initGameLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Si la simu est en mode Play, on avance la logique (Backend)
                if (isRunning) {
                    simulation.tick();
                }

                // Pour utiliser la caméra, on la passe via le canvas au renderer
                renderer.render(simulation, canvas);
            }
        };
    }

    public void startLoop() {
        timer.start();
    }

    public void play() {
        isRunning = true;
        simulation.start();
    }

    public void pause() {
        isRunning = false;
        simulation.stop();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void reset() {
        isRunning = false;
        simulation.stop();
        // simulation.reset()
        renderer.render(simulation, canvas);
    }

    // Fournit l'instance de la simu (pour y chercher un élément par ex)
    public Simulation getSimulation() {
        return simulation;
    }
}