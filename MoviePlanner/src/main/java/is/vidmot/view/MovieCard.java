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

/**
 * Sérhæfður viðmótshlutur sem birtir eina kvikmynd á leitarskjá.
 */
public class MovieCard extends VBox {

    /** Breidd á movie card. */
    private static final double CARD_WIDTH = 250;

    /** Hæð á poster mynd. */
    private static final double POSTER_HEIGHT = 360;

    /**
     * Smíðar nýtt movie card.
     *
     * @param movie   myndin sem á að sýna
     * @param onClick aðgerð sem keyrist þegar smellt er á spjaldið
     */
    public MovieCard(Movie movie, Runnable onClick) {
        configureRoot();

        ImageView posterView = createPosterView(movie);
        Label titleLabel = createTitleLabel(movie);
        Label ratingLabel = createRatingLabel(movie);
        Label yearLabel = createYearLabel(movie);

        VBox metaBox = createMetaBox(titleLabel);
        HBox footer = createFooter(ratingLabel, yearLabel);

        getChildren().addAll(posterView, metaBox, footer);
        setOnMouseClicked(event -> onClick.run());
    }

    /**
     * Stillir grunnútlit á spjaldinu.
     */
    private void configureRoot() {
        getStyleClass().add("movie-card");
        setPrefWidth(CARD_WIDTH);
        setMinWidth(CARD_WIDTH);
        setMaxWidth(CARD_WIDTH);
        setSpacing(10);
        setPadding(new Insets(0, 0, 12, 0));
        setCursor(Cursor.HAND);
    }

    /**
     * Býr til poster view fyrir myndina.
     *
     * @param movie myndin sem á að sýna
     * @return ImageView með poster
     */
    private ImageView createPosterView(Movie movie) {
        ImageView posterView = new ImageView();
        posterView.setFitWidth(CARD_WIDTH);
        posterView.setFitHeight(POSTER_HEIGHT);
        posterView.setPreserveRatio(false);
        posterView.getStyleClass().add("movie-poster");

        if (!movie.getPosterUrl().isBlank()) {
            posterView.setImage(new Image(movie.getPosterUrl(), true));
        }

        return posterView;
    }

    /**
     * Býr til titillabel fyrir mynd.
     *
     * @param movie myndin sem á að sýna
     * @return Label með titli
     */
    private Label createTitleLabel(Movie movie) {
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("movie-card-title");
        titleLabel.setWrapText(true);
        return titleLabel;
    }

    /**
     * Býr til rating label fyrir mynd.
     *
     * @param movie myndin sem á að sýna
     * @return Label með rating
     */
    private Label createRatingLabel(Movie movie) {
        Label ratingLabel = new Label("★ %.1f".formatted(movie.getRating()));
        ratingLabel.getStyleClass().add("movie-card-rating");
        return ratingLabel;
    }

    /**
     * Býr til year label fyrir mynd.
     *
     * @param movie myndin sem á að sýna
     * @return Label með ári
     */
    private Label createYearLabel(Movie movie) {
        Label yearLabel = new Label(movie.getYear() > 0 ? Integer.toString(movie.getYear()) : "—");
        yearLabel.getStyleClass().add("movie-card-year");
        return yearLabel;
    }

    /**
     * Býr til miðhluta spjaldsins með titli.
     *
     * @param titleLabel titill myndar
     * @return VBox með textahluta
     */
    private VBox createMetaBox(Label titleLabel) {
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox metaBox = new VBox(titleLabel, spacer);
        metaBox.setSpacing(6);
        metaBox.setPadding(new Insets(0, 16, 0, 16));
        return metaBox;
    }

    /**
     * Býr til footer með rating og ári.
     *
     * @param ratingLabel rating label
     * @param yearLabel   year label
     * @return footer hluti spjaldsins
     */
    private HBox createFooter(Label ratingLabel, Label yearLabel) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox footer = new HBox(ratingLabel, spacer, yearLabel);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(0, 16, 0, 16));
        return footer;
    }
}