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
import javafx.scene.Node;
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

/**
 * Stýrir aðalviðmóti Movie Planner forritsins.
 * Controllerinn sér um leit, birtingu vinsælla mynda, watchlist,
 * nánari upplýsingar um myndir og opnun á YouTube sýnishornum.
 */
public class MainController {

    /** Fjöldi trending mynda sem eru sóttar í hverju skrefi. */
    private static final int TRENDING_BATCH_SIZE = 18;

    /** Hámarksfjöldi stafa sem birtist áður en lýsing er stytt. */
    private static final int DESCRIPTION_PREVIEW_LENGTH = 180;

    /** Leitarreitur fyrir kvikmyndir. */
    @FXML
    private TextField searchField;

    /** Hnappur sem keyrir leit. */
    @FXML
    private Button searchButton;

    /** Takkinn fyrir leitarskjáinn. */
    @FXML
    private Button searchTabButton;

    /** Takkinn fyrir watchlist skjáinn. */
    @FXML
    private Button watchlistTabButton;

    /** Merki sem sýnir fjölda vistaðra mynda. */
    @FXML
    private Label countLabel;

    /** Fyrirsögn fyrir leitarniðurstöður eða trending lista. */
    @FXML
    private Label searchResultsTitle;

    /** Svæði sem heldur utan um movie cards í leit. */
    @FXML
    private FlowPane resultsPane;

    /** Svæði sem heldur utan um vistaðar myndir. */
    @FXML
    private VBox watchlistPane;

    /** Viðmót fyrir leitarskjáinn. */
    @FXML
    private VBox searchView;

    /** Viðmót fyrir watchlist skjáinn. */
    @FXML
    private VBox watchlistView;

    /** Tómleikamelding á leitarskjá. */
    @FXML
    private Label emptySearchLabel;

    /** Tómleikamelding á watchlist skjá. */
    @FXML
    private Label emptyWatchlistLabel;

    /** Dökk yfirbreiðsla á bak við detail gluggann. */
    @FXML
    private StackPane overlayPane;

    /** Hnappur sem lokar detail glugga. */
    @FXML
    private Button closeDetailButton;

    /** Titill myndar í detail glugga. */
    @FXML
    private Label detailTitleLabel;

    /** Einkunn myndar í detail glugga. */
    @FXML
    private Label detailRatingLabel;

    /** Útgáfudagur myndar í detail glugga. */
    @FXML
    private Label detailDateLabel;

    /** Lengd myndar í detail glugga. */
    @FXML
    private Label detailRuntimeLabel;

    /** Lýsing myndar í detail glugga. */
    @FXML
    private Label detailDescriptionLabel;

    /** Flæði fyrir genres chips. */
    @FXML
    private FlowPane detailGenresPane;

    /** Flæði fyrir actors chips. */
    @FXML
    private FlowPane detailActorsPane;

    /** Poster mynd í detail glugga. */
    @FXML
    private ImageView detailPosterView;

    /** Hnappur til að bæta mynd á watchlist. */
    @FXML
    private Button addToListButton;

    /** Scroll pane fyrir leitarskjá. */
    @FXML
    private ScrollPane detailScrollPane;

    /** Drop-down til að raða watchlist. */
    @FXML
    private ComboBox<String> sortComboBox;

    /** Link fyrir sjá meira / sjá minna á lýsingu. */
    @FXML
    private Hyperlink toggleDescriptionLink;

    /** Backdrop mynd í detail glugga. */
    @FXML
    private ImageView detailBackdropView;

    /** Root node fyrir detail gluggann. */
    @FXML
    private StackPane detailModal;

    /** Texti sem segir að trailer sé ekki tiltækt. */
    @FXML
    private Label noTrailerLabel;

    /** Hnappur sem opnar trailer í vafra. */
    @FXML
    private Button openTrailerButton;

    /** Rað sem heldur utan um "Sjá fleiri" hnappinn. */
    @FXML
    private HBox loadMoreRow;

