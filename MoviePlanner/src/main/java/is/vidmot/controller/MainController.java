package is.vidmot.controller;

import is.vinnsla.Movie;
import is.vinnsla.TmdbService;
import is.vinnsla.WatchStatus;
import is.vinnsla.WatchlistStorage;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainController {

    private static final int TRENDING_BATCH_SIZE = 18;

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
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Hyperlink toggleDescriptionLink;
    @FXML private ImageView detailBackdropView;
    @FXML private StackPane detailModal;
    @FXML private ScrollPane detailScrollPane;
    @FXML private StackPane trailerContainer;
    @FXML private VBox trailerFallbackBox;
    @FXML private Label noTrailerLabel;
    @FXML private Button openTrailerButton;
    @FXML private HBox loadMoreRow;
    @FXML private Button loadMoreButton;

    private final ObservableList<Movie> searchResults = FXCollections.observableArrayList();
    private final ObservableList<Movie> watchlist = FXCollections.observableArrayList();
    private final TmdbService tmdbService = new TmdbService();
    private final WatchlistStorage watchlistStorage = new WatchlistStorage();

    private final List<Movie> trendingCache = new ArrayList<>();

    private boolean descriptionExpanded = false;
    private String fullDescription = "";
    private static final int DESCRIPTION_PREVIEW_LENGTH = 180;

    private boolean showingTrending = true;
    private boolean loadingTrending = false;
    private boolean trendingHasMore = true;
    private int nextTrendingPage = 1;
    private int displayedTrendingCount = 0;

    private Movie selectedMovie;
    private String currentTrailerUrl = "";

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

        overlayPane.setOnMouseClicked(event -> closeDetail());
        detailModal.setOnMouseClicked(event -> event.consume());

        toggleDescriptionLink.setOnAction(event -> toggleDescription());
        openTrailerButton.setOnAction(event -> openCurrentTrailerInBrowser());
        loadMoreButton.setOnAction(event -> loadMoreTrending());

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Vil horfa fyrst",
                "Kannski seinna fyrst",
                "Búinn að horfa fyrst"
        ));
        sortComboBox.setValue("Vil horfa fyrst");
        sortComboBox.setOnAction(event -> renderWatchlist());

        watchlist.setAll(watchlistStorage.loadWatchlist());

        watchlist.addListener((ListChangeListener<Movie>) change -> {
            updateCountLabel();
            renderWatchlist();
            updateDetailButtonState();
            saveWatchlist();
        });

        updateCountLabel();
        renderWatchlist();
        showTrendingHome();
    }

    @FXML
    private void searchMovies() {
        showSearchTab();

        String query = searchField.getText();

        if (query == null || query.isBlank()) {
            showTrendingHome();
            return;
        }

        showingTrending = false;
        loadingTrending = false;
        updateLoadMoreVisibility();

        searchButton.setDisable(true);
        searchButton.setText("...");
        emptySearchLabel.setVisible(false);
        emptySearchLabel.setManaged(false);

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

    private void showTrendingHome() {
        showSearchTab();
        showingTrending = true;
        displayedTrendingCount = 0;
        searchResults.clear();
        renderSearchResults();
        fetchTrendingUntil(TRENDING_BATCH_SIZE, true);
    }

    private void loadMoreTrending() {
        if (!showingTrending || loadingTrending) {
            return;
        }
        fetchTrendingUntil(displayedTrendingCount + TRENDING_BATCH_SIZE, false);
    }

    private void fetchTrendingUntil(int targetCount, boolean resetToFirstBatch) {
        if ((trendingCache.size() >= targetCount || !trendingHasMore) && !loadingTrending) {
            if (resetToFirstBatch) {
                displayedTrendingCount = Math.min(TRENDING_BATCH_SIZE, trendingCache.size());
            } else {
                displayedTrendingCount = Math.min(targetCount, trendingCache.size());
            }
            searchResults.setAll(trendingCache.subList(0, displayedTrendingCount));
            renderSearchResults();
            return;
        }

        loadingTrending = true;
        updateLoadMoreButtonLoading(true);

        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() {
                List<Movie> fetched = new ArrayList<>();

                while (trendingCache.size() + fetched.size() < targetCount && trendingHasMore) {
                    List<Movie> page = tmdbService.getTrendingMovies(nextTrendingPage);
                    if (page == null || page.isEmpty()) {
                        trendingHasMore = false;
                        break;
                    }
                    fetched.addAll(page);
                    nextTrendingPage++;
                }
                return fetched;
            }
        };

        task.setOnSucceeded(event -> {
            List<Movie> fetched = task.getValue();
            if (fetched != null && !fetched.isEmpty()) {
                trendingCache.addAll(fetched);
            } else if (trendingCache.isEmpty()) {
                trendingHasMore = false;
            }

            if (resetToFirstBatch) {
                displayedTrendingCount = Math.min(TRENDING_BATCH_SIZE, trendingCache.size());
            } else {
                displayedTrendingCount = Math.min(targetCount, trendingCache.size());
            }

            searchResults.setAll(trendingCache.subList(0, displayedTrendingCount));

            loadingTrending = false;
            updateLoadMoreButtonLoading(false);
            renderSearchResults();
        });

        task.setOnFailed(event -> {
            loadingTrending = false;
            updateLoadMoreButtonLoading(false);

            if (trendingCache.isEmpty()) {
                searchResults.clear();
            } else if (resetToFirstBatch) {
                displayedTrendingCount = Math.min(TRENDING_BATCH_SIZE, trendingCache.size());
                searchResults.setAll(trendingCache.subList(0, displayedTrendingCount));
            }

            renderSearchResults();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void updateLoadMoreButtonLoading(boolean isLoading) {
        loadMoreButton.setDisable(isLoading);
        loadMoreButton.setText(isLoading ? "..." : "Sjá fleiri");
    }

    private void renderSearchResults() {
        resultsPane.getChildren().clear();

        if (showingTrending) {
            searchResultsTitle.setText("Vinsælar myndir");
        } else {
            searchResultsTitle.setText("Leitarniðurstöður (" + searchResults.size() + ")");
        }

        if (searchResults.isEmpty()) {
            emptySearchLabel.setVisible(true);
            emptySearchLabel.setManaged(true);

            if (showingTrending) {
                if (loadingTrending) {
                    emptySearchLabel.setText("Sæki vinsælar myndir...");
                } else {
                    emptySearchLabel.setText("Engar vinsælar myndir tiltækar núna.");
                }
            } else {
                String query = searchField.getText();
                if (query == null || query.isBlank()) {
                    emptySearchLabel.setText("Leitaðu af kvikmynd...");
                } else {
                    emptySearchLabel.setText("Engar myndir fundust fyrir \"" + query + "\"");
                }
            }

            updateLoadMoreVisibility();
            return;
        }

        emptySearchLabel.setVisible(false);
        emptySearchLabel.setManaged(false);

        for (Movie movie : searchResults) {
            MovieCard card = new MovieCard(movie, () -> openMovieDetail(movie));
            resultsPane.getChildren().add(card);
        }

        updateLoadMoreVisibility();
    }

    private void updateLoadMoreVisibility() {
        boolean showButton = showingTrending
                && !searchResults.isEmpty()
                && (displayedTrendingCount < trendingCache.size() || trendingHasMore);

        loadMoreRow.setVisible(showButton);
        loadMoreRow.setManaged(showButton);
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

        List<Movie> sortedMovies = new ArrayList<>(watchlist);
        sortedMovies.sort(getWatchlistComparator());

        for (Movie movie : sortedMovies) {
            WatchlistItem item = new WatchlistItem(
                    movie,
                    () -> deleteFromWatchlist(movie),
                    newStatus -> {
                        movie.setWatchStatus(newStatus);
                        renderWatchlist();
                        saveWatchlist();
                    }
            );

            item.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    openMovieDetail(movie);
                }
            });

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

        detailDateLabel.setText(safeText(selectedMovie.getReleaseDate(), "Óþekkt dagsetning"));

        if (selectedMovie.getRuntimeMinutes() > 0) {
            detailRuntimeLabel.setText("%d mín".formatted(selectedMovie.getRuntimeMinutes()));
        } else {
            detailRuntimeLabel.setText("Lengd óþekkt");
        }

        updateDescriptionArea(selectedMovie.getDescription());

        detailGenresPane.getChildren().setAll(createChipLabels(selectedMovie.getGenres()));
        detailActorsPane.getChildren().setAll(createChipLabels(selectedMovie.getActors()));

        setPosterImage(selectedMovie.getPosterUrl());
        setBackdropImage(selectedMovie.getBackdropUrl());
        updateTrailerArea(selectedMovie);

        overlayPane.setVisible(true);
        overlayPane.setManaged(true);
        updateDetailButtonState();

        if (detailScrollPane != null) {
            detailScrollPane.setVvalue(0);
        }
    }

    private void setBackdropImage(String backdropUrl) {
        if (backdropUrl == null || backdropUrl.isBlank()) {
            detailBackdropView.setImage(null);
            return;
        }

        try {
            detailBackdropView.setImage(new Image(backdropUrl, true));
        } catch (Exception exception) {
            detailBackdropView.setImage(null);
        }
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

        searchTabButton.setId("activeTab");
        watchlistTabButton.setId(null);
    }

    private void showWatchlistTab() {
        searchView.setVisible(false);
        searchView.setManaged(false);

        watchlistView.setVisible(true);
        watchlistView.setManaged(true);

        searchTabButton.setId(null);
        watchlistTabButton.setId("activeTab");
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
        } catch (Exception exception) {
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
        detailBackdropView.setImage(null);
        addToListButton.setDisable(true);
        addToListButton.setText("+ Bæta á lista");

        descriptionExpanded = false;
        fullDescription = "";
        currentTrailerUrl = "";

        toggleDescriptionLink.setText("");
        toggleDescriptionLink.setVisible(false);
        toggleDescriptionLink.setManaged(false);

        noTrailerLabel.setText("Sýnishorn ekki til");
        openTrailerButton.setVisible(false);
        openTrailerButton.setManaged(false);
    }

    private void saveWatchlist() {
        watchlistStorage.saveWatchlist(new ArrayList<>(watchlist));
    }

    private String safeText(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private Comparator<Movie> getWatchlistComparator() {
        String selectedSort = sortComboBox.getValue();

        if ("Kannski seinna fyrst".equals(selectedSort)) {
            return Comparator.comparingInt(movie -> getSortPriority(movie, WatchStatus.KANNSKI_SEINNA));
        }

        if ("Búinn að horfa fyrst".equals(selectedSort)) {
            return Comparator.comparingInt(movie -> getSortPriority(movie, WatchStatus.BUINN_AD_HORFA));
        }

        return Comparator.comparingInt(movie -> getSortPriority(movie, WatchStatus.VIL_HORFA));
    }

    private int getSortPriority(Movie movie, WatchStatus preferredStatus) {
        WatchStatus status = movie.getWatchStatus();

        if (status == preferredStatus) {
            return 0;
        }
        if (status == WatchStatus.VIL_HORFA) {
            return 1;
        }
        if (status == WatchStatus.KANNSKI_SEINNA) {
            return 2;
        }
        if (status == WatchStatus.BUINN_AD_HORFA) {
            return 3;
        }
        return 99;
    }

    private void updateDescriptionArea(String description) {
        fullDescription = safeText(description, "Engin lýsing tiltæk.");
        descriptionExpanded = false;

        boolean needsToggle = fullDescription.length() > DESCRIPTION_PREVIEW_LENGTH;

        if (needsToggle) {
            detailDescriptionLabel.setText(getCollapsedDescription(fullDescription));
            toggleDescriptionLink.setText("Sjá meira");
            toggleDescriptionLink.setVisible(true);
            toggleDescriptionLink.setManaged(true);
        } else {
            detailDescriptionLabel.setText(fullDescription);
            toggleDescriptionLink.setVisible(false);
            toggleDescriptionLink.setManaged(false);
        }
    }

    private void toggleDescription() {
        if (fullDescription == null || fullDescription.isBlank()) {
            return;
        }

        descriptionExpanded = !descriptionExpanded;

        if (descriptionExpanded) {
            detailDescriptionLabel.setText(fullDescription);
            toggleDescriptionLink.setText("Sjá minna");
        } else {
            detailDescriptionLabel.setText(getCollapsedDescription(fullDescription));
            toggleDescriptionLink.setText("Sjá meira");
        }

        resizeDetailModal();
    }

    private String getCollapsedDescription(String text) {
        if (text == null || text.isBlank()) {
            return "Engin lýsing tiltæk.";
        }

        if (text.length() <= DESCRIPTION_PREVIEW_LENGTH) {
            return text;
        }

        return text.substring(0, DESCRIPTION_PREVIEW_LENGTH).trim() + "...";
    }

    private void updateTrailerArea(Movie movie) {
        String trailerKey = movie.getYoutubeTrailerKey();

        if (trailerKey == null || trailerKey.isBlank()) {
            currentTrailerUrl = "";
            noTrailerLabel.setText("Sýnishorn ekki til");
            openTrailerButton.setVisible(false);
            openTrailerButton.setManaged(false);
            return;
        }

        currentTrailerUrl = "https://www.youtube.com/watch?v=" + trailerKey;
        noTrailerLabel.setText("Sýnishorn tiltækt á YouTube");
        openTrailerButton.setVisible(true);
        openTrailerButton.setManaged(true);
    }

    private void openCurrentTrailerInBrowser() {
        if (currentTrailerUrl == null || currentTrailerUrl.isBlank()) {
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(currentTrailerUrl));
        } catch (Exception exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Villa");
            alert.setHeaderText("Gat ekki opnað YouTube");
            alert.setContentText("Ekki tókst að opna sýnishornið í vafra.");
            alert.showAndWait();
        }
    }

    private void resizeDetailModal() {
        overlayPane.applyCss();
        overlayPane.layout();
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
        copy.setBackdropUrl(source.getBackdropUrl());
        copy.setYoutubeTrailerKey(source.getYoutubeTrailerKey());
        return copy;
    }
}