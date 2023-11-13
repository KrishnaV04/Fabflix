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

public class SAXParserActors extends DefaultHandler {
    private Connection connection;
    private FileWriter inconsistenciesFileWriter;
    private BufferedWriter inconsistenciesBufferedWriter;
    private Map<String, String> stageNameToId;
    private String tempVal;
    private int nextStarId;
    private Star currStar;
    private PreparedStatement starInsert;

//    private static final String STAR_INSERT_QUERY = "INSERT INTO stars (id, name, birthYear)\n" +
//            "SELECT ?, ?, ?\n" +
//            "FROM dual\n" +
//            "WHERE NOT EXISTS (\n" +
//            "    SELECT 1 FROM stars\n" +
//            "    WHERE name = ?\n" +
//            "    AND (\n" +
//            "        (birthYear = ? AND ? IS NOT NULL)\n" +
//            "        OR (birthYear IS NULL AND ? IS NULL)\n" +
//            "    )\n" +
//            ")\n";

    private static final String STAR_INSERT_QUERY = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";

    private static final String MAX_STAR_ID_QUERY = "SELECT MAX(CAST(SUBSTRING(id, 3) AS SIGNED)) FROM stars";

    private static final String XML_PATH = "../project1/src/stanford-movies/actors63.xml";

    private static final String DB_CLASS = "com.mysql.cj.jdbc.Driver";

    private static final String DB_URL = "jdbc:mysql://localhost:3306/moviedb";

    private static final String DB_USERNAME = "mytestuser";

    private static final String DB_PASSWORD = "My6$Password";

    public SAXParserActors() {
        try {
            this.setUpDatabaseConnection();
            stageNameToId = new HashMap<String, String>();
            this.createPreparedInsertionQuery();
            this.getNextIds();
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
            starInsert = this.connection.prepareStatement(STAR_INSERT_QUERY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    private void insertIntoStarsTable(parser_files.Star star) {
//        System.out.println("inserting a star: " + star.toString());
//        try {
//            if (star.isValid()) {
//                starInsert.setString(1, star.getId());
//                starInsert.setString(2, star.getName());
//                if (star.getBirthYear() == -1) {
//                    starInsert.setNull(3, java.sql.Types.INTEGER);
//                    starInsert.setNull(5, java.sql.Types.INTEGER);
//                    starInsert.setNull(6, java.sql.Types.INTEGER);
//                    starInsert.setNull(7, java.sql.Types.INTEGER);
//                } else {
//                    starInsert.setInt(3, star.getBirthYear());
//                    starInsert.setInt(5, star.getBirthYear());
//                    starInsert.setInt(6, star.getBirthYear());
//                    starInsert.setInt(7, star.getBirthYear());
//                }
//                starInsert.setString(4, star.getName());
//                starInsert.addBatch();
//                stageNameToId.put(star.getName(), star.getId());
//            } else {
//                this.addToInconsistencyFile(star);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    private void insertIntoStarsTable(Star star) {
        System.out.println("inserting a star: " + star.toString());
        try {
            if (star.isValid()) {
                starInsert.setString(1, star.getId());
                starInsert.setString(2, star.getName());
                if (star.getBirthYear() == -1) {
                    starInsert.setNull(3, java.sql.Types.INTEGER);
                } else {
                    starInsert.setInt(3, star.getBirthYear());
                }
                starInsert.addBatch();
                stageNameToId.put(star.getName(), star.getId());
            } else {
                this.addToInconsistencyFile(star);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void getNextIds() {
        System.out.println("getting actor ID!");
        PreparedStatement actorIdStatement = null;
        ResultSet actorIdResult = null;

        try {
            actorIdStatement = connection.prepareStatement(MAX_STAR_ID_QUERY);
            actorIdResult = actorIdStatement.executeQuery();

            if (actorIdResult.next()) {
                nextStarId = actorIdResult.getInt(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (actorIdResult != null) {
                    actorIdResult.close();
                }
                if (actorIdStatement != null) {
                    actorIdStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createInconsistenciesFile() {
        try {
            inconsistenciesFileWriter = new FileWriter("actors_inconsistencies.txt");
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

    public void run() {
        try {
            this.parseDocument();
            starInsert.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                starInsert.close();
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

    public Map<String, String> getStageNameToId() {
        return this.stageNameToId;
    }

    public Integer getStarId() { return this.nextStarId; }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";
        if (qName.equalsIgnoreCase("actor")) {
            this.currStarInitialization();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        tempVal = new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (qName.equalsIgnoreCase("stagename")) {
            currStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("actor")) {
            this.insertIntoStarsTable(currStar);
        } else if (qName.equalsIgnoreCase("dob")) {
            try {
                currStar.setBirthYear(Integer.parseInt(tempVal));
            }
            catch (NumberFormatException ignored) {
                System.out.println("INVALID YEAR BEING INSERTED!!! HERE IT IS BELOW:");
                System.out.println(tempVal);
            }
        }
    }

    public static void main(String[] args) {
        SAXParserActors spa = new SAXParserActors();
        spa.run();
    }
}
