import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        String movieId = request.getParameter("id");

        request.getServletContext().log("getting id: " + movieId);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String query = "SELECT m.id, " +
                    "m.title, " +
                    "m.year, " +
                    "m.director, " +
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(s.name, ':', s.id)) " +
                    "FROM (SELECT s.id, s.name, COUNT(stars_in_movies.movieId) AS count_movies " +
                    "FROM stars_in_movies " +
                    "JOIN (SELECT stars.id, stars.name " +
                    "FROM stars " +
                    "JOIN stars_in_movies ON stars.id = stars_in_movies.starId " +
                    "WHERE stars_in_movies.movieId = m.id) AS s " +
                    "ON s.id = stars_in_movies.starId " +
                    "GROUP BY s.id, s.name " +
                    "ORDER BY count_movies DESC, s.name ASC) AS s) AS stars, " +
                    "r.rating " +
                    "FROM movies m " +
                    "JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN genres_in_movies gim ON m.id = gim.movieId " +
                    "LEFT JOIN genres g ON gim.genreId = g.id " +
                    "WHERE m.id = ?;";

            // Create a prepared statement
            PreparedStatement preparedStatement = conn.prepareStatement(query);

            // Set the value for the parameter (movieId)
            preparedStatement.setString(1, movieId);

            // Perform the query
            ResultSet rs = preparedStatement.executeQuery();

            JsonArray jsonArray = new JsonArray();
            if (rs.next()) { // Move the cursor to the first row
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                int movie_year = rs.getInt("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                float movie_rating = rs.getFloat("rating");

                JsonArray genreArray = new JsonArray();
                String[] genreData = movie_genres.split(",");
                for (String genre: genreData) {
                    JsonObject genreObj = new JsonObject();
                    genreObj.addProperty("genre_name", genre);
                    genreArray.add(genreObj);
                }

                // Create an array to store star objects
                JsonArray starsArray = new JsonArray();
                // Split the star names, IDs, and hyperlinks
                String[] starData = movie_stars.split(",");

                for (String star : starData) {
                    String[] starInfo = star.trim().split(":");
                    String starName = starInfo[0];
                    String starId = starInfo[1];

                    // Create a star object with name, ID, and hyperlink
                    JsonObject starObject = new JsonObject();
                    starObject.addProperty("star_name", starName);
                    starObject.addProperty("star_id", starId);
                    starObject.addProperty("star_link", "single-star.html?id=" + starId);

                    // Add the star object to the starsArray
                    starsArray.add(starObject);
                }

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.add("movie_genres", genreArray);
                jsonObject.add("movie_stars", starsArray);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            preparedStatement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);
        } catch (Exception e) {
            e.printStackTrace();

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
        // Always remember to close db connection after usage. Here it's done by try-with-resources
    }
}
