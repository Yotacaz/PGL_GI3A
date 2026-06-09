package fr.cy.view;

import fr.cy.model.fire.Fire;
import fr.cy.model.graph.element.Edge;
import fr.cy.model.graph.element.GraphElement;
import fr.cy.model.graph.element.Node;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import java.util.Optional;

public class DialogHelper {

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

    private static void styleButtons(DialogPane pane, ButtonType okBtn) {
        ((Button) pane.lookupButton(okBtn)).getStyleClass().addAll("action-btn", "play-btn");
        ((Button) pane.lookupButton(ButtonType.CANCEL)).getStyleClass().add("action-btn");
    }

    // --- DIALOGUES ---

    public static Optional<Fire> showFireDialog(GraphElement element, javafx.scene.Node parentNode) {
        Dialog<Fire> dialog = new Dialog<>();
        ButtonType btnType = new ButtonType("🔥 Enflammer", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, "Déclenchement Incendie", "Propriétés (#" + element.getId() + ")",
                parentNode);
        pane.getButtonTypes().addAll(btnType, ButtonType.CANCEL);

        // Style spécifique incendie
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
        grid.add(new Label("Intensité :"), 0, 0);
        grid.add(intS, 1, 0);
        grid.add(new Label("Propagation :"), 0, 1);
        grid.add(sprS, 1, 1);
        pane.setContent(grid);

        dialog.setResultConverter(b -> b == btnType ? new Fire(0, intS.getValue(), sprS.getValue()) : null);
        return dialog.showAndWait();
    }

    public static Optional<NodeParams> showNodeCreationDialog(javafx.scene.Node parentNode) {
        ButtonType okBtn = new ButtonType("Créer le Nœud", ButtonBar.ButtonData.OK_DONE);
        Dialog<NodeParams> dialog = new Dialog<>();
        DialogPane pane = createBaseDialog(dialog, "Nouveau Nœud", "Définir la zone", parentNode);
        pane.getButtonTypes().addAll(okBtn, ButtonType.CANCEL);
        styleButtons(pane, okBtn);

        Spinner<Double> capSpinner = new Spinner<>(1.0, 5000.0, 5.0, 10.0);
        capSpinner.setEditable(true);
        CheckBox exitBox = new CheckBox("Sortie d'évacuation");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Capacité (m²) :"), 0, 0);
        grid.add(capSpinner, 1, 0);
        grid.add(exitBox, 0, 1, 2, 1);
        pane.setContent(grid);

        dialog.setResultConverter(b -> b == okBtn ? new NodeParams(capSpinner.getValue(), exitBox.isSelected()) : null);
        return dialog.showAndWait();
    }

    public static Optional<NodeParams> showNodeUpdateDialog(Node node, javafx.scene.Node parentNode) {
        ButtonType okBtn = new ButtonType("Appliquer", ButtonBar.ButtonData.OK_DONE);
        Dialog<NodeParams> dialog = new Dialog<>();
        DialogPane pane = createBaseDialog(dialog, "Modifier le Nœud", "ID #" + node.getId(), parentNode);
        pane.getButtonTypes().addAll(okBtn, ButtonType.CANCEL);
        styleButtons(pane, okBtn);

        Spinner<Double> capSpinner = new Spinner<>(1.0, 5000.0, node.getCapacity(), 10.0);
        capSpinner.setEditable(true);
        CheckBox exitBox = new CheckBox("Sortie d'évacuation");
        exitBox.setSelected(node.isExit());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Capacité (m²) :"), 0, 0);
        grid.add(capSpinner, 1, 0);
        grid.add(exitBox, 0, 1, 2, 1);
        pane.setContent(grid);

        dialog.setResultConverter(b -> b == okBtn ? new NodeParams(capSpinner.getValue(), exitBox.isSelected()) : null);
        return dialog.showAndWait();
    }

