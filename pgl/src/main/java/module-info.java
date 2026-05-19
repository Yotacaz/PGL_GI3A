module fr.cy {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens fr.cy to javafx.fxml;
    opens fr.cy.controller to javafx.fxml;

    exports fr.cy;
}
