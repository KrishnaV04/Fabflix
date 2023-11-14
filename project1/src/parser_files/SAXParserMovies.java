package parser_files;

import java.io.IOException;
import java.sql.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;

public class SAXParserMovies extends DefaultHandler {
    private Connection connection;
    private FileWriter inconsistenciesFileWriter;
    private BufferedWriter inconsistenciesBufferedWriter;
    private Map<String, String> genreCodeToNameMap;
    private Map<String, String> moviesFidToId;
    private String tempVal;
    private Movie currMovie;
    private String currDirector;
    private int nextMovieId;
    private PreparedStatement movieInsert;
    private PreparedStatement genreInsert;
    private PreparedStatement ratingsInsert;
    private PreparedStatement genresInMoviesInsert;
//    private static final String MOVIE_INSERT_QUERY = "INSERT INTO movies (id, title, year, director) " +
//            "SELECT ?, ?, ?, ? " +
//            "FROM dual " +
//            "WHERE NOT EXISTS (" +
//            "    SELECT 1 FROM movies " +
//            "    WHERE title = ? AND year = ? AND director = ?" +
//            ")";

    private static final String MOVIE_INSERT_QUERY = "INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)";

    private static final String RATINGS_INSERT_QUERY = "INSERT INTO ratings (movieId, rating, numVotes) VALUES (?, ?, ?)";

    private static final String GENRE_INSERT_QUERY = "INSERT INTO genres (name) " +
            "SELECT ? AS name " +
            "FROM dual " +
            "WHERE NOT EXISTS (" +
            "    SELECT 1 FROM genres " +
            "    WHERE name = ?" +
            ")";

    private static final String MAX_MOVIE_ID_QUERY = "SELECT MAX(CAST(SUBSTRING(id, 3) AS SIGNED)) FROM movies";

    private static final String GENRES_IN_MOVIES_INSERT_QUERY = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES ((SELECT id FROM genres WHERE name = ?), ?);";

    private static final String XML_PATH = "../project1/src/stanford-movies/mains243.xml";

    private static final String DB_CLASS = "com.mysql.cj.jdbc.Driver";

    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";

    private static final String DB_USERNAME = "mytestuser";

    private static final String DB_PASSWORD = "My6$Password";
    private static final int BATCH_EXECUTION_SIZE = 250;
    private int MOVIES_BATCH_SIZE = 0;
    private int RATINGS_BATCH_SIZE = 0;

