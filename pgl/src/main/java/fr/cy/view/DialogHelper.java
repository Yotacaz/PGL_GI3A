package fr.cy.view;

import fr.cy.model.agent.Agent;
import fr.cy.model.fire.Fire;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.Optional;

import fr.cy.model.agent.properties.AgentProfile;
import fr.cy.model.agent.properties.EmotionalState;

/**
 * The {@code DialogHelper} class is a utility class providing static methods
 * to create and display custom JavaFX {@link Dialog} windows for user input
 * related to simulation elements (Nodes, Edges, Agents, Fires).
 */
public class DialogHelper {

    /**
     * Initializes a standard dialog with consistent styling and owner window.
     * * @param dialog The dialog instance to configure.
     * * @param title The window title.
     * 
     * @param header The header text.
     * @param parent The parent node used to determine the owner window.
     * @return The configured {@link DialogPane}.
     */
    private static DialogPane createBaseDialog(Dialog<?> dialog, String title, String header,
            javafx.scene.Node parent) {
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        DialogPane pane = dialog.getDialogPane();
        if (parent != null && parent.getScene() != null) {
            dialog.initOwner(parent.getScene().getWindow());
            pane.getStylesheets().addAll(parent.getScene().getStylesheets());
        }
        return pane;
    }

    /**
     * Applies standard action button styles to a dialog.
     * * @param pane The dialog pane containing the buttons.
     * * @param okBtn The ButtonType representing the confirm action.
     */
    private static void styleButtons(DialogPane pane, ButtonType okBtn) {
        ((Button) pane.lookupButton(okBtn)).getStyleClass().addAll("action-btn", "play-btn");
        ((Button) pane.lookupButton(ButtonType.CANCEL)).getStyleClass().add("action-btn");
    }

    /**
     * Opens a dialog to modify an existing Agent's properties.
     *
     * @param agent      The agent to modify.
     * @param parentNode The parent node for window context.
     * @return An Optional containing the {@link AgentParams} if accepted.
     */
    public static Optional<AgentParams> showAgentUpdateDialog(Agent agent, javafx.scene.Node parentNode) {
        Dialog<AgentParams> dialog = new Dialog<>();
        ButtonType applyBtnType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, "Edit Agent", "Properties for Agent #" + agent.getId(),
                parentNode);
        pane.getButtonTypes().addAll(applyBtnType, ButtonType.CANCEL);
        styleButtons(pane, applyBtnType);

        // Define generic area size (min 0.1m², max 2m², current, step 0.01)
        Spinner<Double> healthS = new Spinner<>(0, agent.getPhysicalProperties().getMaxHealth(), agent.getHealth(), 1);
        healthS.setEditable(true);

        // Define generic area size (min 0.1m², max 2m², current, step 0.01)
        Spinner<Double> areaS = new Spinner<>(0.1, 2.0, agent.getSurfaceAreaTakenByAgent(), 0.01);
        areaS.setEditable(true);

        // Define max speed (min 0.1 m/s, max 10 m/s, current, step 0.1)
        Spinner<Double> speedS = new Spinner<>(0.1, 10.0, agent.getMaxSpeed(), 0.1);
        speedS.setEditable(true);

        // Dropdown for Emotional State based on defined Enum
        ComboBox<EmotionalState> stateCombo = new ComboBox<>();
        stateCombo.getItems().addAll(EmotionalState.values());
        stateCombo.setValue(agent.getEmotionalState());
        stateCombo.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        int row = 0;
        grid.add(new Label("Health:"), 0, row);
        grid.add(healthS, 1, row++);

        grid.add(new Label("Physical Area (m²):"), 0, row);
        grid.add(areaS, 1, row++);