    /** Hnappur til að sækja fleiri trending myndir. */
    @FXML
    private Button loadMoreButton;

    /** Observable listi fyrir niðurstöður leitar eða trending. */
    private final ObservableList<Movie> searchResults = FXCollections.observableArrayList();

    /** Observable listi fyrir vistaðar myndir. */
    private final ObservableList<Movie> watchlist = FXCollections.observableArrayList();

    /** Þjónusta sem sækir gögn úr TMDB. */
    private final TmdbService tmdbService = new TmdbService();

    /** Þjónusta sem vistar og les watchlist. */
    private final WatchlistStorage watchlistStorage = new WatchlistStorage();

    /** Cache af sóttum trending myndum. */
    private final List<Movie> trendingCache = new ArrayList<>();

    /** Segir til um hvort lýsing í detail glugga sé útvíkkuð. */
    private boolean descriptionExpanded;

    /** Full lýsing á valinni mynd. */
    private String fullDescription = "";

    /** Segir til um hvort verið sé að sýna trending efni. */
    private boolean showingTrending = true;

    /** Segir til um hvort trending efni sé í hleðslu. */
    private boolean loadingTrending;

    /** Segir til um hvort til séu fleiri trending síður. */
    private boolean trendingHasMore = true;

    /** Næsta trending síða sem á að sækja. */
    private int nextTrendingPage = 1;

    /** Fjöldi trending mynda sem eru sýndar núna. */
    private int displayedTrendingCount;

    /** Valin mynd í detail glugga. */
    private Movie selectedMovie;

    /** Slóð á YouTube trailer fyrir valda mynd. */
    private String currentTrailerUrl = "";

    /**
     * Upphafsstillir controllerinn eftir að FXML hefur verið hlaðið.
     */
    @FXML
    private void initialize() {
        configureInitialState();
        configureActions();
        configureSortBox();
        loadSavedWatchlist();
        showTrendingHome();
    }

