/**
 * JavaFX Demo Application
 */
module com.example {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.fxml;

    opens com.example to javafx.fxml;

    exports com.example;
}