        grid.add(new Label("Base Max Speed (m/s):"), 0, row);
        grid.add(speedS, 1, row++);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == applyBtnType) {
                return new AgentParams(areaS.getValue(), speedS.getValue(), healthS.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    /**
     * Opens a dialog to configure and trigger a fire on a specific graph element.
     * * @param element The graph element to set on fire.
     * * @param parentNode The parent node for window context.
     * 
     * @return An Optional containing the configured {@link Fire} if accepted.
     */
    public static Optional<Fire> showFireDialog(GraphElement element, javafx.scene.Node parentNode) {
        Dialog<Fire> dialog = new Dialog<>();
        ButtonType btnType = new ButtonType("🔥 Ignite", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, "Trigger Fire", "Properties for (#" + element.getId() + ")",
                parentNode);
        pane.getButtonTypes().addAll(btnType, ButtonType.CANCEL);

        ((Button) pane.lookupButton(btnType)).getStyleClass().addAll("action-btn", "danger-btn");
        ((Button) pane.lookupButton(ButtonType.CANCEL)).getStyleClass().add("action-btn");

        Spinner<Double> intS = new Spinner<>(0.1, 5.0, 1.0, 0.1);
        Spinner<Double> sprS = new Spinner<>(0.0, 1.0, 0.05, 0.01);
        intS.setEditable(true);
        sprS.setEditable(true);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Intensity:"), 0, 0);
        grid.add(intS, 1, 0);
        grid.add(new Label("Propagation:"), 0, 1);
        grid.add(sprS, 1, 1);
        pane.setContent(grid);

        dialog.setResultConverter(b -> b == btnType ? new Fire(0, intS.getValue(), sprS.getValue()) : null);
        return dialog.showAndWait();
    }

    /**
     * Opens a dialog to input parameters for creating a new node.
     * * @param parentNode The parent node for window context.
     * * @return An Optional containing the {@link NodeParams}.
     */
    public static Optional<NodeParams> showNodeCreationDialog(javafx.scene.Node parentNode) {
        ButtonType okBtn = new ButtonType("Create Node", ButtonBar.ButtonData.OK_DONE);
        Dialog<NodeParams> dialog = new Dialog<>();
        DialogPane pane = createBaseDialog(dialog, "New Node", "Define node properties", parentNode);
        pane.getButtonTypes().addAll(okBtn, ButtonType.CANCEL);
        styleButtons(pane, okBtn);

        Spinner<Double> capSpinner = new Spinner<>(1.0, 5000.0, 5.0, 10.0);
        capSpinner.setEditable(true);
        CheckBox exitBox = new CheckBox("Evacuation Exit");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Capacity (m²):"), 0, 0);
        grid.add(capSpinner, 1, 0);
        grid.add(exitBox, 0, 1, 2, 1);
        pane.setContent(grid);

        dialog.setResultConverter(b -> b == okBtn ? new NodeParams(capSpinner.getValue(), exitBox.isSelected()) : null);
        return dialog.showAndWait();
    }

    /**
     * Opens a dialog to modify an existing node's properties.
     * * @param node The node to modify.
     * * @param parentNode The parent node for window context.
     * 
     * @return An Optional containing the {@link NodeParams}.
     */
    public static Optional<NodeParams> showNodeUpdateDialog(Node node, javafx.scene.Node parentNode) {
        ButtonType okBtn = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        Dialog<NodeParams> dialog = new Dialog<>();
        DialogPane pane = createBaseDialog(dialog, "Edit Node", "ID #" + node.getId(), parentNode);
        pane.getButtonTypes().addAll(okBtn, ButtonType.CANCEL);
        styleButtons(pane, okBtn);

        Spinner<Double> capSpinner = new Spinner<>(1.0, 5000.0, node.getCapacity(), 10.0);
        capSpinner.setEditable(true);
        CheckBox exitBox = new CheckBox("Evacuation Exit");
        exitBox.setSelected(node.isExit());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Capacity (m²):"), 0, 0);
        grid.add(capSpinner, 1, 0);
        grid.add(exitBox, 0, 1, 2, 1);
        pane.setContent(grid);

        dialog.setResultConverter(b -> b == okBtn ? new NodeParams(capSpinner.getValue(), exitBox.isSelected()) : null);
        return dialog.showAndWait();
    }

    /**
     * Opens a dialog to create a new edge between two nodes.
     * * @param start The starting node.
     * * @param end The ending node.
     * 
     * @param parentNode The parent node for window context.
     * @return An Optional containing the {@link EdgeParams}.
     */
    public static Optional<EdgeParams> showEdgeCreationDialog(Node start, Node end, javafx.scene.Node parentNode) {
        Dialog<EdgeParams> dialog = new Dialog<>();
        ButtonType createBtnType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, "New Edge", "Connect #" + start.getId() + " to #" + end.getId(),
                parentNode);
        pane.getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);
        styleButtons(pane, createBtnType);

        Spinner<Double> wSp = new Spinner<>(0.5, 10.0, 2.0, 0.5);
        wSp.setEditable(true);
        Spinner<Double> lSp = new Spinner<>(0.5, 1000.0, 50.0, 10.0);
        lSp.setEditable(true);
        CheckBox dirBox = new CheckBox("Directed");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Width:"), 0, 0);
        grid.add(wSp, 1, 0);
        grid.add(new Label("Length:"), 0, 1);
        grid.add(lSp, 1, 1);
        grid.add(dirBox, 0, 2, 2, 1);
        pane.setContent(grid);

        dialog.setResultConverter(
                b -> b == createBtnType ? new EdgeParams(wSp.getValue(), lSp.getValue(), dirBox.isSelected()) : null);
        return dialog.showAndWait();
    }

    /**
     * Opens a dialog to modify an existing edge's properties.
     * * @param edge The edge to modify.
     * * @param parentNode The parent node for window context.
     * 
     * @return An Optional containing the {@link EdgeParams}.
     */
    public static Optional<EdgeParams> showEdgeUpdateDialog(Edge edge, javafx.scene.Node parentNode) {
        Dialog<EdgeParams> dialog = new Dialog<>();
        ButtonType appBtnType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, "Edit Edge", "ID #" + edge.getId(), parentNode);
        pane.getButtonTypes().addAll(appBtnType, ButtonType.CANCEL);
        styleButtons(pane, appBtnType);

        TextField wF = new TextField(String.valueOf(edge.getWidth()));
        TextField lF = new TextField(String.valueOf(edge.getLength()));
        CheckBox dirCheck = new CheckBox("Directed");
        dirCheck.setSelected(edge.isDirected());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Width:"), 0, 0);
        grid.add(wF, 1, 0);
        grid.add(new Label("Length:"), 0, 1);
        grid.add(lF, 1, 1);
        grid.add(dirCheck, 0, 2, 2, 1);
        pane.setContent(grid);

        dialog.setResultConverter(b -> {
            if (b == appBtnType) {
                try {
                    return new EdgeParams(Double.parseDouble(wF.getText()), Double.parseDouble(lF.getText()),
                            dirCheck.isSelected());
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });
        return dialog.showAndWait();
    }

    /**
     * Opens a dialog to request the number of agents to generate.
     * * @param parentNode The parent node for window context.
     * * @param title The dialog title.
     * 
     * @param headerText The header text.
     * @return An Optional containing the integer count of agents.
     */
    public static Optional<Integer> showAgentCountDialog(javafx.scene.Node parentNode, String title,
            String headerText) {
        Dialog<Integer> dialog = new Dialog<>();
        ButtonType generateBtnType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, title, headerText, parentNode);
        pane.getButtonTypes().addAll(generateBtnType, ButtonType.CANCEL);

        Button genBtn = (Button) pane.lookupButton(generateBtnType);
        if (genBtn != null)
            genBtn.getStyleClass().addAll("action-btn", "play-btn");
        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        if (cancelBtn != null)
            cancelBtn.getStyleClass().add("action-btn");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        Spinner<Integer> countSpinner = new Spinner<>(1, 1000, 10, 1);
        countSpinner.setEditable(true);

        grid.add(new Label("Number of agents:"), 0, 0);
        grid.add(countSpinner, 1, 0);
        pane.setContent(grid);

        dialog.setResultConverter(btn -> btn == generateBtnType ? countSpinner.getValue() : null);
        return dialog.showAndWait();
    }

    /**
     * Shows a dialog that requests how many agents to create and which profile
     * to assign to them. Returns both values in {@link AgentCreationOptions}.
     */
    public static Optional<AgentCreationOptions> showAgentCountAndProfileDialog(javafx.scene.Node parentNode,
            String title, String headerText) {
        Dialog<AgentCreationOptions> dialog = new Dialog<>();
        ButtonType generateBtnType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, title, headerText, parentNode);
        pane.getButtonTypes().addAll(generateBtnType, ButtonType.CANCEL);
        styleButtons(pane, generateBtnType);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        Spinner<Integer> countSpinner = new Spinner<>(1, 1000, 10, 1);
        countSpinner.setEditable(true);

        ComboBox<AgentProfile> profileCombo = new ComboBox<>();
        profileCombo.getItems().addAll(AgentProfile.DEFAULT, AgentProfile.ELDERLY, AgentProfile.TOURIST);
        profileCombo.setValue(AgentProfile.DEFAULT);

        grid.add(new Label("Number of agents:"), 0, 0);
        grid.add(countSpinner, 1, 0);
        grid.add(new Label("Profile:"), 0, 1);
        grid.add(profileCombo, 1, 1);

        pane.setContent(grid);

        dialog.setResultConverter(btn -> btn == generateBtnType ? new AgentCreationOptions(countSpinner.getValue(),
                profileCombo.getValue()) : null);
        return dialog.showAndWait();
    }

    /**
     * Opens a dialog to define the number of nodes for random graph generation.
     * * @param parentNode The parent node for window context.
     * * @return An Optional containing the number of nodes to generate.
     */
    public static Optional<Integer> showRandomGraphDialog(javafx.scene.Node parentNode) {
        Dialog<Integer> dialog = new Dialog<>();
        ButtonType genBtnType = new ButtonType("Generate", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, "Mass Generation", "Add random nodes", parentNode);
        pane.getButtonTypes().addAll(genBtnType, ButtonType.CANCEL);
        styleButtons(pane, genBtnType);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        Spinner<Integer> countSpinner = new Spinner<>(2, 1000, 5, 5);
        countSpinner.setEditable(true);

        grid.add(new Label("Number of nodes:"), 0, 0);
        grid.add(countSpinner, 1, 0);
        pane.setContent(grid);

        dialog.setResultConverter(btn -> btn == genBtnType ? countSpinner.getValue() : null);
        return dialog.showAndWait();
    }

    /** Data Transfer Object for edge parameters. */
    public static class EdgeParams {
        public final double width, length;
        public final boolean directed;

        public EdgeParams(double w, double l, boolean d) {
            this.width = w;
            this.length = l;
            this.directed = d;
        }
    }

    /** Data Transfer Object for node parameters. */
    public static class NodeParams {
        public final double capacity;
        public final boolean isExit;

        public NodeParams(double c, boolean e) {
            this.capacity = c;
            this.isExit = e;
        }
    }

    /** Data Transfer Object for agent parameters. */
    public static class AgentParams {
        public final double surfaceArea;
        public final double maxSpeed;
        public final double health;

        public AgentParams(double surfaceArea, double maxSpeed, double health) {
            this.surfaceArea = surfaceArea;
            this.maxSpeed = maxSpeed;
            this.health = health;
        }
    }

    /** DTO returned by {@link #showAgentCountAndProfileDialog}. */
    public static record AgentCreationOptions(int count, AgentProfile profile) {
    }
}