    /**
     * Framkvæmir leit að kvikmynd eftir texta í leitarreit.
     * Ef reiturinn er tómur eru trending myndir birtar aftur.
     */
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
        setSearchLoadingState(true);
        setVisibleManaged(emptySearchLabel, false);

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
            setSearchLoadingState(false);
        });

        task.setOnFailed(event -> {
            searchResults.clear();
            renderSearchResults();
            emptySearchLabel.setText("Villa kom upp við leit.");
            setVisibleManaged(emptySearchLabel, true);
            setSearchLoadingState(false);
        });

        startBackgroundTask(task);
    }

    /**
     * Sýnir upphafsskjá með trending myndum.
     */
    private void showTrendingHome() {
        showSearchTab();
        showingTrending = true;
        displayedTrendingCount = 0;
        searchResults.clear();
        renderSearchResults();
        fetchTrendingUntil(TRENDING_BATCH_SIZE, true);
    }

    /**
     * Sækir næstu lotu af trending myndum.
     */
    private void loadMoreTrending() {
        if (!showingTrending || loadingTrending) {
            return;
        }
        fetchTrendingUntil(displayedTrendingCount + TRENDING_BATCH_SIZE, false);
    }

    /**
     * Sækir trending myndir þar til markfjölda hefur verið náð.
     *
     * @param targetCount       markfjöldi mynda sem á að sýna
     * @param resetToFirstBatch segir til um hvort endurstilla eigi sýndan fjölda
     */
    private void fetchTrendingUntil(int targetCount, boolean resetToFirstBatch) {
        if ((trendingCache.size() >= targetCount || !trendingHasMore) && !loadingTrending) {
            displayedTrendingCount = resetToFirstBatch
                    ? Math.min(TRENDING_BATCH_SIZE, trendingCache.size())
                    : Math.min(targetCount, trendingCache.size());
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

            displayedTrendingCount = resetToFirstBatch
                    ? Math.min(TRENDING_BATCH_SIZE, trendingCache.size())
                    : Math.min(targetCount, trendingCache.size());

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

        startBackgroundTask(task);
    }

    /**
     * Uppfærir texta og virkni á "Sjá fleiri" hnappnum.
     *
     * @param isLoading true ef verið er að sækja fleiri myndir
     */
    private void updateLoadMoreButtonLoading(boolean isLoading) {
        loadMoreButton.setDisable(isLoading);
        loadMoreButton.setText(isLoading ? "..." : "Sjá fleiri");
    }

    /**
     * Teiknar upp leitarskjáinn út frá núverandi niðurstöðum.
     */
    private void renderSearchResults() {
        resultsPane.getChildren().clear();
        searchResultsTitle.setText(showingTrending
                ? "Vinsælar myndir"
                : "Leitarniðurstöður (" + searchResults.size() + ")");

        if (searchResults.isEmpty()) {
            updateEmptySearchLabel();
            updateLoadMoreVisibility();
            return;
        }

        setVisibleManaged(emptySearchLabel, false);

        for (Movie movie : searchResults) {
            resultsPane.getChildren().add(new MovieCard(movie, () -> openMovieDetail(movie)));
        }

        updateLoadMoreVisibility();
    }

    /**
     * Sýnir eða felur "Sjá fleiri" eftir stöðu trending birtingar.
     */
    private void updateLoadMoreVisibility() {
        boolean showButton = showingTrending
                && !searchResults.isEmpty()
                && (displayedTrendingCount < trendingCache.size() || trendingHasMore);

        setVisibleManaged(loadMoreRow, showButton);
    }

    /**
     * Teiknar upp watchlist skjáinn.
     */
    private void renderWatchlist() {
        watchlistPane.getChildren().clear();

        if (watchlist.isEmpty()) {
            setVisibleManaged(emptyWatchlistLabel, true);
            return;
        }

        setVisibleManaged(emptyWatchlistLabel, false);

        List<Movie> sortedMovies = new ArrayList<>(watchlist);
        sortedMovies.sort(getWatchlistComparator());

        for (Movie movie : sortedMovies) {
            WatchlistItem item = new WatchlistItem(
                    movie,
                    () -> deleteFromWatchlist(movie),
                    newStatus -> updateMovieStatus(movie, newStatus)
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

    /**
     * Opnar nánari upplýsingar um mynd í detail glugga.
     *
     * @param movie myndin sem á að sýna
     */
    private void openMovieDetail(Movie movie) {
        if (movie == null) {
            return;
        }

        selectedMovie = tmdbService.enrichMovie(copyMovie(movie));
        updateDetailLabels(selectedMovie);
        updateDescriptionArea(selectedMovie.getDescription());
        detailGenresPane.getChildren().setAll(createChipLabels(selectedMovie.getGenres()));
        detailActorsPane.getChildren().setAll(createChipLabels(selectedMovie.getActors()));
        setPosterImage(selectedMovie.getPosterUrl());
        setBackdropImage(selectedMovie.getBackdropUrl());
        updateTrailerArea(selectedMovie);

        setVisibleManaged(overlayPane, true);
        updateDetailButtonState();

        if (detailScrollPane != null) {
            detailScrollPane.setVvalue(0);
        }
    }

    /**
     * Lokar detail glugga og hreinsar upplýsingar.
     */
    private void closeDetail() {
        setVisibleManaged(overlayPane, false);
        selectedMovie = null;
        clearDetailView();
    }

    /**
     * Bætir valinni mynd á watchlist ef hún er ekki þar nú þegar.
     */
    private void addSelectedMovieToWatchlist() {
        if (selectedMovie == null || isMovieSaved(selectedMovie)) {
            return;
        }

        watchlist.add(copyMovie(selectedMovie));
        updateDetailButtonState();
        closeDetail();
    }

    /**
     * Biður notanda um staðfestingu áður en mynd er eytt af watchlist.
     *
     * @param movie myndin sem á að eyða
     */
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

    /**
     * Sýnir leitarskjáinn og felur watchlist skjáinn.
     */
    private void showSearchTab() {
        setVisibleManaged(searchView, true);
        setVisibleManaged(watchlistView, false);
        searchTabButton.setId("activeTab");
        watchlistTabButton.setId(null);
    }

    /**
     * Sýnir watchlist skjáinn og felur leitarskjáinn.
     */
    private void showWatchlistTab() {
        setVisibleManaged(searchView, false);
        setVisibleManaged(watchlistView, true);
        searchTabButton.setId(null);
        watchlistTabButton.setId("activeTab");
    }

    /**
     * Uppfærir texta sem sýnir fjölda vistaðra mynda.
     */
    private void updateCountLabel() {
        countLabel.setText(watchlist.size() + " myndir á listanum");
        watchlistTabButton.setText("Minn listi (" + watchlist.size() + ")");
    }

    /**
     * Uppfærir stöðu og texta á takkanum sem bætir mynd á lista.
     */
    private void updateDetailButtonState() {
        if (selectedMovie == null) {
            addToListButton.setDisable(true);
            addToListButton.setText("+ Bæta á lista");
            return;
        }

        boolean alreadySaved = isMovieSaved(selectedMovie);
        addToListButton.setDisable(alreadySaved);
        addToListButton.setText(alreadySaved ? "✓ Á listanum" : "+ Bæta á lista");
    }

    /**
     * Býr til chips fyrir genre eða actors lista.
     *
     * @param items textagögn sem á að sýna
     * @return listi af Label hlutum
     */
    private List<Label> createChipLabels(List<String> items) {
        List<Label> labels = new ArrayList<>();

        if (items == null || items.isEmpty()) {
            labels.add(createChipLabel("Ótilgreint"));
            return labels;
        }

        for (String item : items) {
            if (item != null && !item.isBlank()) {
                labels.add(createChipLabel(item));
            }
        }

        if (labels.isEmpty()) {
            labels.add(createChipLabel("Ótilgreint"));
        }

        return labels;
    }

    /**
     * Setur poster mynd í detail glugga.
     *
     * @param posterUrl slóð á poster
     */
    private void setPosterImage(String posterUrl) {
        detailPosterView.setImage(loadImageSafely(posterUrl));
    }

    /**
     * Setur backdrop mynd í detail glugga.
     *
     * @param backdropUrl slóð á backdrop
     */
    private void setBackdropImage(String backdropUrl) {
        detailBackdropView.setImage(loadImageSafely(backdropUrl));
    }

    /**
     * Hreinsar allt efni úr detail glugga.
     */
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
        setVisibleManaged(toggleDescriptionLink, false);

        noTrailerLabel.setText("Sýnishorn ekki til");
        setVisibleManaged(openTrailerButton, false);
    }

    /**
     * Vistar watchlist í geymslu.
     */
    private void saveWatchlist() {
        watchlistStorage.saveWatchlist(new ArrayList<>(watchlist));
    }

    /**
     * Skilar öryggistexta ef strengur er tómur eða null.
     *
     * @param value    strengur
     * @param fallback sjálfgefið gildi
     * @return value eða fallback
     */
    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * Skilar comparator fyrir watchlist út frá núverandi röðunarvali.
     *
     * @return comparator fyrir röðun
     */
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

    /**
     * Reiknar forgangsröð fyrir stöðu myndar.
     *
     * @param movie            myndin sem er skoðuð
     * @param preferredStatus  staðan sem á að vera efst
     * @return heiltala sem táknar röðunarforgang
     */
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

    /**
     * Uppfærir lýsingu í detail glugga og ákveður hvort sýna eigi
     * "sjá meira/minna" tengilinn.
     *
     * @param description lýsing myndar
     */
    private void updateDescriptionArea(String description) {
        fullDescription = safeText(description, "Engin lýsing tiltæk.");
        descriptionExpanded = false;

        boolean needsToggle = fullDescription.length() > DESCRIPTION_PREVIEW_LENGTH;
        detailDescriptionLabel.setText(needsToggle
                ? getCollapsedDescription(fullDescription)
                : fullDescription);

        if (needsToggle) {
            toggleDescriptionLink.setText("Sjá meira");
            setVisibleManaged(toggleDescriptionLink, true);
        } else {
            setVisibleManaged(toggleDescriptionLink, false);
        }
    }

    /**
     * Víxlar á milli stuttrar og fullrar lýsingar.
     */
    private void toggleDescription() {
        if (fullDescription.isBlank()) {
            return;
        }

        descriptionExpanded = !descriptionExpanded;
        detailDescriptionLabel.setText(descriptionExpanded
                ? fullDescription
                : getCollapsedDescription(fullDescription));
        toggleDescriptionLink.setText(descriptionExpanded ? "Sjá minna" : "Sjá meira");
        resizeDetailModal();
    }

    /**
     * Skilar stytta útgáfu af lýsingu.
     *
     * @param text lýsing
     * @return stytt lýsing eða fallback texti
     */
    private String getCollapsedDescription(String text) {
        if (text == null || text.isBlank()) {
            return "Engin lýsing tiltæk.";
        }
        if (text.length() <= DESCRIPTION_PREVIEW_LENGTH) {
            return text;
        }
        return text.substring(0, DESCRIPTION_PREVIEW_LENGTH).trim() + "...";
    }

    /**
     * Uppfærir trailer svæði miðað við valda mynd.
     *
     * @param movie myndin sem er valin
     */
    private void updateTrailerArea(Movie movie) {
        String trailerKey = movie.getYoutubeTrailerKey();

        if (trailerKey == null || trailerKey.isBlank()) {
            currentTrailerUrl = "";
            noTrailerLabel.setText("Sýnishorn ekki til");
            setVisibleManaged(openTrailerButton, false);
            return;
        }

        currentTrailerUrl = "https://www.youtube.com/watch?v=" + trailerKey;
        noTrailerLabel.setText("Sýnishorn tiltækt á YouTube");
        setVisibleManaged(openTrailerButton, true);
    }

    /**
     * Opnar núverandi trailer í sjálfgefnum vafra.
     */
    private void openCurrentTrailerInBrowser() {
        if (currentTrailerUrl.isBlank() || !Desktop.isDesktopSupported()) {
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(currentTrailerUrl));
        } catch (Exception exception) {
            showErrorAlert(
                    "Villa",
                    "Gat ekki opnað YouTube",
                    "Ekki tókst að opna sýnishornið í vafra."
            );
        }
    }

    /**
     * Kallar á CSS/layout uppfærslu á detail glugga.
     */
    private void resizeDetailModal() {
        overlayPane.applyCss();
        overlayPane.layout();
    }

    /**
     * Býr til afrit af Movie hlut svo hægt sé að vinna með hann án þess
     * að breyta frumgögnum beint.
     *
     * @param source upprunaleg mynd
     * @return afrit af mynd
     */
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

    /**
     * Stillir grunnstöðu á viðmóti og detail glugga.
     */
    private void configureInitialState() {
        setVisibleManaged(overlayPane, false);
        clearDetailView();
        showSearchTab();
        updateCountLabel();
        renderWatchlist();
    }

    /**
     * Tengir actions við viðmótshluta.
     */
    private void configureActions() {
        searchButton.setOnAction(event -> searchMovies());
        searchField.setOnAction(event -> searchMovies());
        searchTabButton.setOnAction(event -> showSearchTab());
        watchlistTabButton.setOnAction(event -> showWatchlistTab());
        closeDetailButton.setOnAction(event -> closeDetail());
        addToListButton.setOnAction(event -> addSelectedMovieToWatchlist());
        toggleDescriptionLink.setOnAction(event -> toggleDescription());
        openTrailerButton.setOnAction(event -> openCurrentTrailerInBrowser());
        loadMoreButton.setOnAction(event -> loadMoreTrending());

        overlayPane.setOnMouseClicked(event -> closeDetail());
        detailModal.setOnMouseClicked(event -> event.consume());
    }

    /**
     * Stillir sort combo box fyrir watchlist.
     */
    private void configureSortBox() {
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Vil horfa fyrst",
                "Kannski seinna fyrst",
                "Búinn að horfa fyrst"
        ));
        sortComboBox.setValue("Vil horfa fyrst");
        sortComboBox.setOnAction(event -> renderWatchlist());
    }

    /**
     * Les vistaðan watchlist og tengir listener fyrir breytingar.
     */
    private void loadSavedWatchlist() {
        watchlist.setAll(watchlistStorage.loadWatchlist());
        watchlist.addListener((ListChangeListener<Movie>) change -> {
            updateCountLabel();
            renderWatchlist();
            updateDetailButtonState();
            saveWatchlist();
        });
    }

    /**
     * Stillir loading state á leit.
     *
     * @param isLoading true ef leit er í gangi
     */
    private void setSearchLoadingState(boolean isLoading) {
        searchButton.setDisable(isLoading);
        searchButton.setText(isLoading ? "..." : "Leita");
    }

    /**
     * Ræsir JavaFX Task í daemon þræði.
     *
     * @param task task sem á að keyra
     */
    private void startBackgroundTask(Task<?> task) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Uppfærir tómleikameldinguna á leitarskjá.
     */
    private void updateEmptySearchLabel() {
        setVisibleManaged(emptySearchLabel, true);

        if (showingTrending) {
            emptySearchLabel.setText(loadingTrending
                    ? "Sæki vinsælar myndir..."
                    : "Engar vinsælar myndir tiltækar núna.");
            return;
        }

        String query = searchField.getText();
        if (query == null || query.isBlank()) {
            emptySearchLabel.setText("Leitaðu að kvikmynd...");
        } else {
            emptySearchLabel.setText("Engar myndir fundust fyrir \"" + query + "\"");
        }
    }

    /**
     * Uppfærir stöðu myndar í watchlist.
     *
     * @param movie      myndin sem er uppfærð
     * @param newStatus  ný staða
     */
    private void updateMovieStatus(Movie movie, WatchStatus newStatus) {
        movie.setWatchStatus(newStatus);
        renderWatchlist();
        saveWatchlist();
    }

    /**
     * Uppfærir helstu textaupplýsingar í detail glugga.
     *
     * @param movie myndin sem er sýnd
     */
    private void updateDetailLabels(Movie movie) {
        detailTitleLabel.setText(safeText(movie.getTitle(), "Óþekkt mynd"));
        detailRatingLabel.setText(movie.getRating() > 0
                ? "★ %.1f/10".formatted(movie.getRating())
                : "★ Engin einkunn");
        detailDateLabel.setText(safeText(movie.getReleaseDate(), "Óþekkt dagsetning"));
        detailRuntimeLabel.setText(movie.getRuntimeMinutes() > 0
                ? "%d mín".formatted(movie.getRuntimeMinutes())
                : "Lengd óþekkt");
    }

    /**
     * Athugar hvort mynd sé þegar vistuð í watchlist.
     *
     * @param movie mynd sem á að athuga
     * @return true ef myndin er þegar á lista
     */
    private boolean isMovieSaved(Movie movie) {
        return watchlist.stream().anyMatch(savedMovie -> savedMovie.getId() == movie.getId());
    }

    /**
     * Hleður mynd á öruggan hátt.
     *
     * @param imageUrl slóð á mynd
     * @return Image eða null ef ekki tókst að hlaða
     */
    private Image loadImageSafely(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }

        try {
            return new Image(imageUrl, true);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Býr til chip label með gefnum texta.
     *
     * @param text texti á chip
     * @return nýtt Label
     */
    private Label createChipLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("chip-label");
        return label;
    }

    /**
     * Sýnir eða felur node og samstillir managed property.
     *
     * @param node    node sem á að uppfæra
     * @param visible sýnileikastaða
     */
    private void setVisibleManaged(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    /**
     * Sýnir villuglugga með skilaboðum.
     *
     * @param title       titill glugga
     * @param headerText  fyrirsögn
     * @param contentText innihald
     */
    private void showErrorAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}