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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

public class DetailsSidePanel extends ScrollPane {

    private VBox contentBox;
    private Label panelTitle;

    private Label capacityTitle, congestionTitle, stressTitle, fireTitle, agentsTitle;
    private Label stressValue, fireValue, agentsValue, capacityValue, congestionValue, widthValue, lengthValue;
    private Label avgStressValue, dominantStateValue, histTotalValue, histMaxCongValue, histAvgCongValue;
    private Label nodeInfoValue;

    private Label speedValue;
    private VBox speedBox;

    private ProgressBar stressBar, fireBar, congestionBar;
    private VBox widthBox, lengthBox, agentStatsBox, historyBox, nodeInfoBox;

    // --- BOUTONS ET ACTIONS ---
    private VBox actionsBox;
    private Button toggleFireBtn;
    private Button deleteBtn;
    private Object currentEntity;

    private Consumer<GraphElement> onToggleFireRequested;
    private Consumer<GraphElement> onDeleteRequested;

    // --- VARIABLES POUR L'ANIMATION ---
    private boolean isPanelVisible = false;
    private Timeline animationTimeline;

    public DetailsSidePanel() {
        initUI();

        // --- ON DEMARRE AVEC LE PANNEAU CACHÉ ---
        this.setMinWidth(0);
        this.setPrefWidth(0);
        this.setMaxWidth(0);
        this.setOpacity(0);
        this.setVisible(false);
        this.setManaged(false);
    }

    private void initUI() {
        contentBox = new VBox(20);
        contentBox.getStyleClass().add("details-panel");
        contentBox.setMinWidth(300);
        contentBox.setPrefWidth(300);

        panelTitle = new Label("ELEMENT DETAILS");
        panelTitle.getStyleClass().add("panel-title");

        stressValue = new Label("0.0");
        stressValue.getStyleClass().add("stat-value");
        fireValue = new Label("0.0");
        fireValue.getStyleClass().add("stat-value");
        agentsValue = new Label("0");
        agentsValue.getStyleClass().add("stat-value-highlight");
        capacityValue = new Label("0");
        capacityValue.getStyleClass().add("stat-value");
        congestionValue = new Label("0%");
        congestionValue.getStyleClass().add("stat-value");
        widthValue = new Label("0");
        widthValue.getStyleClass().add("stat-value");
        lengthValue = new Label("0");
        lengthValue.getStyleClass().add("stat-value");

        speedValue = new Label("0.0 m/s");
        speedValue.getStyleClass().add("stat-value-highlight");
        speedBox = createStatBox(new Label("CURRENT SPEED"), speedValue);

        stressBar = new ProgressBar(0);
        stressBar.setMaxWidth(Double.MAX_VALUE);
        stressBar.setMinHeight(12);
        stressBar.getStyleClass().addAll("progress-bar", "stress-bar");
        fireBar = new ProgressBar(0);
        fireBar.setMaxWidth(Double.MAX_VALUE);
        fireBar.setMinHeight(12);
        fireBar.getStyleClass().addAll("progress-bar", "fire-bar");
        congestionBar = new ProgressBar(0);
        congestionBar.setMaxWidth(Double.MAX_VALUE);
        congestionBar.setMinHeight(12);
        congestionBar.getStyleClass().addAll("progress-bar", "congestion-bar");

        capacityTitle = new Label("CAPACITY");
        congestionTitle = new Label("CONGESTION");
        stressTitle = new Label("STRESS LEVEL");
        fireTitle = new Label("FIRE INTENSITY");
        agentsTitle = new Label("AGENTS ON SITE");

        VBox capacityBox = createStatBox(capacityTitle, capacityValue);
        VBox congestionBox = createStatBox(congestionTitle, congestionValue);
        congestionBox.getChildren().add(congestionBar);
        VBox stressBox = createStatBox(stressTitle, stressValue);
        stressBox.getChildren().add(stressBar);
        VBox fireBox = createStatBox(fireTitle, fireValue);
        fireBox.getChildren().add(fireBar);
        VBox agentsBox = createStatBox(agentsTitle, agentsValue);

        widthBox = createStatBox(new Label("WIDTH"), widthValue);
        lengthBox = createStatBox(new Label("LENGTH"), lengthValue);

        nodeInfoValue = new Label("--");
        nodeInfoValue.getStyleClass().add("stat-value");
        nodeInfoBox = createStatBox(new Label("NODE INFO"), nodeInfoValue);

        avgStressValue = new Label("--");
        avgStressValue.getStyleClass().add("stat-value");
        dominantStateValue = new Label("--");
        dominantStateValue.getStyleClass().add("stat-value");
        agentStatsBox = new VBox(8);
        agentStatsBox.getStyleClass().add("stat-box");
        Label agentStatsTitle = new Label("AGENT STATS");
        agentStatsTitle.getStyleClass().add("stat-title");
        agentStatsBox.getChildren().addAll(agentStatsTitle, avgStressValue, dominantStateValue);

        histTotalValue = new Label("--");
        histMaxCongValue = new Label("--");
        histAvgCongValue = new Label("--");
        for (Label l : new Label[] { histTotalValue, histMaxCongValue, histAvgCongValue }) {
            l.getStyleClass().add("stat-value");
        }

        historyBox = new VBox(8);
        historyBox.getStyleClass().add("stat-box");
        Label histTitle = new Label("HISTORY");
        histTitle.getStyleClass().add("stat-title");
        historyBox.getChildren().addAll(histTitle, histTotalValue, histMaxCongValue, histAvgCongValue);

        // --- INITIALISATION DES BOUTONS (DESIGN CSS) ---
        toggleFireBtn = new Button("🔥 Déclencher le Feu");
        toggleFireBtn.setMaxWidth(Double.MAX_VALUE);
        toggleFireBtn.getStyleClass().addAll("action-btn"); // Bouton gris de base

        deleteBtn = new Button("🗑 Supprimer l'élément");
        deleteBtn.setMaxWidth(Double.MAX_VALUE);
        deleteBtn.getStyleClass().addAll("action-btn", "danger-btn"); // Bouton rouge

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

        actionsBox = createStatBox(new Label("ACTIONS"), new VBox(10, toggleFireBtn, deleteBtn));

        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // --- ASSEMBLAGE COMME TON PREMIER PROMPT ---
        contentBox.getChildren().addAll(
                panelTitle, new Separator(),
                nodeInfoBox, speedBox,
                capacityBox, congestionBox, stressBox, fireBox, agentsBox,
                agentStatsBox, widthBox, lengthBox, historyBox,
                new Separator(), actionsBox, spacer);

        this.setContent(contentBox);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    // --- SETTERS POUR LES ÉCOUTEURS (MVC) ---
    public void setOnToggleFireRequested(Consumer<GraphElement> listener) {
        this.onToggleFireRequested = listener;
    }

    public void setOnDeleteRequested(Consumer<GraphElement> listener) {
        this.onDeleteRequested = listener;
    }

    private VBox createStatBox(Label title, javafx.scene.Node valueNode) {
        VBox box = new VBox(8);
        box.getStyleClass().add("stat-box");
        title.getStyleClass().add("stat-title");
        box.getChildren().addAll(title, valueNode);
        return box;
    }

    private void setBoxVisible(VBox box, boolean visible) {
        box.setVisible(visible);
        box.setManaged(visible);
    }

    private void safeSetProgress(ProgressBar bar, double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            bar.setProgress(0.0);
        } else {
            bar.setProgress(Math.max(0.0, Math.min(1.0, value)));
        }
    }

