package parser_files;

import java.util.ArrayList;
import java.util.List;

public class Movie {
    private String id = null;

    private String fid = null;

    private String title = null;
    private int year = -1;
    private String director = null;
    private List<String> genres;
    private float rating = 0;

    public Movie() {
        this.genres = new ArrayList<>();
    }

    public boolean isValid() {
        return this.fid != null && this.id != null && this.year != -1 && this.title != null && this.director != null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getFid() {
        return this.fid;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getYear() {
        return this.year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getDirector() {
        return this.director;
    }

    public void addGenre(String genre){
        genres.add(genre);
    }

    public List<String> getGenres() {
        return this.genres;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public float getRating() {
        return this.rating;
    }

    @Override
    public String toString() {
        return "parser_files.Movie ID: " + id + " Year: " + year + " Title: " + title + " Director: " + director + " Genres: " + genres;
    }

}