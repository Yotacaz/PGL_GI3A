package fr.cy.controller;

import fr.cy.model.simulation.Simulation;
import fr.cy.view.GraphCanvas;
import fr.cy.view.GraphRenderer;
import javafx.animation.AnimationTimer;

/**
 * The {@code SimulationController} manages the main simulation execution loop.
 * <p>
 * It acts as the bridge between the {@link Simulation} model and the view
 * components, running at a fixed frame rate (~60 FPS) to handle both logic
 * updates and graphical rendering.
 * </p>
 */
public class SimulationController {

    private Simulation simulation;
    private final GraphRenderer renderer;
    private final GraphCanvas canvas;
    private AnimationTimer timer;

    private boolean isRunning = false;
    private Runnable onRender = null;
    private int stepTicks = 15;

    // --- TPS (Ticks Per Second) Tracking ---
    private int currentTps = 0;
    private int frameCount = 0;
    private long lastFpsTime = 0;

    /**
     * Constructs the controller and initializes the game loop.
     * * @param simulation The simulation model to control.
     * 
     * @param canvas The canvas used for rendering.
     */
    public SimulationController(Simulation simulation, GraphCanvas canvas) {
        this.simulation = simulation;
        this.simulation.getSimulationSettings().setTickDuration(0.016); // Target ~60 FPS
        this.canvas = canvas;
        this.renderer = new GraphRenderer(canvas.getGraphicsContext2D());

        initGameLoop();
    }

    /**
     * Initializes the {@link AnimationTimer} representing the simulation's game
     * loop.
     * Handles logic advancement (if running), triggers UI rendering, and calculates
     * active TPS.
     */
    private void initGameLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // 1. Calculate TPS (Frames Per Second)
                if (lastFpsTime == 0) {
                    lastFpsTime = now;
                }
                frameCount++;
                if (now - lastFpsTime >= 1_000_000_000L) { // 1 second has elapsed
                    currentTps = frameCount;
                    frameCount = 0;
                    lastFpsTime = now;
                }

                // 2. Advance logic if simulation is in play mode
                if (isRunning) {
                    simulation.tick();
                }

                // 3. Render the current state
                renderer.render(simulation, canvas);

                // 4. Notify UI components of the frame update
                if (onRender != null) {
                    onRender.run();
                }
            }
        };
    }

    /**
     * Sets a callback to be executed on every frame render.
     * * @param onRender The runnable to execute.
     */
    public void setOnRender(Runnable onRender) {
        this.onRender = onRender;
    }

    /** Starts the animation timer loop. */
    public void startLoop() {
        timer.start();
    }

    /** Resumes simulation execution. */
    public void play() {
        isRunning = true;
        simulation.start();
    }

    /** Pauses simulation execution. */
    public void pause() {
        isRunning = false;
        simulation.stop();
    }

    /** @return True if the simulation is currently playing. */
    public boolean isRunning() {
        return isRunning;
    }

    /** Resets the simulation to its initial state. */
    public void reset() {
        isRunning = false;
        simulation.stop();
        simulation.reset();

        // Reset TPS counters on restart
        currentTps = 0;
        frameCount = 0;
        lastFpsTime = 0;

        renderer.render(simulation, canvas);
    }

    /** Increases simulation speed multiplier. */
    public void increaseSpeed() {
        simulation.getSimulationSettings().increaseSpeedLevel();
    }

    /** Decreases simulation speed multiplier. */
    public void decreaseSpeed() {
        simulation.getSimulationSettings().decreaseSpeedLevel();
    }

    /** @return The current speed multiplier. */
    public double getSpeed() {
        return simulation.getSimulationSettings().getSpeedMultiplier();
    }

    /** @return The number of ticks to advance per step request. */
    public int getStepTicks() {
        return stepTicks;
    }

    /**
     * Sets the number of ticks to advance per step.
     * * @param stepTicks Number of ticks (> 0).
     */
    public void setStepTicks(int stepTicks) {
        if (stepTicks < 1) {
            throw new IllegalArgumentException("stepTicks must be at least 1");
        }
        this.stepTicks = stepTicks;
    }

    /**
     * Advances the simulation by the defined number of steps.
     */
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

    /** Forces an immediate rendering of the current simulation state. */
    public void forceRender() {
        renderer.render(simulation, canvas);
    }

    /** @return The active simulation instance. */
    public Simulation getSimulation() {
        return simulation;
    }

    /**
     * Replaces the current simulation with a new instance and resets the state.
     * * @param newSimulation The simulation to load.
     */
    public void loadSimulation(Simulation newSimulation) {
        this.simulation = newSimulation;
        this.simulation.getSimulationSettings().setTickDuration(0.016);
        this.isRunning = false;
        this.simulation.stop();

        // Reset TPS counters for the new simulation
        currentTps = 0;
        frameCount = 0;
        lastFpsTime = 0;

        renderer.render(simulation, canvas);

        if (onRender != null) {
            onRender.run();
        }
    }

    /**
     * Retrieves the current real-time frames per second (Ticks Per Second)
     * processed by the animation loop.
     * * @return The current TPS count.
     */
    public int getCurrentTps() {
        return currentTps;
    }
}