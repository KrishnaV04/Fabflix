package edu.uci.ics.fabflixmobile.data.model;

import java.util.ArrayList;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String title;
    private final short year;
    private final String id;
    private final String director;
    private String stars;
    private String genres;

    public Movie(String id, String title, short year, String director, String stars, String genres) {
        this.title = title;
        this.year = year;
        this.id = id;
        this.director = director;
        this.stars = stars;
        this.genres = genres;
    }

    public String getName() {
        return title;
    }
    public short getYear() {
        return year;
    }
    public String getId() { return id; }
    public String getDirector() { return director; }
    public String getStarsString() { return stars; }
    public String getGenresString() { return genres; }
}