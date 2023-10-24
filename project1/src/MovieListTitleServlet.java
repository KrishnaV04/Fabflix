import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "MovieListTitleServlet", urlPatterns = "/movieListTitle")
public class MovieListTitleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        String titleChar = request.getParameter("browseTitle");

        try (Connection conn = dataSource.getConnection()) {
            String query;
            if ("*".equals(titleChar)) {
                query = "SELECT m.id, m.title, m.year, m.director," +
                        "(SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) FROM ( SELECT DISTINCT g.name FROM genres_in_movies gim RIGHT JOIN genres g ON gim.genreId = g.id WHERE gim.movieId = m.id LIMIT 3 ) AS g)AS genres, " +
                        "(SELECT GROUP_CONCAT(CONCAT(s.name, ':', s.id) ) FROM (SELECT s.id, s.name, COUNT(sim.movieId) AS stars_played \n" +
                        "           FROM stars_in_movies sim\n" +
                        "           JOIN stars s ON sim.starId = s.id \n" +
                        "           WHERE sim.movieId = m.id \n" +
                        "           GROUP BY s.id, s.name \n" +
                        "           ORDER BY stars_played DESC, s.name ASC\n" +
                        "           LIMIT 3) AS s) AS stars, " +
                        "m.rating FROM (SELECT m.id, m.title, m.year, m.director, r.rating FROM ratings r JOIN movies m ON m.id = r.movieId ORDER BY r.rating DESC) AS m " +
                        "WHERE m.title REGEXP '^[^A-Za-z0-9]' " +
                        "GROUP BY m.id, m.title, m.year, m.director, m.rating " +
                        "ORDER BY m.rating DESC;";
//                query = "SELECT title FROM movies WHERE title REGEXP '^[^A-Za-z0-9]';";
            } else {
                query = "SELECT m.id, m.title, m.year, m.director," +
                        "(SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) FROM ( SELECT DISTINCT g.name FROM genres_in_movies gim RIGHT JOIN genres g ON gim.genreId = g.id WHERE gim.movieId = m.id LIMIT 3 ) AS g)AS genres, " +
                        "(SELECT GROUP_CONCAT(CONCAT(s.name, ':', s.id) ) FROM (SELECT s.id, s.name, COUNT(sim.movieId) AS stars_played \n" +
                        "           FROM stars_in_movies sim\n" +
                        "           JOIN stars s ON sim.starId = s.id \n" +
                        "           WHERE sim.movieId = m.id \n" +
                        "           GROUP BY s.id, s.name \n" +
                        "           ORDER BY stars_played DESC, s.name ASC\n" +
                        "           LIMIT 3) AS s) AS stars, " +
                        "m.rating FROM (SELECT m.id, m.title, m.year, m.director, r.rating FROM ratings r JOIN movies m ON m.id = r.movieId ORDER BY r.rating DESC) AS m " +
                        "WHERE LOWER(m.title) LIKE ?" +
                        "GROUP BY m.id, m.title, m.year, m.director, m.rating " +
                        "ORDER BY m.rating DESC;";
//                query = "SELECT title FROM movies WHERE LOWER(title) LIKE ?";
            }

            PreparedStatement statement = conn.prepareStatement(query);
            if (!"*".equals(titleChar)) {
                statement.setString(1, titleChar.toLowerCase() + "%");
            }

            ResultSet rs = statement.executeQuery();

            JsonArray movieList = new JsonArray();

            while (rs.next()) {
                JsonObject movie = new JsonObject();
                movie.addProperty("movie_id", rs.getString("id"));
                movie.addProperty("movie_title", rs.getString("title"));
                movie.addProperty("movie_year", rs.getInt("year"));
                movie.addProperty("movie_director", rs.getString("director"));
                movie.addProperty("movie_genres", rs.getString("genres"));
                movie.addProperty("movie_stars", rs.getString("stars"));
                movie.addProperty("movie_rating", rs.getDouble("rating"));
                movieList.add(movie);
            }

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("movies", movieList);
            out.write(new Gson().toJson(jsonResponse));
            rs.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

}
