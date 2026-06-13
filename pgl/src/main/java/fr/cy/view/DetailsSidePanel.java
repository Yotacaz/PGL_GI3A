package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;
import fr.cy.model.agent.properties.EmotionalState;
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
    private VBox widthBox, lengthBox, speedBox, nodeInfoBox, agentStatsBox, historyBox, actionsBox, healthBox;

    // Data value labels
    private Label capacityTitle, congestionTitle, stressTitle, fireTitle, agentsTitle, healthTitle;
    private Label capacityValue, congestionValue, stressValue, fireValue, agentsValue, healthValue;
    private Label widthValue, lengthValue, speedValue, nodeInfoValue;
    private Label avgStressValue, dominantStateValue;
    private Label histTotalValue, histMaxCongValue, histAvgCongValue;

    // Progress visualizer tracking overlays
    private ProgressBar congestionBar, stressBar, fireBar, healthBar;

    // Action execution buttons (Graph Elements)
    private Button toggleFireBtn;
    private Button deleteBtn;

    // Action execution buttons (Agents)
    private Button deleteAgentBtn;
    private Button killAgentBtn;

    // Editable controls — Node
    private VBox editNodeBox;
    private Spinner<Double> capacitySpinner;
    private CheckBox exitCheckBox;

    // Editable controls — Edge
    private VBox editEdgeBox;
    private Spinner<Double> widthSpinner;
    private Spinner<Double> lengthSpinner;
    private CheckBox directedCheckBox;
    private Button reverseDirectionBtn;

    // Edit callbacks
    private Consumer<Double> onNodeCapacityChanged;
    private Consumer<Boolean> onNodeExitChanged;
    private Consumer<Double> onEdgeWidthChanged;
    private Consumer<Double> onEdgeLengthChanged;
    private Consumer<Boolean> onEdgeDirectedChanged;
    private Runnable onReverseEdgeDirectionRequested;

    // Interaction triggers and state variables
    private Object currentEntity;
    private Consumer<GraphElement> onToggleFireRequested;
    private Consumer<GraphElement> onDeleteRequested;
    private Consumer<Agent> onDeleteAgentRequested;
    private Consumer<Agent> onKillAgentRequested;

    private boolean isPanelVisible = false;
    private boolean updatingUI = false;
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

        this.getStyleClass().add("no-scrollbar-pane");
    }

    /**
     * Initializes all UI sub-components, registers click loops, and builds layouts.
     */
    private void initUI() {
        contentBox = new VBox(10);
        contentBox.getStyleClass().add("details-panel");
        contentBox.setMinWidth(PANEL_WIDTH);
        contentBox.setPrefWidth(PANEL_WIDTH);

        panelTitle = new Label("ELEMENT DETAILS");
        panelTitle.getStyleClass().add("panel-title");

        initializeLabelsAndProgressBars();
        buildEditControls();
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
        healthValue = new Label("0 / 0");
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
        healthValue.getStyleClass().add("stat-value-highlight");

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
        healthBar = createProgressBar("health-bar");

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
     * Creates spinners and checkboxes used for inline editing of graph element properties.
     */
    private void buildEditControls() {
        capacitySpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 1_000_000.0, 50.0, 10.0));
        capacitySpinner.setEditable(true);
        capacitySpinner.setMaxWidth(Double.MAX_VALUE);
        commitSpinnerOnBlur(capacitySpinner);

        exitCheckBox = new CheckBox("Exit node");

        widthSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 1_000.0, 2.0, 0.5));
        widthSpinner.setEditable(true);
        widthSpinner.setMaxWidth(Double.MAX_VALUE);
        commitSpinnerOnBlur(widthSpinner);

        lengthSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 100_000.0, 10.0, 5.0));
        lengthSpinner.setEditable(true);
        lengthSpinner.setMaxWidth(Double.MAX_VALUE);
        commitSpinnerOnBlur(lengthSpinner);

        directedCheckBox = new CheckBox("One-way (directed)");

        reverseDirectionBtn = new Button("↔ Invert Direction");
        reverseDirectionBtn.setMaxWidth(Double.MAX_VALUE);
        reverseDirectionBtn.getStyleClass().add("action-btn");
    }

    /** Commits the spinner editor text on focus loss so the value stays consistent. */
    private void commitSpinnerOnBlur(Spinner<Double> spinner) {
        spinner.focusedProperty().addListener((obs, was, isNow) -> {
            if (!isNow) {
                try {
                    double v = Double.parseDouble(spinner.getEditor().getText().trim().replace(',', '.'));
                    ((SpinnerValueFactory.DoubleSpinnerValueFactory) spinner.getValueFactory()).setValue(v);
                } catch (NumberFormatException ignored) {
                    // restore the last valid value
                    spinner.getEditor().setText(spinner.getValue().toString());
                }
            }
        });
    }

    /**
     * Assembles visual component blocks into their unified parent display layout.
     */
    private void buildLayoutStructure() {
        // Initialize Titles
        capacityTitle = new Label();
        congestionTitle = new Label();
        stressTitle = new Label();
        fireTitle = new Label();
        agentsTitle = new Label();
        healthTitle = new Label("Health");

        // Core component cards (Always visible, repurposed based on selection)
        capacityBox = createStatCard(capacityTitle, capacityValue, null);
        congestionBox = createStatCard(congestionTitle, congestionValue, congestionBar);
        stressBox = createStatCard(stressTitle, stressValue, stressBar);
        fireBox = createStatCard(fireTitle, fireValue, fireBar);
        agentsBox = createStatCard(agentsTitle, agentsValue, null);

        // Modular component cards (Hidden/Shown dynamically)
        healthBox = createStatCard(healthTitle, healthValue, healthBar);
        widthBox = createStatCard(new Label("Width"), widthValue, null);
        lengthBox = createStatCard(new Label("Length"), lengthValue, null);
        speedBox = createStatCard(new Label("Current speed"), speedValue, null);
        nodeInfoBox = createStatCard(new Label("Position"), nodeInfoValue, null);

        // Multi-line sections (History, Global Stats)
        agentStatsBox = createMultiStatCard("AGENT STATS",
                new String[] { "Avg Stress", "Dominant State" },
                avgStressValue, dominantStateValue);

        historyBox = createMultiStatCard("HISTORY",
                new String[] { "Total passed", "Max Congestion", "Avg Congestion" },
                histTotalValue, histMaxCongValue, histAvgCongValue);

        // Node edit section
        Label nodeEditHeader = new Label("EDIT NODE");
        nodeEditHeader.getStyleClass().add("section-header");
        editNodeBox = new VBox(10, nodeEditHeader, buildNodeEditCard());
        VBox.setMargin(editNodeBox, new javafx.geometry.Insets(15, 0, 0, 0));

        // Edge edit section
        Label edgeEditHeader = new Label("EDIT EDGE");
        edgeEditHeader.getStyleClass().add("section-header");
        editEdgeBox = new VBox(10, edgeEditHeader, buildEdgeEditCard());
        VBox.setMargin(editEdgeBox, new javafx.geometry.Insets(15, 0, 0, 0));

        // Actions Section
        Label actionsHeader = new Label("ACTIONS");
        actionsHeader.getStyleClass().add("section-header");
        actionsBox = new VBox(10, actionsHeader, toggleFireBtn, deleteBtn, deleteAgentBtn, killAgentBtn);
        VBox.setMargin(actionsBox, new javafx.geometry.Insets(15, 0, 0, 0));

        Pane structuralSpacer = new Pane();
        VBox.setVgrow(structuralSpacer, Priority.ALWAYS);

        contentBox.getChildren().addAll(
                panelTitle, new Separator(),
                nodeInfoBox, healthBox, speedBox, capacityBox, congestionBox, stressBox, fireBox, agentsBox,
                widthBox, lengthBox,
                agentStatsBox, historyBox,
                editNodeBox, editEdgeBox,
                new Separator(), actionsBox, structuralSpacer);
    }

    /** Builds the editable card for node properties (capacity + exit checkbox). */
    private VBox buildNodeEditCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");

        Label capLabel = new Label("Capacity (m²)");
        capLabel.getStyleClass().add("stat-title");
        javafx.scene.layout.HBox capRow = new javafx.scene.layout.HBox(10);
        capRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, Priority.ALWAYS);
        capacitySpinner.setPrefWidth(115);
        capRow.getChildren().addAll(capLabel, spacer, capacitySpinner);

        card.getChildren().addAll(capRow, exitCheckBox);
        return card;
    }

    /** Builds the editable card for edge properties (width, length, directed). */
    private VBox buildEdgeEditCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("stat-card");

        Label wLabel = new Label("Width (m)");
        wLabel.getStyleClass().add("stat-title");
        javafx.scene.layout.HBox wRow = new javafx.scene.layout.HBox(10);
        wRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.Region sp1 = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(sp1, Priority.ALWAYS);
        widthSpinner.setPrefWidth(115);
        wRow.getChildren().addAll(wLabel, sp1, widthSpinner);

        Label lLabel = new Label("Length (m)");
        lLabel.getStyleClass().add("stat-title");
        javafx.scene.layout.HBox lRow = new javafx.scene.layout.HBox(10);
        lRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.Region sp2 = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(sp2, Priority.ALWAYS);
        lengthSpinner.setPrefWidth(115);
        lRow.getChildren().addAll(lLabel, sp2, lengthSpinner);

        card.getChildren().addAll(wRow, lRow, directedCheckBox, reverseDirectionBtn);
        return card;
    }

    /**
     * Helper: Creates a "Card" with the title on the left and the value on the
     * right.
     */
    private VBox createStatCard(Label title, Label value, ProgressBar bar) {
        title.getStyleClass().add("stat-title");
        value.getStyleClass().add("stat-value");

        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Spacer to push the title left and the value right
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(title, spacer, value);

        VBox card = new VBox(5);
        card.getStyleClass().add("stat-card");
        card.getChildren().add(row);

        // Append the progress bar below if it exists
        if (bar != null) {
            bar.setMinHeight(4);
            card.getChildren().add(bar);
        }
        return card;
    }

    /**
     * Helper: Creates a section with a Header followed by a large card containing
     * multiple rows.
     */
    private VBox createMultiStatCard(String sectionTitleText, String[] rowTitles, Label... values) {
        VBox container = new VBox(10);
        VBox.setMargin(container, new javafx.geometry.Insets(15, 0, 0, 0));

        Label sectionTitle = new Label(sectionTitleText);
        sectionTitle.getStyleClass().add("section-header");
        container.getChildren().add(sectionTitle);

        VBox card = new VBox(12);
        card.getStyleClass().add("stat-card");

        for (int i = 0; i < rowTitles.length; i++) {
            Label rowTitle = new Label(rowTitles[i]);
            rowTitle.getStyleClass().add("stat-title");
            values[i].getStyleClass().add("stat-value");

            javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, Priority.ALWAYS);

            row.getChildren().addAll(rowTitle, spacer, values[i]);
            card.getChildren().add(row);
        }

        container.getChildren().add(card);
        return container;
    }

    /**
     * Binds controller callback actions directly onto layout buttons and edit controls.
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

        // Node edit listeners
        capacitySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingUI && newVal != null && onNodeCapacityChanged != null)
                onNodeCapacityChanged.accept(newVal);
        });

        exitCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingUI && onNodeExitChanged != null)
                onNodeExitChanged.accept(newVal);
        });

        // Edge edit listeners
        widthSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingUI && newVal != null && onEdgeWidthChanged != null)
                onEdgeWidthChanged.accept(newVal);
        });

        lengthSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingUI && newVal != null && onEdgeLengthChanged != null)
                onEdgeLengthChanged.accept(newVal);
        });

        directedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingUI && onEdgeDirectedChanged != null)
                onEdgeDirectedChanged.accept(newVal);
            setElementVisible(reverseDirectionBtn, newVal);
        });

        reverseDirectionBtn.setOnAction(e -> {
            if (onReverseEdgeDirectionRequested != null)
                onReverseEdgeDirectionRequested.run();
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

    /** Sets common semantic titles dynamically based on context. */
    private void setCommonTitles(String capacity, String congestion, String stress, String fire, String agents) {
        capacityTitle.setText(capacity);
        congestionTitle.setText(congestion);
        stressTitle.setText(stress);
        fireTitle.setText(fire);
        agentsTitle.setText(agents);
    }

    // --- Callbacks (display) ---

    /**
     * Sets the callback invoked when toggling fire on a graph element is requested.
     *
     * @param listener the consumer that receives the element on which fire is toggled
     */
    public void setOnToggleFireRequested(Consumer<GraphElement> listener) {
        this.onToggleFireRequested = listener;
    }

    /**
     * Sets the callback invoked when deletion of a graph element is requested.
     *
     * @param listener the consumer that receives the element to delete
     */
    public void setOnDeleteRequested(Consumer<GraphElement> listener) {
        this.onDeleteRequested = listener;
    }

    /**
     * Sets the callback invoked when deletion of an agent is requested.
     *
     * @param listener the consumer that receives the agent to delete
     */
    public void setOnDeleteAgentRequested(Consumer<Agent> listener) {
        this.onDeleteAgentRequested = listener;
    }

    /**
     * Sets the callback invoked when killing an agent is requested.
     *
     * @param listener the consumer that receives the agent to kill
     */
    public void setOnKillAgentRequested(Consumer<Agent> listener) {
        this.onKillAgentRequested = listener;
    }

    // --- Callbacks (edit) ---

    /**
     * Sets the callback invoked when the node capacity value is changed.
     *
     * @param listener the consumer that receives the new capacity value
     */
    public void setOnNodeCapacityChanged(Consumer<Double> listener) {
        this.onNodeCapacityChanged = listener;
    }

    /**
     * Sets the callback invoked when the node exit status is toggled.
     *
     * @param listener the consumer that receives the new exit flag value
     */
    public void setOnNodeExitChanged(Consumer<Boolean> listener) {
        this.onNodeExitChanged = listener;
    }

    /**
     * Sets the callback invoked when the edge width is changed.
     *
     * @param listener the consumer that receives the new width value
     */
    public void setOnEdgeWidthChanged(Consumer<Double> listener) {
        this.onEdgeWidthChanged = listener;
    }

    /**
     * Sets the callback invoked when the edge length is changed.
     *
     * @param listener the consumer that receives the new length value
     */
    public void setOnEdgeLengthChanged(Consumer<Double> listener) {
        this.onEdgeLengthChanged = listener;
    }

    /**
     * Sets the callback invoked when the edge directed flag is changed.
     *
     * @param listener the consumer that receives the new directed flag value
     */
    public void setOnEdgeDirectedChanged(Consumer<Boolean> listener) {
        this.onEdgeDirectedChanged = listener;
    }

    /**
     * Sets the callback invoked when reversing the edge direction is requested.
     *
     * @param listener the runnable to invoke when the direction reversal is requested
     */
    public void setOnReverseEdgeDirectionRequested(Runnable listener) {
        this.onReverseEdgeDirectionRequested = listener;
    }

    // --- Animations ---

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

        // Reset visibility of all modular blocks
        VBox[] modularBoxes = { widthBox, lengthBox, nodeInfoBox, agentStatsBox, historyBox, speedBox, healthBox,
                actionsBox, editNodeBox, editEdgeBox };
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
        panelTitle.setText("Agent #" + agent.getId());

        // Setup Agent context titles cleanly via refactored helper
        setCommonTitles("Position", "Edge progress", "Stress level", "Emotional State", "Unique ID");

        // Health Tracking
        setBoxVisible(healthBox, true);
        double maxHealth = agent.getPhysicalProperties().getMaxHealth();
        double currentHealth = agent.getHealth();
        healthValue.setText(String.format("%.1f / %.1f HP", currentHealth, maxHealth));
        safeSetProgress(healthBar, currentHealth / maxHealth);

        // Speed Tracking
        setBoxVisible(speedBox, true);
        speedValue.setText(String.format("%.2f m/s", agent.getEffectiveSpeed(agentSettings)));

        // Process position and navigation progress metrics
        if (agent.isOnNode() && agent.getCurrentNode() != null) {
            capacityValue.setText("Node " + agent.getCurrentNode().getId());
            safeSetProgress(congestionBar, 0);
        } else if (agent.getPreviousOrCurrentEdge() != null) {
            Node target = agent.getPreviousOrCurrentEdge().getOppositeNode(agent.getPreviousOrCurrentNode());
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

        // Restore default layout semantic titles via refactored helper
        setCommonTitles("Capacity", "Congestion", "Stress level", "Fire intensity", "Agents on site");

        // Update operational fire button tracking states
        if (element.isOnFire()) {
            toggleFireBtn.setText("🧯 Extinguish");
            if (!toggleFireBtn.getStyleClass().contains("danger-btn")) {
                toggleFireBtn.getStyleClass().add("danger-btn");
            }
        } else {
            toggleFireBtn.setText("🔥 Trigger Fire");
            toggleFireBtn.getStyleClass().remove("danger-btn");
        }

        // Manage context button visibilities for Infrastructure types
        setElementVisible(deleteAgentBtn, false);
        setElementVisible(killAgentBtn, false);
        setElementVisible(toggleFireBtn, true);
        setElementVisible(deleteBtn, true);

        panelTitle.setText((element instanceof Node ? "Node" : "Edge") + " #" + element.getId());
        capacityValue.setText(String.format("%.1f m²", element.getCapacity()));

        double congestion = element.getCongestion();
        congestionValue.setText(String.format("%.1f%%", congestion * 100));
        safeSetProgress(congestionBar, congestion);

        double stress = element.getStressInducingImpact();
        stressValue.setText(String.format("%.0f%%", stress * 100));
        safeSetProgress(stressBar, stress);

        // Update hazardous properties
        if (element.isOnFire()) {
            fireValue.setText(String.format("%.2f", element.getFire().getIntensity()));
            safeSetProgress(fireBar, element.getFire().getIntensity());
        } else {
            fireValue.setText("None");
            safeSetProgress(fireBar, 0);
        }

        List<Agent> agents = element.getAgents();
        agentsValue.setText(String.valueOf(agents.size()));

        // Branch parameters based on specific element subclasses
        if (element instanceof Node node) {
            nodeInfoValue.setText(String.format("(%.0f, %.0f)", node.getX(), node.getY()));
            setBoxVisible(nodeInfoBox, true);

            // Populate editable node controls
            updatingUI = true;
            ((SpinnerValueFactory.DoubleSpinnerValueFactory) capacitySpinner.getValueFactory())
                    .setValue(node.getCapacity());
            exitCheckBox.setSelected(node.isExit());
            updatingUI = false;
            setBoxVisible(editNodeBox, true);

        } else if (element instanceof Edge edge) {
            widthValue.setText(String.format("%.2f m", edge.getWidth()));
            lengthValue.setText(String.format("%.2f m", edge.getLength()));
            setBoxVisible(widthBox, true);
            setBoxVisible(lengthBox, true);

            // Populate editable edge controls
            updatingUI = true;
            ((SpinnerValueFactory.DoubleSpinnerValueFactory) widthSpinner.getValueFactory())
                    .setValue(edge.getWidth());
            ((SpinnerValueFactory.DoubleSpinnerValueFactory) lengthSpinner.getValueFactory())
                    .setValue(edge.getLength());
            directedCheckBox.setSelected(edge.isDirected());
            updatingUI = false;
            setElementVisible(reverseDirectionBtn, edge.isDirected());
            setBoxVisible(editEdgeBox, true);
        }

        // Compute group metrics if agents populate the workspace
        if (!agents.isEmpty()) {
            double avgStress = agents.stream().mapToDouble(Agent::getStressLevel).average().orElse(0);
            avgStressValue.setText(String.format("%.0f%%", avgStress * 100));
            dominantStateValue.setText(computeDominantState(agents).name());
            setBoxVisible(agentStatsBox, true);
        }

        histTotalValue.setText(String.valueOf(element.getTotalAgentsCount()));
        histMaxCongValue.setText(String.format("%.0f%%", element.getMaxCongestion() * 100));
        histAvgCongValue.setText(String.format("%.0f%%", element.getAverageCongestion() * 100));

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
