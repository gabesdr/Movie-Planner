package is.vidmot.view;

import is.vinnsla.Movie;
import is.vinnsla.WatchStatus;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;


public class WatchlistItem extends HBox {
    public WatchlistItem(Movie movie, Runnable onDelete, Consumer<WatchStatus> onStatusChange) {
        getStyleClass().add("watchlist-item");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(18);
        setPadding(new Insets(16));

        ImageView posterView = new ImageView();
        posterView.setFitWidth(78);
        posterView.setFitHeight(118);
        posterView.setPreserveRatio(false);
        posterView.getStyleClass().add("watchlist-poster");
        if (!movie.getPosterUrl().isBlank()) {
            posterView.setImage(new Image(movie.getPosterUrl(), true));
        }

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("watchlist-title");

        Label ratingLabel = new Label("Einkunn: %.1f/10".formatted(movie.getRating()));
        ratingLabel.getStyleClass().add("watchlist-subtle");

        ComboBox<WatchStatus> statusBox = new ComboBox<>(FXCollections.observableArrayList(WatchStatus.values()));
        statusBox.setValue(movie.getWatchStatus());
        statusBox.getStyleClass().add("status-box");
        statusBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                onStatusChange.accept(newValue);
            }
        });
        statusBox.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(WatchStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });
        statusBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(WatchStatus item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDisplayName());
            }
        });

        VBox infoBox = new VBox(titleLabel, ratingLabel, statusBox);
        infoBox.setSpacing(8);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteButton = new Button("🗑");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(event -> onDelete.run());

        getChildren().addAll(posterView, infoBox, spacer, deleteButton);
    }
}