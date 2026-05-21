module fr.cy {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens fr.cy to javafx.fxml;
    opens fr.cy.controller to javafx.fxml;

    exports fr.cy;
    exports fr.cy.controller;
    exports fr.cy.model.agent;
    exports fr.cy.model.graph;
    exports fr.cy.model.graph.element;
    exports fr.cy.model.pathfinding;
}
