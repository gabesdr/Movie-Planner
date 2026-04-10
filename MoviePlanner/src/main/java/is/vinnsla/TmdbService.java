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

/**
 * Þjónustuklasi sem sér um samskipti við TMDB API.
 * Klasinn sækir leitarnðurstöður, vinsælar myndir og nánari upplýsingar
 * um einstakar myndir.
 */
public class TmdbService {

    /** Grunnslóð á TMDB API. */
    private static final String BASE_URL = "https://api.themoviedb.org/3";

    /** Grunnslóð fyrir myndir frá TMDB. */
    private static final String IMAGE_BASE = "https://image.tmdb.org/t/p/w500";

    /** API lykill fyrir TMDB. */
    private static final String API_KEY = ApiConfig.API_KEY;

    /** ObjectMapper fyrir JSON vinnslu. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Http client sem sér um beiðnir á API. */
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Leitar að kvikmyndum eftir titli.
     *
     * @param query leitarstrengur
     * @return listi af kvikmyndum sem passa við leit
     */
    public List<Movie> searchMovies(String query) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }
        if (isApiKeyMissing()) {
            return createEmptyMovieList();
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = BASE_URL + "/search/movie?api_key=" + API_KEY
                    + "&query=" + encodedQuery + "&language=en-US";
            JsonNode root = getJson(url);
            return parseMovieList(root.path("results"));
        } catch (Exception exception) {
            return createEmptyMovieList();
        }
    }

    /**
     * Sækir vinsælar myndir frá TMDB.
     *
     * @param page blaðsíðunúmer
     * @return listi af vinsælum myndum
     */
    public List<Movie> getTrendingMovies(int page) {
        if (isApiKeyMissing()) {
            return mockTrending(page);
        }

        try {
            String url = BASE_URL + "/trending/movie/week?api_key=" + API_KEY
                    + "&language=en-US&page=" + page;
            JsonNode root = getJson(url);
            return parseMovieList(root.path("results"));
        } catch (Exception exception) {
            return mockTrending(page);
        }
    }

    /**
     * Sækir ítarlegri upplýsingar um eina mynd.
     *
     * @param movie myndin sem á að auðga með frekari upplýsingum
     * @return sama mynd með viðbættum upplýsingum
     */
    public Movie enrichMovie(Movie movie) {
        if (movie == null) {
            return null;
        }
        if (isApiKeyMissing()) {
            return mockDetails(movie);
        }

        try {
            JsonNode detail = getJson(buildMovieDetailUrl(movie.getId()));
            JsonNode credits = getJson(buildMovieCreditsUrl(movie.getId()));
            JsonNode videos = getJson(buildMovieVideosUrl(movie.getId()));

            updateMovieDetails(movie, detail, credits, videos);
            return movie;
        } catch (Exception exception) {
            return mockDetails(movie);
        }
    }

    /**
     * Breytir JSON niðurstöðum í lista af Movie hlutum.
     *
     * @param results JSON fylki af niðurstöðum
     * @return listi af kvikmyndum
     */
    private List<Movie> parseMovieList(JsonNode results) {
        List<Movie> movies = new ArrayList<>();

        for (JsonNode item : results) {
            Movie movie = new Movie();
            movie.setId(item.path("id").asInt());
            movie.setTitle(item.path("title").asText("Óþekkt mynd"));
            movie.setDescription(parseOverview(item.path("overview").asText("")));
            movie.setRating(item.path("vote_average").asDouble());

            String releaseDate = item.path("release_date").asText("");
            movie.setReleaseDate(releaseDate);
            movie.setYear(parseYear(releaseDate));
            movie.setPosterUrl(buildImageUrl(item.path("poster_path").asText("")));
            movie.setBackdropUrl(buildImageUrl(item.path("backdrop_path").asText("")));

            movies.add(movie);
        }

        return movies;
    }

    /**
     * Sendir HTTP beiðni og les JSON svar.
     *
     * @param url slóð sem á að sækja
     * @return JSON niðurstaða
     * @throws IOException ef villa kemur upp við lestur
     * @throws InterruptedException ef þráður er truflaður
     */
    private JsonNode getJson(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
        return MAPPER.readTree(response.body());
    }

    /**
     * Sækir allt að þrjá flokka úr genre lista.
     *
     * @param genreArray JSON fylki af flokkum
     * @return listi af flokkum
     */
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

    /**
     * Sækir allt að fjóra leikara úr cast lista.
     *
     * @param castArray JSON fylki af leikurum
     * @return listi af leikurum
     */
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

    /**
     * Reiknar útgáfuár úr útgáfudegi.
     *
     * @param releaseDate útgáfudagur
     * @return útgáfuár eða 0 ef ekki tekst að lesa
     */
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

    /**
     * Velur besta trailer lykil úr video niðurstöðum.
     *
     * @param results JSON fylki af videos
     * @return trailer lykill eða tómur strengur
     */
    private String parseTrailerKey(JsonNode results) {
        for (JsonNode item : results) {
            if (isPreferredTrailer(item)) {
                return item.path("key").asText("");
            }
        }

        for (JsonNode item : results) {
            if (isYouTubeTrailer(item)) {
                return item.path("key").asText("");
            }
        }

        for (JsonNode item : results) {
            if (isYouTubeTeaser(item)) {
                return item.path("key").asText("");
            }
        }

        return "";
    }

    /**
     * Athugar hvort JSON item sé opinber YouTube trailer.
     *
     * @param item JSON item
     * @return true ef item er preferred trailer
     */
    private boolean isPreferredTrailer(JsonNode item) {
        return isYouTube(item)
                && "Trailer".equalsIgnoreCase(item.path("type").asText())
                && item.path("official").asBoolean(false);
    }

    /**
     * Athugar hvort JSON item sé YouTube trailer.
     *
     * @param item JSON item
     * @return true ef item er YouTube trailer
     */
    private boolean isYouTubeTrailer(JsonNode item) {
        return isYouTube(item)
                && "Trailer".equalsIgnoreCase(item.path("type").asText());
    }

    /**
     * Athugar hvort JSON item sé YouTube teaser.
     *
     * @param item JSON item
     * @return true ef item er YouTube teaser
     */
    private boolean isYouTubeTeaser(JsonNode item) {
        return isYouTube(item)
                && "Teaser".equalsIgnoreCase(item.path("type").asText());
    }

    /**
     * Athugar hvort JSON item vísi á YouTube.
     *
     * @param item JSON item
     * @return true ef site er YouTube
     */
    private boolean isYouTube(JsonNode item) {
        return "YouTube".equalsIgnoreCase(item.path("site").asText(""));
    }

    /**
     * Skilar tómu movie listi sem fallback.
     *
     * @return tómur listi
     */
    private List<Movie> createEmptyMovieList() {
        return new ArrayList<>();
    }

    /**
     * Býr til mock trending gögn þegar API er ekki tiltækt.
     *
     * @param page blaðsíðunúmer
     * @return listi af mock kvikmyndum
     */
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

    /**
     * Bætir við mock detail upplýsingum ef API gögn vantar.
     *
     * @param movie mynd sem á að fylla út
     * @return uppfærð mynd
     */
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

    /**
     * Uppfærir detail upplýsingar á mynd út frá JSON gögnum.
     *
     * @param movie   myndin sem á að uppfæra
     * @param detail  detail JSON
     * @param credits credits JSON
     * @param videos  videos JSON
     */
    private void updateMovieDetails(Movie movie, JsonNode detail,
                                    JsonNode credits, JsonNode videos) {
        movie.setRuntimeMinutes(detail.path("runtime").asInt(0));
        movie.setDescription(parseOverview(detail.path("overview").asText("")));
        movie.setReleaseDate(detail.path("release_date").asText(movie.getReleaseDate()));
        movie.setYear(parseYear(movie.getReleaseDate()));
        movie.setRating(detail.path("vote_average").asDouble(movie.getRating()));
        movie.setGenres(parseGenres(detail.path("genres")));
        movie.setActors(parseActors(credits.path("cast")));

        String backdropPath = detail.path("backdrop_path").asText("");
        if (!backdropPath.isBlank()) {
            movie.setBackdropUrl(buildImageUrl(backdropPath));
        }

        movie.setYoutubeTrailerKey(parseTrailerKey(videos.path("results")));
    }

    /**
     * Býr til detail slóð fyrir mynd.
     *
     * @param movieId auðkenni myndar
     * @return slóð á detail endpoint
     */
    private String buildMovieDetailUrl(int movieId) {
        return BASE_URL + "/movie/" + movieId
                + "?api_key=" + API_KEY + "&language=en-US";
    }

    /**
     * Býr til credits slóð fyrir mynd.
     *
     * @param movieId auðkenni myndar
     * @return slóð á credits endpoint
     */
    private String buildMovieCreditsUrl(int movieId) {
        return BASE_URL + "/movie/" + movieId
                + "/credits?api_key=" + API_KEY + "&language=en-US";
    }

    /**
     * Býr til videos slóð fyrir mynd.
     *
     * @param movieId auðkenni myndar
     * @return slóð á videos endpoint
     */
    private String buildMovieVideosUrl(int movieId) {
        return BASE_URL + "/movie/" + movieId
                + "/videos?api_key=" + API_KEY + "&language=en-US";
    }

    /**
     * Býr til fulla myndaslóð út frá path.
     *
     * @param imagePath relative path frá TMDB
     * @return full slóð eða tómur strengur
     */
    private String buildImageUrl(String imagePath) {
        return imagePath == null || imagePath.isBlank()
                ? ""
                : IMAGE_BASE + imagePath;
    }

    /**
     * Hreinsar overview texta og setur fallback ef hann vantar.
     *
     * @param overview overview texti
     * @return örugg lýsing
     */
    private String parseOverview(String overview) {
        return overview == null || overview.isBlank()
                ? "Engin lýsing tiltæk."
                : overview;
    }

    /**
     * Athugar hvort API lykil vanti.
     *
     * @return true ef lykil vantar
     */
    private boolean isApiKeyMissing() {
        return API_KEY == null || API_KEY.isBlank();
    }
}