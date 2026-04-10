package is.vinnsla;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Sér um að vista og lesa watchlist úr JSON skrá.
 */
public class WatchlistStorage {

    /** ObjectMapper fyrir JSON lestur og vistun. */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /** Slóð á JSON skrána sem geymir watchlist. */
    private final Path filePath = Paths.get("data", "watchlist.json");

    /**
     * Les watchlist úr JSON skrá.
     *
     * @return listi af vistuðum myndum
     */
    public List<Movie> loadWatchlist() {
        try {
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }

            List<StoredMovie> storedMovies = MAPPER.readValue(
                    filePath.toFile(),
                    new TypeReference<List<StoredMovie>>() { }
            );

            return convertToMovies(storedMovies);
        } catch (IOException exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Vistar watchlist í JSON skrá.
     *
     * @param movies listi af myndum sem á að vista
     */
    public void saveWatchlist(List<Movie> movies) {
        try {
            createParentDirectoryIfNeeded();

            List<StoredMovie> storedMovies = new ArrayList<>();
            for (Movie movie : movies) {
                storedMovies.add(toStoredMovie(movie));
            }

            MAPPER.writeValue(filePath.toFile(), storedMovies);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Breytir StoredMovie í Movie.
     *
     * @param stored geymt movie object
     * @return Movie hlutur
     */
    private Movie toMovie(StoredMovie stored) {
        Movie movie = new Movie(
                stored.id,
                safe(stored.title),
                safe(stored.posterUrl),
                safe(stored.description),
                stored.rating,
                stored.year
        );

        movie.setReleaseDate(safe(stored.releaseDate));
        movie.setRuntimeMinutes(stored.runtimeMinutes);
        movie.setGenres(stored.genres != null ? stored.genres : new ArrayList<>());
        movie.setActors(stored.actors != null ? stored.actors : new ArrayList<>());
        movie.setBackdropUrl(safe(stored.backdropUrl));
        movie.setYoutubeTrailerKey(safe(stored.youtubeTrailerKey));

        try {
            movie.setWatchStatus(WatchStatus.valueOf(
                    safe(stored.watchStatus, WatchStatus.VIL_HORFA.name())
            ));
        } catch (IllegalArgumentException exception) {
            movie.setWatchStatus(WatchStatus.VIL_HORFA);
        }

        return movie;
    }

    /**
     * Breytir Movie í StoredMovie sem er hentugur fyrir JSON.
     *
     * @param movie movie sem á að vista
     * @return geymd útgáfa af movie
     */
    private StoredMovie toStoredMovie(Movie movie) {
        StoredMovie stored = new StoredMovie();
        stored.id = movie.getId();
        stored.title = movie.getTitle();
        stored.posterUrl = movie.getPosterUrl();
        stored.description = movie.getDescription();
        stored.rating = movie.getRating();
        stored.year = movie.getYear();
        stored.releaseDate = movie.getReleaseDate();
        stored.runtimeMinutes = movie.getRuntimeMinutes();
        stored.genres = new ArrayList<>(movie.getGenres());
        stored.actors = new ArrayList<>(movie.getActors());
        stored.watchStatus = movie.getWatchStatus() != null
                ? movie.getWatchStatus().name()
                : WatchStatus.VIL_HORFA.name();
        stored.backdropUrl = movie.getBackdropUrl();
        stored.youtubeTrailerKey = movie.getYoutubeTrailerKey();
        return stored;
    }

    /**
     * Breytir lista af StoredMovie í lista af Movie.
     *
     * @param storedMovies listi af geymdum movie hlutum
     * @return listi af Movie hlutum
     */
    private List<Movie> convertToMovies(List<StoredMovie> storedMovies) {
        List<Movie> movies = new ArrayList<>();

        for (StoredMovie storedMovie : storedMovies) {
            movies.add(toMovie(storedMovie));
        }

        return movies;
    }

    /**
     * Býr til parent möppu ef hún er ekki til.
     *
     * @throws IOException ef ekki tekst að búa til möppu
     */
    private void createParentDirectoryIfNeeded() throws IOException {
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }
    }

    /**
     * Skilar öruggum streng.
     *
     * @param value strengur
     * @return strengur eða tómur strengur
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Skilar öruggum streng með fallback gildi.
     *
     * @param value strengur
     * @param fallback fallback gildi
     * @return value eða fallback
     */
    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * Hjálparklasi sem er notaður til að vista kvikmyndir í JSON.
     */
    private static class StoredMovie {

        /** Auðkenni myndar. */
        public int id;

        /** Titill myndar. */
        public String title;

        /** Slóð á poster. */
        public String posterUrl;

        /** Lýsing myndar. */
        public String description;

        /** Einkunn myndar. */
        public double rating;

        /** Útgáfuár myndar. */
        public int year;

        /** Útgáfudagur myndar. */
        public String releaseDate;

        /** Lengd myndar í mínútum. */
        public int runtimeMinutes;

        /** Flokkar myndar. */
        public List<String> genres;

        /** Leikarar myndar. */
        public List<String> actors;

        /** Staða myndar á lista. */
        public String watchStatus;

        /** Backdrop slóð. */
        public String backdropUrl;

        /** YouTube trailer lykill. */
        public String youtubeTrailerKey;
    }
}