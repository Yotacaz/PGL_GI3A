package fr.cy.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import fr.cy.App;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}