    // --- METHODES D'ANIMATION ---

    public void showPanel() {
        if (isPanelVisible)
            return;
        isPanelVisible = true;
        if (animationTimeline != null)
            animationTimeline.stop();
        this.setManaged(true);
        this.setVisible(true);
        animationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(this.maxWidthProperty(), this.getWidth()),
                        new KeyValue(this.minWidthProperty(), this.getWidth()),
                        new KeyValue(this.prefWidthProperty(), this.getWidth()),
                        new KeyValue(this.opacityProperty(), this.getOpacity())),
                new KeyFrame(Duration.millis(250), new KeyValue(this.maxWidthProperty(), 300, Interpolator.EASE_OUT),
                        new KeyValue(this.minWidthProperty(), 300, Interpolator.EASE_OUT),
                        new KeyValue(this.prefWidthProperty(), 300, Interpolator.EASE_OUT),
                        new KeyValue(this.opacityProperty(), 1.0, Interpolator.EASE_OUT)));
        animationTimeline.play();
    }

    public void hidePanel() {
        if (!isPanelVisible)
            return;
        isPanelVisible = false;
        if (animationTimeline != null)
            animationTimeline.stop();
        animationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(this.maxWidthProperty(), this.getWidth()),
                        new KeyValue(this.minWidthProperty(), this.getWidth()),
                        new KeyValue(this.prefWidthProperty(), this.getWidth()),
                        new KeyValue(this.opacityProperty(), this.getOpacity())),
                new KeyFrame(Duration.millis(200), new KeyValue(this.maxWidthProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(this.minWidthProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(this.prefWidthProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(this.opacityProperty(), 0.0, Interpolator.EASE_IN)));
        animationTimeline.setOnFinished(e -> {
            this.setManaged(false);
            this.setVisible(false);
        });
        animationTimeline.play();
    }

    public void update(Object entity, AgentSettings agentSettings) {
        if (entity == null)
            return;
        this.currentEntity = entity;

        // 1. On cache toutes les boîtes optionnelles
        for (VBox box : new VBox[] { widthBox, lengthBox, nodeInfoBox, agentStatsBox, historyBox, speedBox,
                actionsBox }) {
            box.setVisible(false);
            box.setManaged(false);
        }

        // --- 2. GESTION DE L'AFFICHAGE D'UN AGENT ---
        if (entity instanceof Agent agent) {
            panelTitle.setText("AGENT #" + agent.getId());
            setBoxVisible(speedBox, true);
            speedValue.setText(String.format("%.2f m/s", agent.getEffectiveSpeed(agentSettings)));

            capacityTitle.setText("POSITION");
            if (agent.isOnNode() && agent.getCurrentNode() != null) {
                capacityValue.setText("Nœud " + agent.getCurrentNode().getId());
                safeSetProgress(congestionBar, 0);
            } else if (agent.getCurrentOrPreviousEdge() != null) {
                Node target = agent.getCurrentOrPreviousEdge().getOppositeNode(agent.getPreviousOrCurrentNode());
                capacityValue.setText("Vers Nœud " + target.getId());
                safeSetProgress(congestionBar, agent.getTravelProgressPercentageOnEdge());
            } else {
                capacityValue.setText("Échappé / Inconnu");
                safeSetProgress(congestionBar, 0);
            }

            congestionTitle.setText("PROGRESSION SUR ARÊTE");
            congestionValue.setText(String.format("%.0f%%", agent.getTravelProgressPercentageOnEdge() * 100));

            stressTitle.setText("STRESS LEVEL");
            stressValue.setText(String.format("%.0f%%", agent.getStressLevel() * 100));
            safeSetProgress(stressBar, agent.getStressLevel());

            fireTitle.setText("ÉTAT ÉMOTIONNEL");
            fireValue.setText(agent.getEmotionalState().name());
            safeSetProgress(fireBar, 0);

            agentsTitle.setText("IDENTIFIANT UNIQUE");
            agentsValue.setText("#" + agent.getId());
            return;
        }

        // --- 3. GESTION DE L'AFFICHAGE CLASSIQUE (NOEUDS/ARÊTES) ---
        GraphElement element = (GraphElement) entity;
        setBoxVisible(actionsBox, true);

        // Mise à jour dynamique du style du bouton de feu
        if (element.isOnFire()) {
            toggleFireBtn.setText("🧯 Éteindre le Feu");
            toggleFireBtn.getStyleClass().add("danger-btn"); // Devient rouge
        } else {
            toggleFireBtn.setText("🔥 Déclencher le Feu");
            toggleFireBtn.getStyleClass().remove("danger-btn"); // Redevient gris classique
        }

        capacityTitle.setText("CAPACITY");
        congestionTitle.setText("CONGESTION");
        stressTitle.setText("STRESS LEVEL");
        fireTitle.setText("FIRE INTENSITY");
        agentsTitle.setText("AGENTS ON SITE");

        String typeName = element instanceof Node ? "NODE" : "EDGE";
        panelTitle.setText(typeName + " #" + element.getId());

        capacityValue.setText(String.format("%.1f", element.getCapacity()));
        double congestion = element.getCongestion();
        congestionValue.setText(String.format("%.1f%%", congestion * 100));
        safeSetProgress(congestionBar, congestion);

        double stress = element.getStressInducingImpact();
        stressValue.setText(String.format("%.0f%%", stress * 100));
        safeSetProgress(stressBar, stress);

        if (element.isOnFire()) {
            fireValue.setText(String.format("%.2f (fumée %.2f)",
                    element.getFire().getIntensity(), element.getFire().getSmokeLevel()));
            safeSetProgress(fireBar, element.getFire().getIntensity());
        } else {
            fireValue.setText("None");
            safeSetProgress(fireBar, 0);
        }

        List<Agent> agents = element.getAgents();
        agentsValue.setText(String.valueOf(agents.size()));

        if (element instanceof Node node) {
            nodeInfoValue.setText(String.format("(%.0f, %.0f)  %s",
                    node.getX(), node.getY(), node.isExit() ? "★ SORTIE" : "nœud"));
            setBoxVisible(nodeInfoBox, true);
        }

        if (element instanceof Edge edge) {
            widthValue.setText(String.format("%.2f", edge.getWidth()));
            lengthValue.setText(String.format("%.2f", edge.getLength()));
            setBoxVisible(widthBox, true);
            setBoxVisible(lengthBox, true);
        }

        if (!agents.isEmpty()) {
            double avgStress = agents.stream().mapToDouble(Agent::getStressLevel).average().orElse(0);
            avgStressValue.setText("Stress moy. : " + String.format("%.0f%%", avgStress * 100));
            EmotionalState dominant = dominantState(agents);
            dominantStateValue.setText("État : " + dominant.name());
            setBoxVisible(agentStatsBox, true);
        }

        histTotalValue.setText("Total passés : " + element.getTotalAgentsCount());
        histMaxCongValue.setText("Congestion max : " + String.format("%.0f%%", element.getMaxCongestion() * 100));
        histAvgCongValue.setText("Congestion moy. : " + String.format("%.0f%%", element.getAverageCongestion() * 100));
        setBoxVisible(historyBox, true);
    }

    private EmotionalState dominantState(List<Agent> agents) {
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