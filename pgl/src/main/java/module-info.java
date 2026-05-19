module fr.cy {
    requires javafx.controls;
    requires javafx.fxml;

    opens fr.cy to javafx.fxml;
    opens fr.cy.controller to javafx.fxml;

    exports fr.cy;
}
