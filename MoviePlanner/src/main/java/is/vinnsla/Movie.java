package is.vinnsla;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gagnaklasi sem lýsir einni kvikmynd í forritinu.
 * Klasinn geymir bæði grunnupplýsingar úr leit og ítarlegri upplýsingar
 * sem eru sóttar þegar notandi skoðar mynd nánar.
 */
public class Movie {

    /** Auðkenni myndar úr TMDB. */
    private final IntegerProperty id = new SimpleIntegerProperty();

    /** Titill myndar. */
    private final StringProperty title = new SimpleStringProperty("");

    /** Slóð á poster mynd. */
    private final StringProperty posterUrl = new SimpleStringProperty("");

    /** Lýsing á mynd. */
    private final StringProperty description = new SimpleStringProperty("");

    /** Einkunn myndar. */
    private final DoubleProperty rating = new SimpleDoubleProperty();

    /** Útgáfuár myndar. */
    private final IntegerProperty year = new SimpleIntegerProperty();

    /** Útgáfudagur myndar. */
    private final StringProperty releaseDate = new SimpleStringProperty("");

    /** Lengd myndar í mínútum. */
    private final IntegerProperty runtimeMinutes = new SimpleIntegerProperty();

    /** Listi af flokkum sem myndin tilheyrir. */
    private final ObjectProperty<List<String>> genres =
            new SimpleObjectProperty<>(new ArrayList<>());

    /** Listi af helstu leikurum myndarinnar. */
    private final ObjectProperty<List<String>> actors =
            new SimpleObjectProperty<>(new ArrayList<>());

    /** Staða myndar á persónulega listanum. */
    private final ObjectProperty<WatchStatus> watchStatus =
            new SimpleObjectProperty<>(WatchStatus.VIL_HORFA);

    /** Slóð á backdrop mynd. */
    private final StringProperty backdropUrl = new SimpleStringProperty("");

    /** YouTube trailer lykill ef hann er tiltækur. */
    private final StringProperty youtubeTrailerKey = new SimpleStringProperty("");

    /**
     * Smíðar tóma kvikmynd.
     */
    public Movie() {
    }

    /**
     * Smíðar kvikmynd með helstu grunnupplýsingum.
     *
     * @param id          auðkenni myndar
     * @param title       titill myndar
     * @param posterUrl   slóð á poster
     * @param description lýsing myndar
     * @param rating      einkunn myndar
     * @param year        útgáfuár
     */
    public Movie(int id, String title, String posterUrl,
                 String description, double rating, int year) {
        setId(id);
        setTitle(title);
        setPosterUrl(posterUrl);
        setDescription(description);
        setRating(rating);
        setYear(year);
    }

    /**
     * Skilar auðkenni myndar.
     *
     * @return auðkenni myndar
     */
    public int getId() {
        return id.get();
    }

    /**
     * Setur auðkenni myndar.
     *
     * @param value nýtt auðkenni
     */
    public void setId(int value) {
        id.set(value);
    }

    /**
     * Skilar property fyrir auðkenni myndar.
     *
     * @return id property
     */
    public IntegerProperty idProperty() {
        return id;
    }

    /**
     * Skilar titli myndar.
     *
     * @return titill myndar
     */
    public String getTitle() {
        return title.get();
    }

    /**
     * Setur titil myndar.
     *
     * @param value nýr titill
     */
    public void setTitle(String value) {
        title.set(safeString(value));
    }

    /**
     * Skilar property fyrir titil myndar.
     *
     * @return title property
     */
    public StringProperty titleProperty() {
        return title;
    }

    /**
     * Skilar poster slóð.
     *
     * @return poster slóð
     */
    public String getPosterUrl() {
        return posterUrl.get();
    }

    /**
     * Setur poster slóð.
     *
     * @param value ný slóð
     */
    public void setPosterUrl(String value) {
        posterUrl.set(safeString(value));
    }

    /**
     * Skilar property fyrir poster slóð.
     *
     * @return posterUrl property
     */
    public StringProperty posterUrlProperty() {
        return posterUrl;
    }

    /**
     * Skilar lýsingu myndar.
     *
     * @return lýsing myndar
     */
    public String getDescription() {
        return description.get();
    }

    /**
     * Setur lýsingu myndar.
     *
     * @param value ný lýsing
     */
    public void setDescription(String value) {
        description.set(safeString(value));
    }

    /**
     * Skilar property fyrir lýsingu myndar.
     *
     * @return description property
     */
    public StringProperty descriptionProperty() {
        return description;
    }

    /**
     * Skilar einkunn myndar.
     *
     * @return einkunn myndar
     */
    public double getRating() {
        return rating.get();
    }

    /**
     * Setur einkunn myndar.
     *
     * @param value ný einkunn
     */
    public void setRating(double value) {
        rating.set(value);
    }

    /**
     * Skilar property fyrir einkunn myndar.
     *
     * @return rating property
     */
    public DoubleProperty ratingProperty() {
        return rating;
    }

    /**
     * Skilar útgáfuári myndar.
     *
     * @return útgáfuár
     */
    public int getYear() {
        return year.get();
    }

    /**
     * Setur útgáfuár myndar.
     *
     * @param value nýtt útgáfuár
     */
    public void setYear(int value) {
        year.set(value);
    }

