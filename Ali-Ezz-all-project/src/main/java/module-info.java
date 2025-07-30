module com.coursemanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;

    opens com.coursemanagement to javafx.fxml;
    exports com.coursemanagement;
    requires javafx.graphicsEmpty;
}