    public static Optional<EdgeParams> showEdgeCreationDialog(fr.cy.model.graph.element.Node start,
            fr.cy.model.graph.element.Node end,
            javafx.scene.Node parentNode) {
        Dialog<EdgeParams> dialog = new Dialog<>();
        // ... (utilise ici la logique createBaseDialog que nous avons faite) ...
        ButtonType createBtnType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, "Nouvelle Arête",
                "Connexion #" + start.getId() + " vers #" + end.getId(), parentNode);
        pane.getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);
        styleButtons(pane, createBtnType);

        Spinner<Double> wSp = new Spinner<>(0.5, 10.0, 2.0, 0.5);
        wSp.setEditable(true);
        Spinner<Double> lSp = new Spinner<>(0.5, 1000.0, 50.0, 10.0);
        lSp.setEditable(true);
        CheckBox dirBox = new CheckBox("Orientée");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Largeur:"), 0, 0);
        grid.add(wSp, 1, 0);
        grid.add(new Label("Longueur:"), 0, 1);
        grid.add(lSp, 1, 1);
        grid.add(dirBox, 0, 2, 2, 1);
        pane.setContent(grid);

        dialog.setResultConverter(
                b -> b == createBtnType ? new EdgeParams(wSp.getValue(), lSp.getValue(), dirBox.isSelected()) : null);
        return dialog.showAndWait();
    }

    public static Optional<EdgeParams> showEdgeUpdateDialog(Edge edge, javafx.scene.Node parentNode) {
        Dialog<EdgeParams> dialog = new Dialog<>();
        ButtonType appBtnType = new ButtonType("Appliquer", ButtonBar.ButtonData.OK_DONE);
        DialogPane pane = createBaseDialog(dialog, "Modifier Arête", "ID #" + edge.getId(), parentNode);
        pane.getButtonTypes().addAll(appBtnType, ButtonType.CANCEL);
        styleButtons(pane, appBtnType);

        TextField wF = new TextField(String.valueOf(edge.getWidth()));
        TextField lF = new TextField(String.valueOf(edge.getLength()));
        CheckBox dirCheck = new CheckBox("Orientée");
        dirCheck.setSelected(edge.isDirected());

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Largeur:"), 0, 0);
        grid.add(wF, 1, 0);
        grid.add(new Label("Longueur:"), 0, 1);
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

    public static Optional<Integer> showAgentCountDialog(javafx.scene.Node parentNode, String title,
            String headerText) {
        Dialog<Integer> dialog = new Dialog<>();
        ButtonType generateBtnType = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);

        // Utilisation de notre méthode usine pour initialiser le dialogue
        DialogPane pane = createBaseDialog(dialog, title, headerText, parentNode);
        pane.getButtonTypes().addAll(generateBtnType, ButtonType.CANCEL);

        // Application sécurisée du style
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

        grid.add(new Label("Nombre d'agents :"), 0, 0);
        grid.add(countSpinner, 1, 0);
        pane.setContent(grid);

        dialog.setResultConverter(btn -> btn == generateBtnType ? countSpinner.getValue() : null);
        return dialog.showAndWait();
    }

    public static Optional<Integer> showRandomGraphDialog(javafx.scene.Node parentNode) {
        Dialog<Integer> dialog = new Dialog<>();
        ButtonType genBtnType = new ButtonType("Générer", ButtonBar.ButtonData.OK_DONE);

        // Utilisation de la méthode usine factorisée
        DialogPane pane = createBaseDialog(dialog, "Génération de Masse", "Ajouter des nœuds aléatoires", parentNode);
        pane.getButtonTypes().addAll(genBtnType, ButtonType.CANCEL);

        // Style des boutons
        styleButtons(pane, genBtnType);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        Spinner<Integer> countSpinner = new Spinner<>(5, 1000, 20, 5);
        countSpinner.setEditable(true);

        grid.add(new Label("Nombre de nœuds :"), 0, 0);
        grid.add(countSpinner, 1, 0);
        pane.setContent(grid);

        dialog.setResultConverter(btn -> btn == genBtnType ? countSpinner.getValue() : null);
        return dialog.showAndWait();
    }

    // --- CLASSES PARAMÈTRES ---
    public static class EdgeParams {
        public final double width, length;
        public final boolean directed;

        public EdgeParams(double w, double l, boolean d) {
            this.width = w;
            this.length = l;
            this.directed = d;
        }
    }

    public static class NodeParams {
        public final double capacity;
        public final boolean isExit;

        public NodeParams(double c, boolean e) {
            this.capacity = c;
            this.isExit = e;
        }
    }
}