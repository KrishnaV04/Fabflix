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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;


// Declaring a WebServlet called MoviesServlet, which maps to url "/api/movies"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
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

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT m.id, m.title, m.year, m.director,\n" +
                    "       (SELECT GROUP_CONCAT(DISTINCT g.name)\n" +
                    "     FROM (\n" +
                    "         SELECT DISTINCT g.name\n" +
                    "         FROM genres_in_movies gim\n" +
                    "         LEFT JOIN genres g ON gim.genreId = g.id\n" +
                    "         WHERE gim.movieId = m.id\n" +
                    "         LIMIT 3\n" +
                    "     ) AS g LIMIT 3) AS genres,\n" +
                    "       (SELECT GROUP_CONCAT(DISTINCT CONCAT(s.name, ':', s.id))\n" +
                    "     FROM (\n" +
                    "         SELECT DISTINCT s.name, s.id\n" +
                    "         FROM stars_in_movies sim\n" +
                    "         LEFT JOIN stars s ON sim.starId = s.id\n" +
                    "         WHERE sim.movieId = m.id\n" +
                    "         LIMIT 3\n" +
                    "     ) AS s LIMIT 3) AS stars,\n" +
                    "       r.rating\n" +
                    "FROM movies m\n" +
                    "JOIN ratings r ON m.id = r.movieId\n" +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating\n" +
                    "ORDER BY r.rating DESC\n" +
                    "LIMIT 20;\n";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                int movie_year = rs.getInt("year");
                String movie_director = rs.getString("director");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                float movie_rating = rs.getFloat("rating");
                // Create an array to store star objects
                JsonArray starsArray = new JsonArray();

                // Split the star names and IDs
                String[] starData = movie_stars.split(",");

                for (String star : starData) {
                    String[] starInfo = star.trim().split(":");

                    // Check if starInfo has at least two elements before accessing the second element
                    String starName = starInfo[0];

                    String starId = starInfo[1];

                    // Create a star object with name and hyperlink
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
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.add("movie_stars", starsArray); // Use starsArray to include star names, IDs, and hyperlinks
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

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
