package is.vidmot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class MoviePlannerApp extends Application {

    private static final double WIDTH = 1280;
    private static final double HEIGHT = 820;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(MoviePlannerApp.class.getResource("/is/vidmot/main-view.fxml"));
        Scene scene = new Scene(loader.load(), WIDTH, HEIGHT);
        scene.getStylesheets().add(MoviePlannerApp.class.getResource("/css/movie-planner.css").toExternalForm());

        stage.setTitle("Movie Planner");
        stage.getIcons().add(new Image(
                "https://cdn-icons-png.flaticon.com/512/3163/3163478.png",
                true
        ));
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}