    /**
     * Skilar property fyrir útgáfuár.
     *
     * @return year property
     */
    public IntegerProperty yearProperty() {
        return year;
    }

    /**
     * Skilar útgáfudegi myndar.
     *
     * @return útgáfudagur
     */
    public String getReleaseDate() {
        return releaseDate.get();
    }

    /**
     * Setur útgáfudag myndar.
     *
     * @param value nýr útgáfudagur
     */
    public void setReleaseDate(String value) {
        releaseDate.set(safeString(value));
    }

    /**
     * Skilar property fyrir útgáfudag.
     *
     * @return releaseDate property
     */
    public StringProperty releaseDateProperty() {
        return releaseDate;
    }

    /**
     * Skilar lengd myndar í mínútum.
     *
     * @return lengd í mínútum
     */
    public int getRuntimeMinutes() {
        return runtimeMinutes.get();
    }

    /**
     * Setur lengd myndar í mínútum.
     *
     * @param value ný lengd
     */
    public void setRuntimeMinutes(int value) {
        runtimeMinutes.set(value);
    }

    /**
     * Skilar property fyrir lengd myndar.
     *
     * @return runtimeMinutes property
     */
    public IntegerProperty runtimeMinutesProperty() {
        return runtimeMinutes;
    }

    /**
     * Skilar lista af flokkum myndar.
     *
     * @return listi af flokkum
     */
    public List<String> getGenres() {
        return genres.get();
    }

    /**
     * Setur lista af flokkum myndar.
     *
     * @param value nýr listi af flokkum
     */
    public void setGenres(List<String> value) {
        genres.set(copyList(value));
    }

    /**
     * Skilar property fyrir flokka myndar.
     *
     * @return genres property
     */
    public ObjectProperty<List<String>> genresProperty() {
        return genres;
    }

    /**
     * Skilar lista af leikurum myndar.
     *
     * @return listi af leikurum
     */
    public List<String> getActors() {
        return actors.get();
    }

    /**
     * Setur lista af leikurum myndar.
     *
     * @param value nýr listi af leikurum
     */
    public void setActors(List<String> value) {
        actors.set(copyList(value));
    }

    /**
     * Skilar property fyrir leikara myndar.
     *
     * @return actors property
     */
    public ObjectProperty<List<String>> actorsProperty() {
        return actors;
    }

    /**
     * Skilar stöðu myndar á persónulega listanum.
     *
     * @return staða myndar
     */
    public WatchStatus getWatchStatus() {
        return watchStatus.get();
    }

    /**
     * Setur stöðu myndar á persónulega listanum.
     *
     * @param value ný staða
     */
    public void setWatchStatus(WatchStatus value) {
        watchStatus.set(value == null ? WatchStatus.VIL_HORFA : value);
    }

    /**
     * Skilar property fyrir stöðu myndar.
     *
     * @return watchStatus property
     */
    public ObjectProperty<WatchStatus> watchStatusProperty() {
        return watchStatus;
    }

    /**
     * Skilar backdrop slóð.
     *
     * @return backdrop slóð
     */
    public String getBackdropUrl() {
        return backdropUrl.get();
    }

    /**
     * Setur backdrop slóð.
     *
     * @param value ný slóð
     */
    public void setBackdropUrl(String value) {
        backdropUrl.set(safeString(value));
    }

    /**
     * Skilar property fyrir backdrop slóð.
     *
     * @return backdropUrl property
     */
    public StringProperty backdropUrlProperty() {
        return backdropUrl;
    }

    /**
     * Skilar YouTube trailer lyklinum.
     *
     * @return trailer lykill
     */
    public String getYoutubeTrailerKey() {
        return youtubeTrailerKey.get();
    }

    /**
     * Setur YouTube trailer lykil.
     *
     * @param value nýr trailer lykill
     */
    public void setYoutubeTrailerKey(String value) {
        youtubeTrailerKey.set(safeString(value));
    }

    /**
     * Skilar property fyrir YouTube trailer lykil.
     *
     * @return youtubeTrailerKey property
     */
    public StringProperty youtubeTrailerKeyProperty() {
        return youtubeTrailerKey;
    }

    /**
     * Athugar hvort mynd sé á gefnum watchlist.
     *
     * @param watchlist listi sem á að athuga
     * @return true ef mynd er á listanum, annars false
     */
    public boolean isOnWatchlist(List<Movie> watchlist) {
        return watchlist.stream().anyMatch(movie -> movie.getId() == getId());
    }

    /**
     * Ber saman tvær myndir eftir TMDB auðkenni.
     *
     * @param object hlutur til samanburðar
     * @return true ef auðkenni passa saman, annars false
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Movie movie)) {
            return false;
        }
        return getId() == movie.getId();
    }

    /**
     * Skilar hash kóða byggðum á auðkenni myndar.
     *
     * @return hash kóði
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * Skilar öruggum streng þar sem null verður að tómum streng.
     *
     * @param value strengur sem á að hreinsa
     * @return öruggur strengur
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Býr til afrit af lista eða tóman lista ef gildið er null.
     *
     * @param value listi sem á að afrita
     * @return nýtt afrit af lista
     */
    private List<String> copyList(List<String> value) {
        return value == null ? new ArrayList<>() : new ArrayList<>(value);
    }
}