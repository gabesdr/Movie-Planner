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

public class WatchlistStorage {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Path filePath = Paths.get("data", "watchlist.json");

    public List<Movie> loadWatchlist() {
        try {
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }

            List<StoredMovie> storedMovies = MAPPER.readValue(
                    filePath.toFile(),
                    new TypeReference<List<StoredMovie>>() {}
            );

            List<Movie> movies = new ArrayList<>();
            for (StoredMovie storedMovie : storedMovies) {
                movies.add(toMovie(storedMovie));
            }
            return movies;
        } catch (IOException exception) {
            exception.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void saveWatchlist(List<Movie> movies) {
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }

            List<StoredMovie> storedMovies = new ArrayList<>();
            for (Movie movie : movies) {
                storedMovies.add(toStoredMovie(movie));
            }

            MAPPER.writeValue(filePath.toFile(), storedMovies);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

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
            movie.setWatchStatus(WatchStatus.valueOf(safe(stored.watchStatus, WatchStatus.VIL_HORFA.name())));
        } catch (IllegalArgumentException exception) {
            movie.setWatchStatus(WatchStatus.VIL_HORFA);
        }

        return movie;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static class StoredMovie {
        public int id;
        public String title;
        public String posterUrl;
        public String description;
        public double rating;
        public int year;
        public String releaseDate;
        public int runtimeMinutes;
        public List<String> genres;
        public List<String> actors;
        public String watchStatus;
        public String backdropUrl;
        public String youtubeTrailerKey;
    }
}