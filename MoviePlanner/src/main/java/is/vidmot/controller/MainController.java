package is.vidmot.controller;

import is.vinnsla.Movie;
import is.vinnsla.TmdbService;
import is.vidmot.view.MovieCard;
import is.vidmot.view.WatchlistItem;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button searchTabButton;
    @FXML private Button watchlistTabButton;
    @FXML private Label countLabel;
    @FXML private Label searchResultsTitle;
    @FXML private FlowPane resultsPane;
    @FXML private VBox watchlistPane;
    @FXML private VBox searchView;
    @FXML private VBox watchlistView;
    @FXML private Label emptySearchLabel;
    @FXML private Label emptyWatchlistLabel;
    @FXML private StackPane overlayPane;
    @FXML private Button closeDetailButton;
    @FXML private Label detailTitleLabel;
    @FXML private Label detailRatingLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailRuntimeLabel;
    @FXML private Label detailDescriptionLabel;
    @FXML private FlowPane detailGenresPane;
    @FXML private FlowPane detailActorsPane;
    @FXML private ImageView detailPosterView;
    @FXML private Button addToListButton;
    @FXML private ScrollPane resultsScrollPane;

    private final ObservableList<Movie> searchResults = FXCollections.observableArrayList();
    private final ObservableList<Movie> watchlist = FXCollections.observableArrayList();
    private final TmdbService tmdbService = new TmdbService();

    private Movie selectedMovie;

    @FXML
    private void initialize() {
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);

        clearDetailView();
        showSearchTab();

        searchButton.setOnAction(event -> searchMovies());
        searchField.setOnAction(event -> searchMovies());

        searchTabButton.setOnAction(event -> showSearchTab());
        watchlistTabButton.setOnAction(event -> showWatchlistTab());

        closeDetailButton.setOnAction(event -> closeDetail());
        addToListButton.setOnAction(event -> addSelectedMovieToWatchlist());

        watchlist.addListener((ListChangeListener<Movie>) change -> {
            updateCountLabel();
            renderWatchlist();
            updateDetailButtonState();
        });

        updateCountLabel();
        renderSearchResults();
        renderWatchlist();
    }

    @FXML
    private void searchMovies() {
        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            searchResults.clear();
            renderSearchResults();
            return;
        }

        searchButton.setDisable(true);
        searchButton.setText("...");
        emptySearchLabel.setText("Leita...");

        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() {
                return tmdbService.searchMovies(query.trim());
            }
        };

        task.setOnSucceeded(event -> {
            List<Movie> result = task.getValue();
            searchResults.setAll(result != null ? result : List.of());
            renderSearchResults();
            searchButton.setDisable(false);
            searchButton.setText("Leita");
        });

        task.setOnFailed(event -> {
            searchResults.clear();
            renderSearchResults();
            emptySearchLabel.setVisible(true);
            emptySearchLabel.setManaged(true);
            emptySearchLabel.setText("Villa kom upp við leit.");
            searchButton.setDisable(false);
            searchButton.setText("Leita");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void renderSearchResults() {
        resultsPane.getChildren().clear();

        if (searchResults.isEmpty()) {
            searchResultsTitle.setText("Leitarniðurstöður (0)");
            emptySearchLabel.setVisible(true);
            emptySearchLabel.setManaged(true);

            String query = searchField.getText();
            if (query == null || query.isBlank()) {
                emptySearchLabel.setText("Leitaðu að kvikmynd...");
            } else {
                emptySearchLabel.setText("Engar myndir fundust fyrir \"" + query + "\"");
            }
            return;
        }

        emptySearchLabel.setVisible(false);
        emptySearchLabel.setManaged(false);
        searchResultsTitle.setText("Leitarniðurstöður (" + searchResults.size() + ")");

        for (Movie movie : searchResults) {
            MovieCard card = new MovieCard(movie, () -> openMovieDetail(movie));
            resultsPane.getChildren().add(card);
        }
    }

    private void renderWatchlist() {
        watchlistPane.getChildren().clear();

        if (watchlist.isEmpty()) {
            emptyWatchlistLabel.setVisible(true);
            emptyWatchlistLabel.setManaged(true);
            return;
        }

        emptyWatchlistLabel.setVisible(false);
        emptyWatchlistLabel.setManaged(false);

        for (Movie movie : watchlist) {
            WatchlistItem item = new WatchlistItem(
                    movie,
                    () -> deleteFromWatchlist(movie),
                    newStatus -> movie.setWatchStatus(newStatus)
            );
            VBox.setMargin(item, new Insets(0, 0, 16, 0));
            watchlistPane.getChildren().add(item);
        }
    }

    private void openMovieDetail(Movie movie) {
        if (movie == null) {
            return;
        }

        Movie enrichedMovie = tmdbService.enrichMovie(copyMovie(movie));
        selectedMovie = enrichedMovie;

        detailTitleLabel.setText(safeText(selectedMovie.getTitle(), "Óþekkt mynd"));

        if (selectedMovie.getRating() > 0) {
            detailRatingLabel.setText("★ %.1f/10".formatted(selectedMovie.getRating()));
        } else {
            detailRatingLabel.setText("★ Engin einkunn");
        }

        detailDateLabel.setText(
                safeText(selectedMovie.getReleaseDate(), "Óþekkt dagsetning")
        );

        if (selectedMovie.getRuntimeMinutes() > 0) {
            detailRuntimeLabel.setText("%d mín".formatted(selectedMovie.getRuntimeMinutes()));
        } else {
            detailRuntimeLabel.setText("Lengd óþekkt");
        }

        detailDescriptionLabel.setText(
                safeText(selectedMovie.getDescription(), "Engin lýsing tiltæk.")
        );

        detailGenresPane.getChildren().setAll(createChipLabels(selectedMovie.getGenres()));
        detailActorsPane.getChildren().setAll(createChipLabels(selectedMovie.getActors()));

        setPosterImage(selectedMovie.getPosterUrl());

        overlayPane.setVisible(true);
        overlayPane.setManaged(true);
        updateDetailButtonState();
    }

    private void closeDetail() {
        overlayPane.setVisible(false);
        overlayPane.setManaged(false);
        selectedMovie = null;
        clearDetailView();
    }

    private void addSelectedMovieToWatchlist() {
        if (selectedMovie == null) {
            return;
        }

        boolean alreadySaved = watchlist.stream()
                .anyMatch(movie -> movie.getId() == selectedMovie.getId());

        if (!alreadySaved) {
            watchlist.add(copyMovie(selectedMovie));
        }

        updateDetailButtonState();
        showWatchlistTab();
        closeDetail();
    }

    private void deleteFromWatchlist(Movie movie) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Staðfesting");
        alert.setHeaderText("Eyða mynd af lista?");
        alert.setContentText("Myndin verður fjarlægð af listanum þínum.");

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                watchlist.remove(movie);
            }
        });
    }

    private void showSearchTab() {
        searchView.setVisible(true);
        searchView.setManaged(true);

        watchlistView.setVisible(false);
        watchlistView.setManaged(false);

        searchTabButton.getStyleClass().remove("tab-button-active");
        watchlistTabButton.getStyleClass().remove("tab-button-active");
        searchTabButton.getStyleClass().add("tab-button-active");
    }

    private void showWatchlistTab() {
        searchView.setVisible(false);
        searchView.setManaged(false);

        watchlistView.setVisible(true);
        watchlistView.setManaged(true);

        searchTabButton.getStyleClass().remove("tab-button-active");
        watchlistTabButton.getStyleClass().remove("tab-button-active");
        watchlistTabButton.getStyleClass().add("tab-button-active");
    }

    private void updateCountLabel() {
        countLabel.setText(watchlist.size() + " myndir á listanum");
        watchlistTabButton.setText("Minn listi (" + watchlist.size() + ")");
    }

    private void updateDetailButtonState() {
        if (selectedMovie == null) {
            addToListButton.setDisable(true);
            addToListButton.setText("+ Bæta á lista");
            return;
        }

        boolean alreadySaved = watchlist.stream()
                .anyMatch(movie -> movie.getId() == selectedMovie.getId());

        addToListButton.setDisable(alreadySaved);
        addToListButton.setText(alreadySaved ? "✓ Á listanum" : "+ Bæta á lista");
    }

    private List<Label> createChipLabels(List<String> items) {
        List<Label> labels = new ArrayList<>();

        if (items == null || items.isEmpty()) {
            Label emptyLabel = new Label("Ótilgreint");
            emptyLabel.getStyleClass().add("chip-label");
            labels.add(emptyLabel);
            return labels;
        }

        for (String item : items) {
            if (item != null && !item.isBlank()) {
                Label label = new Label(item);
                label.getStyleClass().add("chip-label");
                labels.add(label);
            }
        }

        if (labels.isEmpty()) {
            Label emptyLabel = new Label("Ótilgreint");
            emptyLabel.getStyleClass().add("chip-label");
            labels.add(emptyLabel);
        }

        return labels;
    }

    private void setPosterImage(String posterUrl) {
        if (posterUrl == null || posterUrl.isBlank()) {
            detailPosterView.setImage(null);
            return;
        }

        try {
            detailPosterView.setImage(new Image(posterUrl, true));
        } catch (Exception e) {
            detailPosterView.setImage(null);
        }
    }

    private void clearDetailView() {
        detailTitleLabel.setText("");
        detailRatingLabel.setText("");
        detailDateLabel.setText("");
        detailRuntimeLabel.setText("");
        detailDescriptionLabel.setText("");
        detailGenresPane.getChildren().clear();
        detailActorsPane.getChildren().clear();
        detailPosterView.setImage(null);
        addToListButton.setDisable(true);
        addToListButton.setText("+ Bæta á lista");
    }

    private String safeText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private Movie copyMovie(Movie source) {
        Movie copy = new Movie(
                source.getId(),
                source.getTitle(),
                source.getPosterUrl(),
                source.getDescription(),
                source.getRating(),
                source.getYear()
        );
        copy.setReleaseDate(source.getReleaseDate());
        copy.setRuntimeMinutes(source.getRuntimeMinutes());
        copy.setGenres(source.getGenres());
        copy.setActors(source.getActors());
        copy.setWatchStatus(source.getWatchStatus());
        return copy;
    }
}