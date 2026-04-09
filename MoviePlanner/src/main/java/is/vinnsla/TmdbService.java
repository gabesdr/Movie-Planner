package is.vinnsla;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TmdbService {
    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p/w500";
    private static final String API_KEY = ApiConfig.API_KEY;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient client = HttpClient.newHttpClient();

    public List<Movie> searchMovies(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        if (API_KEY == null || API_KEY.isBlank()) {
            return mockSearch(query);
        }

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = BASE_URL + "/search/movie?api_key=" + API_KEY + "&query=" + encoded + "&language=en-US";
            JsonNode root = getJson(url);
            return parseMovieList(root.path("results"));
        } catch (Exception exception) {
            return mockSearch(query);
        }
    }

    public List<Movie> getTrendingMovies(int page) {
        if (API_KEY == null || API_KEY.isBlank()) {
            return mockTrending(page);
        }

        try {
            String url = BASE_URL + "/trending/movie/week?api_key=" + API_KEY + "&language=en-US&page=" + page;
            JsonNode root = getJson(url);
            return parseMovieList(root.path("results"));
        } catch (Exception exception) {
            return mockTrending(page);
        }
    }

    public Movie enrichMovie(Movie movie) {
        if (movie == null) {
            return null;
        }
        if (API_KEY == null || API_KEY.isBlank()) {
            return mockDetails(movie);
        }

        try {
            JsonNode detail = getJson(BASE_URL + "/movie/" + movie.getId() + "?api_key=" + API_KEY + "&language=en-US");
            JsonNode credits = getJson(BASE_URL + "/movie/" + movie.getId() + "/credits?api_key=" + API_KEY + "&language=en-US");
            JsonNode videos = getJson(BASE_URL + "/movie/" + movie.getId() + "/videos?api_key=" + API_KEY + "&language=en-US");

            movie.setRuntimeMinutes(detail.path("runtime").asInt(0));

            String overview = detail.path("overview").asText("");
            if (overview == null || overview.isBlank()) {
                overview = movie.getDescription().isBlank() ? "Engin lýsing tiltæk." : movie.getDescription();
            }
            movie.setDescription(overview);

            movie.setReleaseDate(detail.path("release_date").asText(movie.getReleaseDate()));
            movie.setYear(parseYear(movie.getReleaseDate()));
            movie.setRating(detail.path("vote_average").asDouble(movie.getRating()));
            movie.setGenres(parseGenres(detail.path("genres")));
            movie.setActors(parseActors(credits.path("cast")));

            String backdropPath = detail.path("backdrop_path").asText("");
            movie.setBackdropUrl(backdropPath.isBlank() ? movie.getBackdropUrl() : IMAGE_BASE + backdropPath);

            movie.setYoutubeTrailerKey(parseTrailerKey(videos.path("results")));
            return movie;
        } catch (Exception exception) {
            return mockDetails(movie);
        }
    }

    private List<Movie> parseMovieList(JsonNode results) {
        List<Movie> movies = new ArrayList<>();

        for (JsonNode item : results) {
            Movie movie = new Movie();
            movie.setId(item.path("id").asInt());
            movie.setTitle(item.path("title").asText("Óþekkt mynd"));

            String overview = item.path("overview").asText("");
            if (overview == null || overview.isBlank()) {
                overview = "Engin lýsing tiltæk.";
            }
            movie.setDescription(overview);

            movie.setRating(item.path("vote_average").asDouble());

            String releaseDate = item.path("release_date").asText("");
            movie.setReleaseDate(releaseDate);
            movie.setYear(parseYear(releaseDate));

            String posterPath = item.path("poster_path").asText("");
            movie.setPosterUrl(posterPath.isBlank() ? "" : IMAGE_BASE + posterPath);

            String backdropPath = item.path("backdrop_path").asText("");
            movie.setBackdropUrl(backdropPath.isBlank() ? "" : IMAGE_BASE + backdropPath);

            movies.add(movie);
        }

        return movies;
    }

    private JsonNode getJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return MAPPER.readTree(response.body());
    }

    private List<String> parseGenres(JsonNode genreArray) {
        List<String> genres = new ArrayList<>();
        for (JsonNode item : genreArray) {
            genres.add(item.path("name").asText());
            if (genres.size() == 3) {
                break;
            }
        }
        return genres;
    }

    private List<String> parseActors(JsonNode castArray) {
        List<String> actors = new ArrayList<>();
        for (JsonNode item : castArray) {
            actors.add(item.path("name").asText());
            if (actors.size() == 4) {
                break;
            }
        }
        return actors;
    }

    private int parseYear(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4) {
            return 0;
        }
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String parseTrailerKey(JsonNode results) {
        for (JsonNode item : results) {
            if (isPreferredTrailer(item)) {
                return item.path("key").asText("");
            }
        }

        for (JsonNode item : results) {
            if (isYouTube(item) && "Trailer".equalsIgnoreCase(item.path("type").asText())) {
                return item.path("key").asText("");
            }
        }

        for (JsonNode item : results) {
            if (isYouTube(item) && "Teaser".equalsIgnoreCase(item.path("type").asText())) {
                return item.path("key").asText("");
            }
        }

        return "";
    }

    private boolean isPreferredTrailer(JsonNode item) {
        return isYouTube(item)
                && "Trailer".equalsIgnoreCase(item.path("type").asText())
                && item.path("official").asBoolean(false);
    }

    private boolean isYouTube(JsonNode item) {
        return "YouTube".equalsIgnoreCase(item.path("site").asText(""));
    }

    private List<Movie> mockSearch(String query) {
        return new ArrayList<>();
    }

    private List<Movie> mockTrending(int page) {
        List<Movie> movies = new ArrayList<>();

        if (page > 3) {
            return movies;
        }

        int start = (page - 1) * 20 + 1;
        for (int i = 0; i < 20; i++) {
            int number = start + i;
            Movie movie = new Movie(
                    100000 + number,
                    "Trending mynd " + number,
                    "",
                    "Engin lýsing tiltæk.",
                    7.0 + ((i % 5) * 0.3),
                    2024
            );
            movie.setReleaseDate("2024-01-01");
            movies.add(movie);
        }

        return movies;
    }

    private Movie mockDetails(Movie movie) {
        if (movie.getGenres().isEmpty()) {
            movie.setGenres(List.of("Drama", "Adventure", "Science Fiction"));
        }
        if (movie.getActors().isEmpty()) {
            movie.setActors(List.of("Leikari 1", "Leikari 2", "Leikari 3", "Leikari 4"));
        }
        if (movie.getRuntimeMinutes() == 0) {
            movie.setRuntimeMinutes(120);
        }
        if (movie.getReleaseDate().isBlank()) {
            movie.setReleaseDate("2024-01-01");
        }
        if (movie.getDescription().isBlank()) {
            movie.setDescription("Engin lýsing tiltæk.");
        }
        return movie;
    }
}