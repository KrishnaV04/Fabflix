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
import java.util.Map;
import java.util.Objects;

public class SAXParserCasts extends DefaultHandler {
    private Connection connection;
    private FileWriter inconsistenciesFileWriter;
    private BufferedWriter inconsistenciesBufferedWriter;
    private Map<String, String> moviesFidToId;
    private Map<String, String> stageNameToId;
    private String tempVal;
    private int nextStarId;
    private Star currStar;
    private CastMember currCastMember;
    private PreparedStatement starsInMoviesInsert;
    private static final String STARS_IN_MOVIES_INSERT_QUERY =
            "INSERT INTO stars_in_movies (starId, movieId) " +
                    "SELECT ?, ? " +
                    "FROM dual " +
                    "WHERE NOT EXISTS (" +
                    "    SELECT 1 FROM stars_in_movies " +
                    "    WHERE starId = ? AND movieId = ?" +
                    ")";
    private PreparedStatement starsInsert;
    private static final String STAR_INSERT_QUERY = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
    private static final String UPDATE_NUM_MOVIES_COUNT = "{call UpdateNumMoviesCount(?)}";
    private static final String XML_PATH = "../project1/src/stanford-movies/casts124.xml";
    private static final String DB_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";
    private static final String DB_USERNAME = "mytestuser";
    private static final String DB_PASSWORD = "My6$Password";
    private static final int BATCH_EXECUTION_SIZE = 1000;
    private int STARS_IN_MOVIES_BATCH_SIZE = 0;

    public SAXParserCasts() {
        try {
            this.setUpDatabaseConnection();
            this.createPreparedInsertionQuery();
            this.createInconsistenciesFile();
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

    private void createPreparedInsertionQuery() {
        try {
            this.starsInsert = this.connection.prepareStatement(STAR_INSERT_QUERY);
            this.starsInMoviesInsert = this.connection.prepareStatement(STARS_IN_MOVIES_INSERT_QUERY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertIntoStarsInMoviesTable(CastMember castMember) {
//        System.out.println("ACTUALLY inserting a castMember: " + castMember.toString());
        try {
            starsInMoviesInsert.setString(1, castMember.getStarId());
            starsInMoviesInsert.setString(2, castMember.getMovieId());
            starsInMoviesInsert.setString(3, castMember.getStarId());
            starsInMoviesInsert.setString(4, castMember.getMovieId());
            starsInMoviesInsert.addBatch();
            STARS_IN_MOVIES_BATCH_SIZE += 1;
            if (STARS_IN_MOVIES_BATCH_SIZE % BATCH_EXECUTION_SIZE == 0) {
                starsInsert.executeBatch();
                starsInMoviesInsert.executeBatch();
                STARS_IN_MOVIES_BATCH_SIZE = 0;
            }
            CallableStatement cstmt = connection.prepareCall(UPDATE_NUM_MOVIES_COUNT);
            cstmt.setString(1, castMember.getStarId());
            cstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insertIntoStarsInMoviesTableIfNeeded(CastMember castMember) {
//        System.out.println("validating a castMember: " + castMember.toString());
        if (castMember.isValid() && stageNameToId.containsKey(castMember.getStarName()) && moviesFidToId.containsKey(castMember.getFid())) {
            castMember.setStarId(stageNameToId.get(castMember.getStarName()));
            castMember.setMovieId(moviesFidToId.get(castMember.getFid()));
            insertIntoStarsInMoviesTable(castMember);
        } else {
            this.addToInconsistencyFile(castMember);
        }
    }

    private void insertIntoStarsTable(Star star) {
//        System.out.println("inserting a star: " + star.toString());
        try {
            if (star.isValid() && !stageNameToId.containsKey(star.getName()) && !Objects.equals(star.getName(), "sa")) {
                this.starsInsert.setString(1, star.getId());
                this.starsInsert.setString(2, star.getName());
                if (star.getBirthYear() == -1) {
                    this.starsInsert.setNull(3, java.sql.Types.INTEGER);
                } else {
                    this.starsInsert.setInt(3, star.getBirthYear());
                }
                this.starsInsert.addBatch();
//                STARS_BATCH_SIZE += 1;
//                if (STARS_BATCH_SIZE % BATCH_EXECUTION_SIZE == 0) {
//                    starsInsert.executeBatch();
//                    STARS_BATCH_SIZE = 0;
//                }
                stageNameToId.put(star.getName(), star.getId());
            } else {
                this.addToInconsistencyFile(star);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createInconsistenciesFile() {
        try {
            inconsistenciesFileWriter = new FileWriter("casts_inconsistencies.txt");
            inconsistenciesBufferedWriter = new BufferedWriter(inconsistenciesFileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToInconsistencyFile(Star star) {
        try {
            inconsistenciesBufferedWriter.write(star.toString());
            inconsistenciesBufferedWriter.newLine();
            inconsistenciesBufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToInconsistencyFile(CastMember castMember) {
        try {
            inconsistenciesBufferedWriter.write(castMember.toString());
            inconsistenciesBufferedWriter.newLine();
            inconsistenciesBufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            System.out.println("Cast Parsing Starting");
            this.parseMainAndActors();
            this.parseDocument();
            starsInsert.executeBatch();
            System.out.println("Cast New Stars All Added");
            starsInMoviesInsert.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                starsInsert.close();
                starsInMoviesInsert.close();
                inconsistenciesBufferedWriter.close();
                inconsistenciesFileWriter.close();
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseMainAndActors() {
        SAXParserActors spa = new SAXParserActors();
        SAXParserMovies spm = new SAXParserMovies();
        spa.run();
        System.out.println("Actors Parsing Finished");
        spm.run();
        System.out.println("Movies Parsing Finished");
        stageNameToId = spa.getStageNameToId();
        moviesFidToId = spm.getMoviesFidToId();
        nextStarId = spa.getStarId();
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

    private void currStarInitialization() {
        currStar = new Star();
        String newStarId;
        if (nextStarId <= 999999) {
            newStarId = "nm0" + nextStarId;
        } else {
            newStarId = "nm" + nextStarId;
        }
        currStar.setId(newStarId);
        nextStarId += 1;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        if (qName.equalsIgnoreCase("a")) {
            this.currStarInitialization();
        } else if (qName.equalsIgnoreCase("m")) {
            currCastMember = new CastMember();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("a")) {
            currStar.setName(tempVal);
            currCastMember.setStarName(tempVal);
            this.insertIntoStarsTable(this.currStar);
            this.insertIntoStarsInMoviesTableIfNeeded(currCastMember);
        } else if (qName.equalsIgnoreCase("f")) {
            currCastMember.setFid(tempVal);
        }
    }

    public static void main(String[] args) {
        SAXParserCasts spc = new SAXParserCasts();
        spc.run();
        System.out.println("Cast Parsing Finished");
    }
}
