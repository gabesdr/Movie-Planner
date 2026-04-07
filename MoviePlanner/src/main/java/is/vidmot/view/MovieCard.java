package is.vidmot.view;

import is.vinnsla.Movie;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MovieCard extends VBox {
    private static final double CARD_WIDTH = 250;
    private static final double POSTER_HEIGHT = 360;

    public MovieCard(Movie movie, Runnable onClick) {
        getStyleClass().add("movie-card");
        setPrefWidth(CARD_WIDTH);
        setMinWidth(CARD_WIDTH);
        setMaxWidth(CARD_WIDTH);
        setSpacing(10);
        setPadding(new Insets(0, 0, 12, 0));
        setCursor(Cursor.HAND);

        ImageView posterView = new ImageView();
        posterView.setFitWidth(CARD_WIDTH);
        posterView.setFitHeight(POSTER_HEIGHT);
        posterView.setPreserveRatio(false);
        posterView.getStyleClass().add("movie-poster");
        if (!movie.getPosterUrl().isBlank()) {
            posterView.setImage(new Image(movie.getPosterUrl(), true));
        }

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("movie-card-title");
        titleLabel.setWrapText(true);

        Label ratingLabel = new Label("★ %.1f".formatted(movie.getRating()));
        ratingLabel.getStyleClass().add("movie-card-rating");

        Label yearLabel = new Label(movie.getYear() > 0 ? Integer.toString(movie.getYear()) : "—");
        yearLabel.getStyleClass().add("movie-card-year");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox metaBox = new VBox(titleLabel, spacer);
        metaBox.setSpacing(6);
        metaBox.setPadding(new Insets(0, 16, 0, 16));

        HBox footer = new HBox(ratingLabel, new Region(), yearLabel);
        footer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);
        footer.setPadding(new Insets(0, 16, 0, 16));

        getChildren().addAll(posterView, metaBox, footer);
        setOnMouseClicked(event -> onClick.run());
    }
}