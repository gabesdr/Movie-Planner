module is.vidmot.movieplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens is.vidmot to javafx.fxml;
    opens is.vidmot.controller to javafx.fxml;

    exports is.vidmot;
    exports is.vidmot.controller;

}