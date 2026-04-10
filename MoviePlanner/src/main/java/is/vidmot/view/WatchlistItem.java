package is.vidmot.view;

import is.vinnsla.Movie;
import is.vinnsla.WatchStatus;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

/**
 * Sérhæfður viðmótshlutur sem birtir eina vistaða mynd á watchlist.
 */
public class WatchlistItem extends HBox {

    /**
     * Smíðar nýtt watchlist item.
     *
     * @param movie          myndin sem á að sýna
     * @param onDelete       aðgerð sem keyrist við eyðingu
     * @param onStatusChange aðgerð sem keyrist þegar staða breytist
     */
    public WatchlistItem(Movie movie, Runnable onDelete, Consumer<WatchStatus> onStatusChange) {
        configureRoot();

        ImageView posterView = createPosterView(movie);
        Label titleLabel = createTitleLabel(movie);
        Label ratingLabel = createRatingLabel(movie);
        ComboBox<WatchStatus> statusBox = createStatusBox(movie, onStatusChange);
        VBox infoBox = createInfoBox(titleLabel, ratingLabel, statusBox);
        Region spacer = createSpacer();
        Button deleteButton = createDeleteButton(onDelete);

        getChildren().addAll(posterView, infoBox, spacer, deleteButton);
    }

    /**
     * Stillir grunnútlit á hlutnum.
     */
    private void configureRoot() {
        getStyleClass().add("watchlist-item");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(18);
        setPadding(new Insets(16));
        setCursor(Cursor.HAND);
    }

    /**
     * Býr til poster view fyrir watchlist item.
     *
     * @param movie myndin sem á að sýna
     * @return ImageView með poster
     */
    private ImageView createPosterView(Movie movie) {
        ImageView posterView = new ImageView();
        posterView.setFitWidth(78);
        posterView.setFitHeight(118);
        posterView.setPreserveRatio(false);
        posterView.getStyleClass().add("watchlist-poster");

        if (!movie.getPosterUrl().isBlank()) {
            posterView.setImage(new Image(movie.getPosterUrl(), true));
        }

        return posterView;
    }

    /**
     * Býr til titillabel.
     *
     * @param movie myndin sem á að sýna
     * @return Label með titli
     */
    private Label createTitleLabel(Movie movie) {
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("watchlist-title");
        return titleLabel;
    }

    /**
     * Býr til rating label.
     *
     * @param movie myndin sem á að sýna
     * @return Label með einkunn
     */
    private Label createRatingLabel(Movie movie) {
        Label ratingLabel = new Label("Einkunn: %.1f/10".formatted(movie.getRating()));
        ratingLabel.getStyleClass().add("watchlist-subtle");
        return ratingLabel;
    }

    /**
     * Býr til combo box fyrir stöðu myndar.
     *
     * @param movie          myndin sem á að sýna
     * @param onStatusChange aðgerð við stöðubreytingu
     * @return ComboBox fyrir stöður
     */
    private ComboBox<WatchStatus> createStatusBox(Movie movie, Consumer<WatchStatus> onStatusChange) {
        ComboBox<WatchStatus> statusBox = new ComboBox<>(FXCollections.observableArrayList(WatchStatus.values()));
        statusBox.setValue(movie.getWatchStatus());
        statusBox.getStyleClass().add("status-box");

        statusBox.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(WatchStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });

        statusBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(WatchStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });

        statusBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                onStatusChange.accept(newValue);
            }
        });

        statusBox.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        return statusBox;
    }

    /**
     * Býr til upplýsingabox með titli, rating og stöðuvali.
     *
     * @param titleLabel  titill myndar
     * @param ratingLabel rating myndar
     * @param statusBox   stöðuval
     * @return VBox með upplýsingum
     */
    private VBox createInfoBox(Label titleLabel, Label ratingLabel, ComboBox<WatchStatus> statusBox) {
        VBox infoBox = new VBox(titleLabel, ratingLabel, statusBox);
        infoBox.setSpacing(8);
        return infoBox;
    }

    /**
     * Býr til spacer sem ýtir delete takka út til hægri.
     *
     * @return spacer region
     */
    private Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /**
     * Býr til delete takka.
     *
     * @param onDelete aðgerð sem keyrist við eyðingu
     * @return Button fyrir eyðingu
     */
    private Button createDeleteButton(Runnable onDelete) {
        Button deleteButton = new Button("🗑");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> onDelete.run());
        deleteButton.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);
        return deleteButton;
    }
}