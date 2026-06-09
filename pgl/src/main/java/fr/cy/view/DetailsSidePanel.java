package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.behaviour.properties.EmotionalState;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

/**
 * The {@code DetailsSidePanel} class provides an animated UI side panel
 * that displays context-aware statistics and management tools for selected
 * simulation components (Agents, Nodes, and Edges).
 */
public class DetailsSidePanel extends ScrollPane {

    private static final double PANEL_WIDTH = 300.0;

    // Core layout containers
    private VBox contentBox;
    private Label panelTitle;

    // Component-specific display blocks
    private VBox capacityBox, congestionBox, stressBox, fireBox, agentsBox;
    private VBox widthBox, lengthBox, speedBox, nodeInfoBox, agentStatsBox, historyBox, actionsBox;

    // Data value labels
    private Label capacityTitle, congestionTitle, stressTitle, fireTitle, agentsTitle;
    private Label capacityValue, congestionValue, stressValue, fireValue, agentsValue;
    private Label widthValue, lengthValue, speedValue, nodeInfoValue;
    private Label avgStressValue, dominantStateValue;
    private Label histTotalValue, histMaxCongValue, histAvgCongValue;

    // Progress visualizer tracking overlays
    private ProgressBar congestionBar, stressBar, fireBar;

    // Action execution buttons (Graph Elements)
    private Button toggleFireBtn;
    private Button deleteBtn;

    // Action execution buttons (Agents)
    private Button deleteAgentBtn;
    private Button killAgentBtn;

    // Interaction triggers and state variables
    private Object currentEntity;
    private Consumer<GraphElement> onToggleFireRequested;
    private Consumer<GraphElement> onDeleteRequested;
    private Consumer<Agent> onDeleteAgentRequested;
    private Consumer<Agent> onKillAgentRequested;

    private boolean isPanelVisible = false;
    private Timeline animationTimeline;

    /**
     * Constructs the side panel and initializes structural interface bindings.
     * Starts layout boundaries in an invisible state.
     */
    public DetailsSidePanel() {
        initUI();

        this.setMinWidth(0);
        this.setPrefWidth(0);
        this.setMaxWidth(0);
        this.setOpacity(0);
        this.setVisible(false);
        this.setManaged(false);
    }

