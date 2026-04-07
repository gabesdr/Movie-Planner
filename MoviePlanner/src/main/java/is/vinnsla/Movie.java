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


public class Movie {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty posterUrl = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final DoubleProperty rating = new SimpleDoubleProperty();
    private final IntegerProperty year = new SimpleIntegerProperty();
    private final StringProperty releaseDate = new SimpleStringProperty("");
    private final IntegerProperty runtimeMinutes = new SimpleIntegerProperty();
    private final ObjectProperty<List<String>> genres = new SimpleObjectProperty<>(new ArrayList<>());
    private final ObjectProperty<List<String>> actors = new SimpleObjectProperty<>(new ArrayList<>());
    private final ObjectProperty<WatchStatus> watchStatus = new SimpleObjectProperty<>(WatchStatus.VIL_HORFA);

    public Movie() {
    }

    public Movie(int id, String title, String posterUrl, String description, double rating, int year) {
        setId(id);
        setTitle(title);
        setPosterUrl(posterUrl);
        setDescription(description);
        setRating(rating);
        setYear(year);
    }

    public int getId() {
        return id.get();
    }

    public void setId(int value) {
        id.set(value);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public String getTitle() {
        return title.get();
    }

    public void setTitle(String value) {
        title.set(value);
    }

    public StringProperty titleProperty() {
        return title;
    }

    public String getPosterUrl() {
        return posterUrl.get();
    }

    public void setPosterUrl(String value) {
        posterUrl.set(value == null ? "" : value);
    }

    public StringProperty posterUrlProperty() {
        return posterUrl;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String value) {
        description.set(value == null ? "" : value);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public double getRating() {
        return rating.get();
    }

    public void setRating(double value) {
        rating.set(value);
    }

    public DoubleProperty ratingProperty() {
        return rating;
    }

    public int getYear() {
        return year.get();
    }

    public void setYear(int value) {
        year.set(value);
    }

    public IntegerProperty yearProperty() {
        return year;
    }

    public String getReleaseDate() {
        return releaseDate.get();
    }

    public void setReleaseDate(String value) {
        releaseDate.set(value == null ? "" : value);
    }

    public StringProperty releaseDateProperty() {
        return releaseDate;
    }

    public int getRuntimeMinutes() {
        return runtimeMinutes.get();
    }

    public void setRuntimeMinutes(int value) {
        runtimeMinutes.set(value);
    }

    public IntegerProperty runtimeMinutesProperty() {
        return runtimeMinutes;
    }

    public List<String> getGenres() {
        return genres.get();
    }

    public void setGenres(List<String> value) {
        genres.set(value == null ? new ArrayList<>() : new ArrayList<>(value));
    }

    public ObjectProperty<List<String>> genresProperty() {
        return genres;
    }

    public List<String> getActors() {
        return actors.get();
    }

    public void setActors(List<String> value) {
        actors.set(value == null ? new ArrayList<>() : new ArrayList<>(value));
    }

    public ObjectProperty<List<String>> actorsProperty() {
        return actors;
    }

    public WatchStatus getWatchStatus() {
        return watchStatus.get();
    }

    public void setWatchStatus(WatchStatus value) {
        watchStatus.set(value == null ? WatchStatus.VIL_HORFA : value);
    }

    public ObjectProperty<WatchStatus> watchStatusProperty() {
        return watchStatus;
    }

    public boolean isOnWatchlist(List<Movie> watchlist) {
        return watchlist.stream().anyMatch(m -> m.getId() == getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Movie movie)) {
            return false;
        }
        return getId() == movie.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}