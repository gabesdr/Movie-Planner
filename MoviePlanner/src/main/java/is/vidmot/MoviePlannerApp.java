package is.vidmot;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Aðalklasi sem ræsir Movie Planner JavaFX forritið.
 */
public class MoviePlannerApp extends Application {

    /** Upphafsbreidd gluggans. */
    private static final double WIDTH = 1280;

    /** Upphafshæð gluggans. */
    private static final double HEIGHT = 820;

    /** Lágmarksbreidd gluggans. */
    private static final double MIN_WIDTH = 1100;

    /** Lágmarkshæð gluggans. */
    private static final double MIN_HEIGHT = 720;

    /**
     * Ræsir aðalglugga forritsins.
     *
     * @param stage aðalstage forritsins
     * @throws IOException ef ekki tekst að lesa FXML skrá
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                MoviePlannerApp.class.getResource("/is/vidmot/main-view.fxml")
        );

        Scene scene = new Scene(loader.load(), WIDTH, HEIGHT);
        scene.getStylesheets().add(
                MoviePlannerApp.class.getResource("/css/movie-planner.css").toExternalForm()
        );

        stage.setTitle("Movie Planner");
        stage.getIcons().add(new Image(
                "https://cdn-icons-png.flaticon.com/512/3163/3163478.png",
                true
        ));
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Aðalinngangur forritsins.
     *
     * @param args skipanalínubreytur
     */
    public static void main(String[] args) {
        launch(args);
    }
}