    /**
     * Initializes all UI sub-components, registers click loops, and builds layouts.
     */
    private void initUI() {
        contentBox = new VBox(20);
        contentBox.getStyleClass().add("details-panel");
        contentBox.setMinWidth(PANEL_WIDTH);
        contentBox.setPrefWidth(PANEL_WIDTH);

        panelTitle = new Label("ELEMENT DETAILS");
        panelTitle.getStyleClass().add("panel-title");

        initializeLabelsAndProgressBars();
        buildLayoutStructure();
        setupActionListeners();

        this.setContent(contentBox);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    /**
     * Instantiates descriptive display fields and bar track wrappers.
     */
    private void initializeLabelsAndProgressBars() {
        // Base metric values
        capacityValue = new Label("0");
        congestionValue = new Label("0%");
        stressValue = new Label("0.0");
        fireValue = new Label("0.0");
        agentsValue = new Label("0");
        widthValue = new Label("0");
        lengthValue = new Label("0");
        speedValue = new Label("0.0 m/s");
        nodeInfoValue = new Label("--");
        avgStressValue = new Label("--");
        dominantStateValue = new Label("--");
        histTotalValue = new Label("--");
        histMaxCongValue = new Label("--");
        histAvgCongValue = new Label("--");

        // Apply CSS style profile configurations
        agentsValue.getStyleClass().add("stat-value-highlight");
        speedValue.getStyleClass().add("stat-value-highlight");

        Label[] generalLabels = {
                capacityValue, congestionValue, stressValue, fireValue, widthValue,
                lengthValue, nodeInfoValue, avgStressValue, dominantStateValue,
                histTotalValue, histMaxCongValue, histAvgCongValue
        };
        for (Label label : generalLabels) {
            label.getStyleClass().add("stat-value");
        }

        // Initialize progress tracker visualizations
        congestionBar = createProgressBar("congestion-bar");
        stressBar = createProgressBar("stress-bar");
        fireBar = createProgressBar("fire-bar");

        // Infrastructure action buttons
        toggleFireBtn = new Button("🔥 Trigger Fire");
        toggleFireBtn.setMaxWidth(Double.MAX_VALUE);
        toggleFireBtn.getStyleClass().add("action-btn");

        deleteBtn = new Button("🗑 Delete Element");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.getStyleClass().addAll("action-btn", "danger-btn");

        // Agent action buttons
        deleteAgentBtn = new Button("🗑 Delete Agent");
        deleteAgentBtn.setMaxWidth(Double.MAX_VALUE);
        deleteAgentBtn.getStyleClass().addAll("action-btn", "danger-btn");

        killAgentBtn = new Button("💀 Kill Agent");
        killAgentBtn.setMaxWidth(Double.MAX_VALUE);
        killAgentBtn.getStyleClass().addAll("action-btn", "danger-btn");
    }

    /**
     * Assembles visual component blocks into their unified parent display layout.
     */
    private void buildLayoutStructure() {
        capacityTitle = new Label("CAPACITY");
        congestionTitle = new Label("CONGESTION");
        stressTitle = new Label("STRESS LEVEL");
        fireTitle = new Label("FIRE INTENSITY");
        agentsTitle = new Label("AGENTS ON SITE");

        capacityBox = createStatBox(capacityTitle, capacityValue);
        congestionBox = createStatBox(congestionTitle, new VBox(4, congestionValue, congestionBar));
        stressBox = createStatBox(stressTitle, new VBox(4, stressValue, stressBar));
        fireBox = createStatBox(fireTitle, new VBox(4, fireValue, fireBar));
        agentsBox = createStatBox(agentsTitle, agentsValue);

        widthBox = createStatBox(new Label("WIDTH"), widthValue);
        lengthBox = createStatBox(new Label("LENGTH"), lengthValue);
        speedBox = createStatBox(new Label("CURRENT SPEED"), speedValue);
        nodeInfoBox = createStatBox(new Label("NODE INFO"), nodeInfoValue);

        agentStatsBox = createMultiStatBox("AGENT STATS", avgStressValue, dominantStateValue);
        historyBox = createMultiStatBox("HISTORY", histTotalValue, histMaxCongValue, histAvgCongValue);

        // Include all contextual action buttons inside the core actions VBox container
        actionsBox = createStatBox(new Label("ACTIONS"),
                new VBox(10, toggleFireBtn, deleteBtn, deleteAgentBtn, killAgentBtn));

        Pane structuralSpacer = new Pane();
        VBox.setVgrow(structuralSpacer, Priority.ALWAYS);

        contentBox.getChildren().addAll(
                panelTitle, new Separator(),
                nodeInfoBox, speedBox, capacityBox, congestionBox, stressBox, fireBox, agentsBox,
                agentStatsBox, widthBox, lengthBox, historyBox,
                new Separator(), actionsBox, structuralSpacer);
    }

    /**
     * Binds controller callback actions directly onto layout buttons.
     */
    private void setupActionListeners() {
        toggleFireBtn.setOnAction(e -> {
            if (currentEntity instanceof GraphElement element && onToggleFireRequested != null) {
                onToggleFireRequested.accept(element);
            }
        });

        deleteBtn.setOnAction(e -> {
            if (currentEntity instanceof GraphElement element && onDeleteRequested != null) {
                onDeleteRequested.accept(element);
                hidePanel();
            }
        });

        deleteAgentBtn.setOnAction(e -> {
            if (currentEntity instanceof Agent agent && onDeleteAgentRequested != null) {
                onDeleteAgentRequested.accept(agent);
                hidePanel();
            }
        });

        killAgentBtn.setOnAction(e -> {
            if (currentEntity instanceof Agent agent && onKillAgentRequested != null) {
                onKillAgentRequested.accept(agent);
                hidePanel();
            }
        });
    }

    /** Helper factory to configure standard progress bars. */
    private ProgressBar createProgressBar(String styleClass) {
        ProgressBar bar = new ProgressBar(0);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setMinHeight(12);
        bar.getStyleClass().addAll("progress-bar", styleClass);
        return bar;
    }

    /** Helper factory to create single metric layout containers. */
    private VBox createStatBox(Label title, javafx.scene.Node valueContainer) {
        VBox box = new VBox(8);
        box.getStyleClass().add("stat-box");
        title.getStyleClass().add("stat-title");
        box.getChildren().addAll(title, valueContainer);
        return box;
    }

    /** Helper factory to build compound group statistic panels. */
    private VBox createMultiStatBox(String sectionTitle, Label... labels) {
        VBox box = new VBox(8);
        box.getStyleClass().add("stat-box");
        Label title = new Label(sectionTitle);
        title.getStyleClass().add("stat-title");
        box.getChildren().add(title);
        box.getChildren().addAll(labels);
        return box;
    }

    private void setBoxVisible(VBox box, boolean visible) {
        box.setVisible(visible);
        box.setManaged(visible);
    }

    private void setElementVisible(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void safeSetProgress(ProgressBar bar, double value) {
        bar.setProgress(Math.max(0.0, Math.min(1.0, Double.isNaN(value) || Double.isInfinite(value) ? 0.0 : value)));
    }

    public void setOnToggleFireRequested(Consumer<GraphElement> listener) {
        this.onToggleFireRequested = listener;
    }

    public void setOnDeleteRequested(Consumer<GraphElement> listener) {
        this.onDeleteRequested = listener;
    }

    /** Sets the consumer to be called when an agent deletion is requested. */
    public void setOnDeleteAgentRequested(Consumer<Agent> listener) {
        this.onDeleteAgentRequested = listener;
    }

    /** Sets the consumer to be called when an agent elimination is requested. */
    public void setOnKillAgentRequested(Consumer<Agent> listener) {
        this.onKillAgentRequested = listener;
    }

    /** Animates the panel smoothly into the viewport. */
    public void showPanel() {
        if (isPanelVisible)
            return;
        isPanelVisible = true;
        if (animationTimeline != null)
            animationTimeline.stop();

        this.setManaged(true);
        this.setVisible(true);

        animationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(this.maxWidthProperty(), this.getWidth()),
                        new KeyValue(this.minWidthProperty(), this.getWidth()),
                        new KeyValue(this.prefWidthProperty(), this.getWidth()),
                        new KeyValue(this.opacityProperty(), this.getOpacity())),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(this.maxWidthProperty(), PANEL_WIDTH, Interpolator.EASE_OUT),
                        new KeyValue(this.minWidthProperty(), PANEL_WIDTH, Interpolator.EASE_OUT),
                        new KeyValue(this.prefWidthProperty(), PANEL_WIDTH, Interpolator.EASE_OUT),
                        new KeyValue(this.opacityProperty(), 1.0, Interpolator.EASE_OUT)));
        animationTimeline.play();
    }

    /** Animates the panel out of the viewport. */
    public void hidePanel() {
        if (!isPanelVisible)
            return;
        isPanelVisible = false;
        if (animationTimeline != null)
            animationTimeline.stop();

        animationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(this.maxWidthProperty(), this.getWidth()),
                        new KeyValue(this.minWidthProperty(), this.getWidth()),
                        new KeyValue(this.prefWidthProperty(), this.getWidth()),
                        new KeyValue(this.opacityProperty(), this.getOpacity())),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(this.maxWidthProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(this.minWidthProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(this.prefWidthProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(this.opacityProperty(), 0.0, Interpolator.EASE_IN)));
        animationTimeline.setOnFinished(e -> {
            this.setManaged(false);
            this.setVisible(false);
        });
        animationTimeline.play();
    }

    /**
     * Updates content metrics mapping dynamically relative to the runtime selected
     * entity type.
     *
     * @param entity        The selected runtime item (Agent, Node, or Edge).
     * @param agentSettings Global configuration parameters.
     */
    public void update(Object entity, AgentSettings agentSettings) {
        if (entity == null)
            return;
        this.currentEntity = entity;

        // Reset visibility of entity-specific blocks
        VBox[] modularBoxes = { widthBox, lengthBox, nodeInfoBox, agentStatsBox, historyBox, speedBox, actionsBox };
        for (VBox box : modularBoxes) {
            setBoxVisible(box, false);
        }

        if (entity instanceof Agent agent) {
            updateAgentDetails(agent, agentSettings);
        } else if (currentEntity instanceof GraphElement element) {
            updateElementDetails(element);
        }
    }

    /**
     * Formats and populates layout nodes with custom agent tracking data.
     */
    private void updateAgentDetails(Agent agent, AgentSettings agentSettings) {
        panelTitle.setText("AGENT #" + agent.getId());
        setBoxVisible(speedBox, true);
        speedValue.setText(String.format("%.2f m/s", agent.getEffectiveSpeed(agentSettings)));

        // Setup Agent context titles cleanly
        capacityTitle.setText("POSITION");
        congestionTitle.setText("EDGE PROGRESS");
        stressTitle.setText("STRESS LEVEL");
        fireTitle.setText("EMOTIONAL STATE");
        agentsTitle.setText("UNIQUE ID");

        // Process position and navigation progress metrics
        if (agent.isOnNode() && agent.getCurrentNode() != null) {
            capacityValue.setText("Node " + agent.getCurrentNode().getId());
            safeSetProgress(congestionBar, 0);
        } else if (agent.getCurrentOrPreviousEdge() != null) {
            Node target = agent.getCurrentOrPreviousEdge().getOppositeNode(agent.getPreviousOrCurrentNode());
            capacityValue.setText("Towards Node " + target.getId());
            safeSetProgress(congestionBar, agent.getCurrentEdgeProgress());
        }

        congestionValue.setText(String.format("%.0f%%", agent.getCurrentEdgeProgress() * 100));
        stressValue.setText(String.format("%.0f%%", agent.getStressLevel() * 100));
        safeSetProgress(stressBar, agent.getStressLevel());

        fireValue.setText(agent.getEmotionalState().name());
        agentsValue.setText("#" + agent.getId());

        // Manage context button visibilities for Agent types
        setBoxVisible(actionsBox, true);
        setElementVisible(deleteAgentBtn, true);
        setElementVisible(killAgentBtn, true);
        setElementVisible(toggleFireBtn, false);
        setElementVisible(deleteBtn, false);
    }

    /**
     * Formats and populates layout nodes with infrastructure graph component
     * metrics.
     */
    private void updateElementDetails(GraphElement element) {
        setBoxVisible(actionsBox, true);

        // Update operational fire button tracking states
        if (element.isOnFire()) {
            toggleFireBtn.setText("🧯 Extinguish");
            toggleFireBtn.getStyleClass().add("danger-btn");
        } else {
            toggleFireBtn.setText("🔥 Trigger Fire");
            toggleFireBtn.getStyleClass().remove("danger-btn");
        }

        // Manage context button visibilities for Infrastructure types
        setElementVisible(deleteAgentBtn, false);
        setElementVisible(killAgentBtn, false);
        setElementVisible(toggleFireBtn, true);
        setElementVisible(deleteBtn, true);

        // Restore default layout semantic titles
        capacityTitle.setText("CAPACITY");
        congestionTitle.setText("CONGESTION");
        stressTitle.setText("STRESS LEVEL");
        fireTitle.setText("FIRE INTENSITY");
        agentsTitle.setText("AGENTS ON SITE");

        panelTitle.setText((element instanceof Node ? "NODE" : "EDGE") + " #" + element.getId());
        capacityValue.setText(String.format("%.1f", element.getCapacity()));

        double congestion = element.getCongestion();
        congestionValue.setText(String.format("%.1f%%", congestion * 100));
        safeSetProgress(congestionBar, congestion);

        double stress = element.getStressInducingImpact();
        stressValue.setText(String.format("%.0f%%", stress * 100));
        safeSetProgress(stressBar, stress);

        // Update hazardous properties
        if (element.isOnFire()) {
            fireValue.setText(String.format("%.2f (Smoke %.2f)", element.getFire().getIntensity(),
                    element.getFire().getSmokeLevel()));
            safeSetProgress(fireBar, element.getFire().getIntensity());
        } else {
            fireValue.setText("None");
            safeSetProgress(fireBar, 0);
        }

        List<Agent> agents = element.getAgents();
        agentsValue.setText(String.valueOf(agents.size()));

        // Branch parameters based on specific element subclasses
        if (element instanceof Node node) {
            nodeInfoValue.setText(
                    String.format("(%.0f, %.0f) %s", node.getX(), node.getY(), node.isExit() ? "★ EXIT" : "node"));
            setBoxVisible(nodeInfoBox, true);
        } else if (element instanceof Edge edge) {
            widthValue.setText(String.format("%.2f", edge.getWidth()));
            lengthValue.setText(String.format("%.2f", edge.getLength()));
            setBoxVisible(widthBox, true);
            setBoxVisible(lengthBox, true);
        }

        // Compute group metrics if agents populate the workspace
        if (!agents.isEmpty()) {
            double avgStress = agents.stream().mapToDouble(Agent::getStressLevel).average().orElse(0);
            avgStressValue.setText("Avg Stress: " + String.format("%.0f%%", avgStress * 100));
            dominantStateValue.setText("State: " + computeDominantState(agents).name());
            setBoxVisible(agentStatsBox, true);
        }

        // Update history collection logs
        histTotalValue.setText("Total passed: " + element.getTotalAgentsCount());
        histMaxCongValue.setText("Max Congestion: " + String.format("%.0f%%", element.getMaxCongestion() * 100));
        histAvgCongValue.setText("Avg Congestion: " + String.format("%.0f%%", element.getAverageCongestion() * 100));
        setBoxVisible(historyBox, true);
    }

    /**
     * Determines the primary emotional state trend within an agent population.
     */
    private EmotionalState computeDominantState(List<Agent> agents) {
        int calm = 0, selfish = 0, panicking = 0;
        for (Agent a : agents) {
            switch (a.getEmotionalState()) {
                case CALM -> calm++;
                case SELFISH -> selfish++;
                case PANICKING -> panicking++;
            }
        }
        if (panicking >= calm && panicking >= selfish)
            return EmotionalState.PANICKING;
        if (selfish >= calm)
            return EmotionalState.SELFISH;
        return EmotionalState.CALM;
    }
}