    public SAXParserMovies() {
        try {
            this.setUpDatabaseConnection();
            this.createMoviesAndGenresMaps();
            this.createPreparedInsertionQueries();
            this.getNextIds();
            this.createInconsistenciesFile();
            this.populateGenreCodeToNameMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpDatabaseConnection() {
        try {
            Class.forName(DB_CLASS);
            this.connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createMoviesAndGenresMaps() {
        genreCodeToNameMap = new HashMap<String, String>();
        moviesFidToId = new HashMap<String, String>();
    }

    private void createPreparedInsertionQueries() {
        try {
            movieInsert = this.connection.prepareStatement(MOVIE_INSERT_QUERY);
            genreInsert = this.connection.prepareStatement(GENRE_INSERT_QUERY);
            ratingsInsert = this.connection.prepareStatement(RATINGS_INSERT_QUERY);
            genresInMoviesInsert = this.connection.prepareStatement(GENRES_IN_MOVIES_INSERT_QUERY);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createInconsistenciesFile() {
        try {
            inconsistenciesFileWriter = new FileWriter("movies_inconsistencies.txt");
            inconsistenciesBufferedWriter = new BufferedWriter(inconsistenciesFileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void populateGenreCodeToNameMap() {
        genreCodeToNameMap.put("susp", "Thriller");
        genreCodeToNameMap.put("cnr", "Cops And Robbers");
        genreCodeToNameMap.put("dram", "Drama");
        genreCodeToNameMap.put("dram>", "Drama");
        genreCodeToNameMap.put("dramd", "Drama");
        genreCodeToNameMap.put("dramn", "Drama");
        genreCodeToNameMap.put("west", "Western");
        genreCodeToNameMap.put("myst", "Mystery");
        genreCodeToNameMap.put("s.f.", "Sci-Fi");
        genreCodeToNameMap.put("scfi", "Sci-Fi");
        genreCodeToNameMap.put("advt", "Adventure");
        genreCodeToNameMap.put("adct", "Adventure");
        genreCodeToNameMap.put("horr", "Horror");
        genreCodeToNameMap.put("romt", "Romance");
        genreCodeToNameMap.put("comd", "Comedy");
        genreCodeToNameMap.put("musc", "Musical");
        genreCodeToNameMap.put("docu", "Documentary");
        genreCodeToNameMap.put("porn", "Adult");
        genreCodeToNameMap.put("porb", "Adult");
        genreCodeToNameMap.put("noir", "Black");
        genreCodeToNameMap.put("bioP", "Biography");
        genreCodeToNameMap.put("tv", "TV Show");
        genreCodeToNameMap.put("tvmini", "TV Show");
        genreCodeToNameMap.put("camp", "Camping");
        genreCodeToNameMap.put("actn", "Violence");
        genreCodeToNameMap.put("disa", "Disaster");
        genreCodeToNameMap.put("epic", "Epic");
        genreCodeToNameMap.put("cart", "Cartoon");
        genreCodeToNameMap.put("faml", "Family");
        genreCodeToNameMap.put("surl", "Sureal");
        genreCodeToNameMap.put("avga", "Avant Garde");
        genreCodeToNameMap.put("hist", "History");
        genreCodeToNameMap.put("kinky", "Adult");
        genreCodeToNameMap.put("adctx", "Action");
        genreCodeToNameMap.put("ctxx", "Uncategorized");
        genreCodeToNameMap.put("fant", "Fantasy");
    }

    public void run() {
        try {
            this.parseDocument();
            movieInsert.executeBatch();
            genreInsert.executeBatch();
            ratingsInsert.executeBatch();
            genresInMoviesInsert.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                movieInsert.close();
                genreInsert.close();
                ratingsInsert.close();
                connection.close();
                inconsistenciesBufferedWriter.close();
                inconsistenciesFileWriter.close();
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseDocument() {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            sp.parse(XML_PATH, this);
        } catch (SAXException | ParserConfigurationException | IOException se) {
            se.printStackTrace();
        }
    }

    private void getNextIds() {
        System.out.println("getting movie ID!");
        PreparedStatement movieIdStatement = null;
        ResultSet movieIdResult = null;

        try {
            movieIdStatement = connection.prepareStatement(MAX_MOVIE_ID_QUERY);
            movieIdResult = movieIdStatement.executeQuery();

            if (movieIdResult.next()) {
                nextMovieId = movieIdResult.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (movieIdResult != null) {
                    movieIdResult.close();
                }
                if (movieIdStatement != null) {
                    movieIdStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertIntoRatingsTable(Movie movie) {
        try {
            ratingsInsert.setString(1, movie.getId());
            ratingsInsert.setFloat(2, movie.getRating());
            ratingsInsert.setInt(3, 0);
            ratingsInsert.addBatch();
            RATINGS_BATCH_SIZE += 1;
            if (RATINGS_BATCH_SIZE % BATCH_EXECUTION_SIZE == 0) {
                ratingsInsert.executeBatch();
                RATINGS_BATCH_SIZE = 0;
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void insertIntoMoviesTable(Movie movie) {
        System.out.println("inserting a movie: " + movie.toString());
        try {
            if (movie.isValid()) {
                if (movie.getGenres().isEmpty()) {
                    movie.addGenre("ctxx");
                }
                for (String genre : movie.getGenres()) {
                    if (genreCodeToNameMap.containsKey(genre.toLowerCase())) {
                        genre = genreCodeToNameMap.get(genre.toLowerCase());
                        this.insertIntoGenresTable(genre);
                        this.insertIntoGenresInMoviesTable(genre, movie.getId());
                    }
                }
                movieInsert.setString(1, movie.getId());
                movieInsert.setString(2, movie.getTitle());
                movieInsert.setInt(3, movie.getYear());
                movieInsert.setString(4, movie.getDirector());
                movieInsert.addBatch();
                MOVIES_BATCH_SIZE += 1;
                if (MOVIES_BATCH_SIZE % BATCH_EXECUTION_SIZE == 0) {
                    movieInsert.executeBatch();
                    MOVIES_BATCH_SIZE = 0;
                }
                moviesFidToId.put(movie.getFid(), movie.getId());
                this.insertIntoRatingsTable(movie);
            } else {
                this.addToInconsistencyFile(movie);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void addToInconsistencyFile(Movie movie) {
        try {
            inconsistenciesBufferedWriter.write(movie.toString());
            inconsistenciesBufferedWriter.newLine();
            inconsistenciesBufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertIntoGenresTable(String genreName) {
        System.out.println("inserting genre:" + genreName);
        try {
            genreInsert.setString(1, genreName);
            genreInsert.setString(2, genreName);
            genreInsert.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertIntoGenresInMoviesTable(String genreName, String movieId) {
        try {
            genresInMoviesInsert.setString(1, genreName);
            genresInMoviesInsert.setString(2, movieId);
            genresInMoviesInsert.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void currMovieInitialization() {
        currMovie = new Movie();
        String newMovieId;
        if (nextMovieId <= 999999) {
            newMovieId = "tt0" + nextMovieId;
        } else {
            newMovieId = "tt" + nextMovieId;
        }
        currMovie.setId(newMovieId);
        nextMovieId += 1;
        currMovie.setDirector(currDirector);
    }

    public Map<String, String> getMoviesFidToId() {
        return moviesFidToId;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            this.currMovieInitialization();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("dirname")) {
            currDirector = tempVal;
        } else if (qName.equalsIgnoreCase("film")) {
            this.insertIntoMoviesTable(this.currMovie);
        } else if (qName.equalsIgnoreCase("fid")){
            currMovie.setFid(tempVal);
        } else if (qName.equalsIgnoreCase("t")) {
            currMovie.setTitle(tempVal);
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                currMovie.setYear(Integer.parseInt(tempVal));
            }
            catch (NumberFormatException ignored) {
                System.out.println("INVALID YEAR BEING INSERTED!!! HERE IT IS BELOW:");
                System.out.println(tempVal);
            }
        } else if (qName.equalsIgnoreCase("cat")) {
            currMovie.addGenre(tempVal);
        }
    }

    public static void main(String[] args) {
        SAXParserMovies spm = new SAXParserMovies();
        spm.run();
    }
}
