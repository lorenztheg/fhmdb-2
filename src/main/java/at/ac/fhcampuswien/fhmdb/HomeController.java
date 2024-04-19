package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.api.MovieAPI;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.models.SortedState;
import at.ac.fhcampuswien.fhmdb.ui.MovieCell;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static at.ac.fhcampuswien.fhmdb.api.MovieAPI.getAllMovies;

public class HomeController implements Initializable {
    @FXML
    public JFXButton searchBtn;

    @FXML
    public TextField searchField;

    @FXML
    public JFXButton resetBtn;

    @FXML
    public JFXListView movieListView;

    @FXML
    public JFXComboBox genreComboBox;

    @FXML
    public JFXButton sortBtn;
    @FXML
    public JFXComboBox releaseYearComboBox;

    @FXML JFXComboBox ratingComboBox;

    public List<Movie> allMovies;

    protected ObservableList<Movie> observableMovies = FXCollections.observableArrayList();

    protected SortedState sortedState;

    public static ArrayList<Label> titlesList = new ArrayList<Label>();
    public static ArrayList<Label> descriptionsList = new ArrayList<Label>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeState();
        initializeLayout();
    }

    public void initializeState() {
        //allMovies = Movie.initializeMovies();
        allMovies = getAllMovies();
        observableMovies.clear();
        observableMovies.addAll(allMovies); // add all movies to the observable list
        sortedState = SortedState.NONE;
    }

    public void initializeLayout() {
        movieListView.setItems(observableMovies);   // set the items of the listview to the observable list
        movieListView.setCellFactory(movieListView -> new MovieCell()); // apply custom cells to the listview

        Object[] genres = Genre.values();   // get all genres
        genreComboBox.getItems().add("No filter");  // add "no filter" to the combobox
        genreComboBox.getItems().addAll(genres);    // add all genres to the combobox
        genreComboBox.setPromptText("Filter by Genre");

        releaseYearComboBox.setPromptText("Filter by Release Year");
        Integer[] releaseYears = new Integer[78];
        for (int i = 0; i < 78; i++) {
            releaseYears[i] = 2023 - i;
        }
        //releaseYearComboBox.getItems().add("No filter");
        releaseYearComboBox.getItems().addAll(releaseYears);

        ratingComboBox.setPromptText("Filter by rating");
        Double[] rating = new Double[]{1.00, 2.00, 3.00, 4.00, 5.00, 6.00, 7.00, 8.00, 9.00, 10.00};
        ratingComboBox.getItems().addAll(rating);
    }
    // sort movies based on sortedState
    // by default sorted state is NONE
    // afterwards it switches between ascending and descending
    public void sortMovies() {
        if (sortedState == SortedState.NONE || sortedState == SortedState.DESCENDING) {
            observableMovies.sort(Comparator.comparing(Movie::getTitle));
            sortedState = SortedState.ASCENDING;
        } else if (sortedState == SortedState.ASCENDING) {
            observableMovies.sort(Comparator.comparing(Movie::getTitle).reversed());
            sortedState = SortedState.DESCENDING;
        }
    }


    public List<Movie> filterByQuery(List<Movie> movies, String query){
        if(query == null || query.isEmpty()) return movies;

        if(movies == null) {
            throw new IllegalArgumentException("movies must not be null");
        }

        return movies.stream()
                .filter(Objects::nonNull)
                .filter(movie ->
                    movie.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    movie.getDescription().toLowerCase().contains(query.toLowerCase())
                )
                .toList();
    }

    public List<Movie> filterByGenre(List<Movie> movies, Genre genre){
        if(genre == null) return movies;

        if(movies == null) {
            throw new IllegalArgumentException("movies must not be null");
        }

        return movies.stream()
                .filter(Objects::nonNull)
                .filter(movie -> movie.getGenres().contains(genre))
                .toList();
    }

    public void applyAllFilters(String searchQuery, Object genre, String releaseYear, String rating) {
        List<Movie> filteredMovies = searchQuery.isEmpty() && genre == "No filter" && releaseYear == "Filter by Release Year" && rating == "Filter by rating" ? getAllMovies(null, null, null, null)
                : getAllMovies(searchQuery.isEmpty() ? null : searchQuery,
                genre == "No filter" ? null : (Genre) genre,
                releaseYear == "Filter by Release Year" ? null : releaseYear,
                rating == "Filter by rating" ? null : rating);
        observableMovies.clear();
        observableMovies.addAll(filteredMovies);
    }

    public void searchBtnClicked(ActionEvent actionEvent) {
        String searchQuery = searchField.getText().trim().toLowerCase();
        Object genre = genreComboBox.getSelectionModel().getSelectedItem();
        String releaseYear = "";
        String rating = "";
        if (releaseYearComboBox.getSelectionModel().getSelectedItem() != null)
            releaseYear = releaseYearComboBox.getSelectionModel().getSelectedItem().toString();
        if (ratingComboBox.getSelectionModel().getSelectedItem() != null)
            rating = ratingComboBox.getSelectionModel().getSelectedItem().toString();

        applyAllFilters(searchQuery, genre, releaseYear, rating);

        if(sortedState != SortedState.NONE) {
            sortMovies();
        }
    }

    public void resetBtnClicked(ActionEvent actionEvent) {
        genreComboBox.setPromptText("Filter by Genre");
        releaseYearComboBox.getSelectionModel().clearSelection();
        searchField.clear();
        ratingComboBox.getSelectionModel().clearSelection();

    }
    public void sortBtnClicked(ActionEvent actionEvent) {
        sortMovies();
    }

    public long countMoviesFrom(List<Movie> movies, String director) {
        return movies.stream()
                .filter(movie -> movie.getDirectors().contains(director))
                .count();
    }

    public int getLongestMovieTitle(List<Movie> movies) {
        return movies.stream()
                .map(Movie::getTitle)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    public List<Movie> getMoviesBetweenYears(List<Movie> movies, int startYear, int endYear) {
        return movies.stream()
                .filter(movie -> movie.getReleaseYear() >= startYear && movie.getReleaseYear() <= endYear)
                .collect(Collectors.toList());
    }

    public String getMostPopularActor(List<Movie> movies) {
        return movies.stream()
                .flatMap(movie -> movie.getMainCast().stream())
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }
    /*public Movie getLongestMovie(List<Movie> movies) {
        return movies.stream()
                .max(Comparator.comparingInt(movie -> movie.getTitle().length()))
                .orElse(null);
    }
    public long countMoviesFromDirector(String directorName) {
        List<Movie> filteredMovies = new ArrayList<>(observableMovies);
        return countMoviesFrom(filteredMovies, directorName);
    }

    @FXML
    public void countMoviesBtnClicked(ActionEvent actionEvent) {
        String director = "Christopher Nolan"; // Or any other director's name you want to count movies for
        long moviesFromDirector = countMoviesFromDirector(director);
        // Do something with the count, like displaying it in a label
        System.out.println("Number of movies directed by " + director + ": " + moviesFromDirector);
    }

    @FXML
    public void longestTitleBtnClicked(ActionEvent actionEvent) {
        List<Movie> filteredMovies = new ArrayList<>(observableMovies);
        Movie longestMovie = getLongestMovie(filteredMovies);
        // Do something with the longest movie title, like displaying it in a label
        System.out.println("Longest movie title: " + longestMovie.getTitle());
    }
    @FXML
    public void filterByYearsBtnClicked(ActionEvent actionEvent) {
        // Dialog für das Startjahr anzeigen
        TextInputDialog startYearDialog = new TextInputDialog();
        startYearDialog.setTitle("Enter Start Year");
        startYearDialog.setHeaderText(null);
        startYearDialog.setContentText("Please enter the start year:");

        Optional<String> startYearResult = startYearDialog.showAndWait();
        int startYear = startYearResult.map(Integer::parseInt).orElse(0);

        // Dialog für das Endjahr anzeigen
        TextInputDialog endYearDialog = new TextInputDialog();
        endYearDialog.setTitle("Enter End Year");
        endYearDialog.setHeaderText(null);
        endYearDialog.setContentText("Please enter the end year:");

        Optional<String> endYearResult = endYearDialog.showAndWait();
        int endYear = endYearResult.map(Integer::parseInt).orElse(0);

        // Überprüfen, ob gültige Jahre eingegeben wurden
        if (startYear > 0 && endYear > 0 && startYear <= endYear) {
            // Filme zwischen den eingegebenen Jahren filtern
            List<Movie> filteredMovies = getMoviesBetweenYears(observableMovies, startYear, endYear);

            // Die ObservableList der Filme aktualisieren und der ListView zuweisen
            observableMovies.clear();
            observableMovies.addAll(filteredMovies);
            movieListView.setItems(observableMovies);
        } else {
            // Fehlermeldung anzeigen, wenn ungültige Eingaben vorliegen
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Invalid input. Please make sure the start year is less than or equal to the end year.");
            alert.showAndWait();
        }
    }

    @FXML
    public void mostPopularActorBtnClicked(ActionEvent actionEvent) {
        List<Movie> filteredMovies = new ArrayList<>(observableMovies);
        String mostPopularActor = getMostPopularActor(filteredMovies);
        // Do something with the most popular actor, like displaying it in a label
        System.out.println("Most popular actor: " + mostPopularActor);
    }*/


}