package fr.cy.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import fr.cy.App;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}