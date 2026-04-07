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
            List<Movie> movies = new ArrayList<>();

            for (JsonNode item : root.path("results")) {
                Movie movie = new Movie();
                movie.setId(item.path("id").asInt());
                movie.setTitle(item.path("title").asText("Óþekkt mynd"));

                String overview = item.path("overview").asText("");
                if (overview == null || overview.isBlank()) {
                    overview = "Hópur könnuða fer í gegnum maskagöng í geimnum til að tryggja framtíð mannkynsins.";
                }
                movie.setDescription(overview);

                movie.setRating(item.path("vote_average").asDouble());

                String releaseDate = item.path("release_date").asText("");
                movie.setReleaseDate(releaseDate);
                movie.setYear(parseYear(releaseDate));

                String posterPath = item.path("poster_path").asText("");
                movie.setPosterUrl(posterPath.isBlank() ? "" : IMAGE_BASE + posterPath);

                movies.add(movie);
            }
            return movies;
        } catch (Exception exception) {
            return mockSearch(query);
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

            movie.setRuntimeMinutes(detail.path("runtime").asInt(0));

            String overview = detail.path("overview").asText("");
            if (overview == null || overview.isBlank()) {
                overview = "Hópur könnuða fer í gegnum maskagöng í geimnum til að tryggja framtíð mannkynsins.";
            }
            movie.setDescription(overview);

            movie.setReleaseDate(detail.path("release_date").asText(movie.getReleaseDate()));
            movie.setYear(parseYear(movie.getReleaseDate()));
            movie.setRating(detail.path("vote_average").asDouble(movie.getRating()));
            movie.setGenres(parseGenres(detail.path("genres")));
            movie.setActors(parseActors(credits.path("cast")));
            return movie;
        } catch (Exception exception) {
            return mockDetails(movie);
        }
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

    private List<Movie> mockSearch(String query) {
        List<Movie> movies = new ArrayList<>();
        if ("interstellar".contains(query.toLowerCase()) || query.toLowerCase().contains("inter")) {
            Movie movie = new Movie(
                    157336,
                    "Interstellar",
                    "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                    "Hópur könnuða fer í gegnum maskagöng í geimnum til að tryggja framtíð mannkynsins.",
                    8.4,
                    2014
            );
            movie.setReleaseDate("2014-11-07");
            movie.setRuntimeMinutes(169);
            movie.setGenres(List.of("Ævintýri", "Drama", "Vísindaskáldskapur"));
            movie.setActors(List.of("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine"));
            movies.add(movie);
        }
        return movies;
    }

    private Movie mockDetails(Movie movie) {
        if (movie.getGenres().isEmpty()) {
            movie.setGenres(List.of("Ævintýri", "Drama", "Vísindaskáldskapur"));
        }
        if (movie.getActors().isEmpty()) {
            movie.setActors(List.of("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine"));
        }
        if (movie.getRuntimeMinutes() == 0) {
            movie.setRuntimeMinutes(169);
        }
        if (movie.getReleaseDate().isBlank()) {
            movie.setReleaseDate("2014-11-07");
        }
        if (movie.getDescription().isBlank()) {
            movie.setDescription("Hópur könnuða fer í gegnum maskagöng í geimnum til að tryggja framtíð mannkynsins.");
        }
        return movie;
    }
}