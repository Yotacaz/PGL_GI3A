package fr.cy.view;

import fr.cy.controller.SimulationController;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.stage.Popup;
import javafx.util.Duration;

public class SimulationToolBar extends ToolBar {

    private final SimulationController simController;

    public SimulationToolBar(SimulationController simController) {
        this.simController = simController;
        this.getStyleClass().add("custom-toolbar");
        initToolBar();
    }

    private void initToolBar() {
        Button playBtn = new Button("▶ Play");
        Button pauseBtn = new Button("⏸ Pause");
        Button resetBtn = new Button("🔄 Reset");

        playBtn.getStyleClass().addAll("action-btn", "play-btn");
        pauseBtn.getStyleClass().addAll("action-btn", "pause-btn");
        resetBtn.getStyleClass().addAll("action-btn", "danger-btn");

        playBtn.setOnAction(e -> simController.play());
        pauseBtn.setOnAction(e -> simController.pause());
        resetBtn.setOnAction(e -> simController.reset());

        Separator sep1 = new Separator();

        Button stepBtn = new Button("⏭ Step");
        stepBtn.getStyleClass().addAll("action-btn", "step-btn");

        Label stepCountValue = new Label("+" + simController.getStepTicks());
        stepCountValue.getStyleClass().add("step-count-value");

        Label stepHint = new Label("Ticks/step");
        stepHint.getStyleClass().add("step-hint");

        Button stepMinusBtn = new Button("−100");
        Button stepPlusBtn = new Button("+100");
        stepMinusBtn.getStyleClass().addAll("action-btn", "step-adjust-btn");
        stepPlusBtn.getStyleClass().addAll("action-btn", "step-adjust-btn");

        HBox stepAdjustRow = new HBox(8, stepMinusBtn, stepCountValue, stepPlusBtn);
        stepAdjustRow.getStyleClass().add("step-adjust-row");

        VBox stepPopup = new VBox(8, stepHint, stepAdjustRow);
        stepPopup.getStyleClass().add("step-popup");
        Popup stepPopupWindow = new Popup();
        stepPopupWindow.setAutoHide(true);
        stepPopupWindow.getContent().add(stepPopup);

        FadeTransition showPopup = new FadeTransition(Duration.millis(120), stepPopup);
        showPopup.setFromValue(0);
        showPopup.setToValue(1);

        FadeTransition hidePopup = new FadeTransition(Duration.millis(90), stepPopup);
        hidePopup.setFromValue(1);
        hidePopup.setToValue(0);

        PauseTransition hideDelay = new PauseTransition(Duration.millis(120));
        hideDelay.setOnFinished(event -> {
            if (stepPopupWindow.isShowing()) {
                hidePopup.setOnFinished(e -> stepPopupWindow.hide());
                hidePopup.playFromStart();
            }
        });

        stepBtn.setOnMouseEntered(event -> {
            hideDelay.stop();
            if (!stepPopupWindow.isShowing()) {
                Point2D anchorPoint = stepBtn.localToScreen(0, 0);
                if (anchorPoint != null) {
                    stepPopupWindow.show(stepBtn, anchorPoint.getX(), anchorPoint.getY() + stepBtn.getHeight() + 8);
                }
                stepPopup.setOpacity(0);
                showPopup.playFromStart();
            }
        });
        stepBtn.setOnMouseExited(event -> hideDelay.playFromStart());
        stepPopup.setOnMouseEntered(event -> hideDelay.stop());
        stepPopup.setOnMouseExited(event -> hideDelay.playFromStart());

        stepBtn.setOnAction(e -> simController.stepTick());
        stepMinusBtn.setOnAction(e -> {
            int stepTicks = Math.max(1, simController.getStepTicks() - 100);
            simController.setStepTicks(stepTicks);
            stepCountValue.setText("+" + stepTicks);
        });
        stepPlusBtn.setOnAction(e -> {
            int stepTicks = Math.min(1000, simController.getStepTicks() + 100);
            simController.setStepTicks(stepTicks);
            stepCountValue.setText("+" + stepTicks);
        });

        // Contrôle de la vitesse
        Button decreaseSpeedBtn = new Button("⏪ -");
        Button increaseSpeedBtn = new Button("+ ⏩");
        Label speedLabel = new Label(formatSpeedMultiplier(simController.getSpeed()));

        decreaseSpeedBtn.getStyleClass().addAll("action-btn", "speed-btn");
        increaseSpeedBtn.getStyleClass().addAll("action-btn", "speed-btn");
        speedLabel.getStyleClass().add("speed-badge");

        decreaseSpeedBtn.setOnAction(e -> {
            simController.decreaseSpeed();
            speedLabel.setText(formatSpeedMultiplier(simController.getSpeed()));
        });
        increaseSpeedBtn.setOnAction(e -> {
            simController.increaseSpeed();
            speedLabel.setText(formatSpeedMultiplier(simController.getSpeed()));
        });

        this.getItems().addAll(
                playBtn, pauseBtn, resetBtn, sep1,
                stepBtn, stepCountValue,
                decreaseSpeedBtn, speedLabel, increaseSpeedBtn);
    }

    private String formatSpeedMultiplier(double speedMultiplier) {
        if (Math.abs(speedMultiplier - Math.rint(speedMultiplier)) < 0.001) {
            return "x" + (int) Math.rint(speedMultiplier);
        }
        return String.format("x%.2f", speedMultiplier).replace(".", ",");
    }
}