package fr.cy.controller;

import fr.cy.model.simulation.Simulation;
import fr.cy.view.GraphCanvas;
import fr.cy.view.GraphRenderer;
import javafx.animation.AnimationTimer;

/**
 * Gère la boucle en temps réel à la manière d'un Game Loop de ~60FPS.
 */
public class SimulationController {

    private Simulation simulation;
    private final GraphRenderer renderer;
    private final GraphCanvas canvas;
    private AnimationTimer timer;
    private boolean isRunning = false;
    private Runnable onRender = null;
    private int stepTicks = 15;

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

                // Notifie le contrôleur principal pour mettre à jour les stats
                if (onRender != null)
                    onRender.run();
            }
        };
    }

    public void setOnRender(Runnable onRender) {
        this.onRender = onRender;
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
        simulation.reset();
        renderer.render(simulation, canvas);
    }

    public void increaseSpeed() {
        simulation.getSimulationSettings().increaseSpeedLevel();
    }

    public void decreaseSpeed() {
        simulation.getSimulationSettings().decreaseSpeedLevel();
    }

    public double getSpeed() {
        return simulation.getSimulationSettings().getSpeedMultiplier();
    }

    public int getStepTicks() {
        return stepTicks;
    }

    public void setStepTicks(int stepTicks) {
        if (stepTicks < 1) {
            throw new IllegalArgumentException("stepTicks must be at least 1");
        }
        this.stepTicks = stepTicks;
    }

    public void stepTick() {
        this.pause();

        for (int i = 0; i < stepTicks; i++) {
            simulation.stepTick();
        }

        renderer.render(simulation, canvas);

        if (onRender != null) {
            onRender.run();
        }
    }

    public void forceRender() {
        renderer.render(simulation, canvas);
    }

    // Fournit l'instance de la simu (pour y chercher un élément par ex)
    public Simulation getSimulation() {
        return simulation;
    }

    public void loadSimulation(Simulation newSimulation) {
        this.simulation = newSimulation;
        this.simulation.getSimulationSettings().setTickDuration(0.016); // 60fps
        this.isRunning = false;
        this.simulation.stop();
        renderer.render(simulation, canvas);
        
        if (onRender != null) {
            onRender.run();
        }
    }
}
