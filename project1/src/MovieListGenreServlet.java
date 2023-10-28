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

@WebServlet(name = "MovieListGenreServlet", urlPatterns = "/movieListGenre")
public class MovieListGenreServlet extends HttpServlet {
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

        String genre = request.getParameter("browseGenre");
        String order = request.getParameter("order");
        String rating_sort = request.getParameter("rating_sort");
        String title_sort = request.getParameter("title_sort");
        String results_per_page = request.getParameter("results_per_page");
        String pageNumber = request.getParameter("page_number");

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director," +
                    "(SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) FROM ( SELECT DISTINCT g.name FROM genres_in_movies gim RIGHT JOIN genres g ON gim.genreId = g.id WHERE gim.movieId = m.id LIMIT 3 ) AS g)AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(s.name, ':', s.id) )\n" +
                    "           FROM ( SELECT s.id, s.name, COUNT(stars_in_movies.movieId) AS count_movies \n" +
                    "           FROM stars_in_movies JOIN (SELECT stars.id, stars.name FROM stars JOIN stars_in_movies ON stars.id = stars_in_movies.starId WHERE stars_in_movies.movieId = m.id) AS s \n" +
                    "            ON s.id = stars_in_movies.starId \n" +
                    "            GROUP BY s.id, s.name \n" +
                    "            ORDER BY count_movies DESC, s.name ASC \n" +
                    "            LIMIT 3) AS s) AS stars, "+
                    "m.rating FROM (SELECT m.id, m.title, m.year, m.director, r.rating FROM ratings r JOIN movies m ON m.id = r.movieId ORDER BY r.rating DESC) AS m " +
                    "WHERE m.id IN (\n" +
                    "    SELECT DISTINCT m.id\n" +
                    "    FROM movies m\n" +
                    "    JOIN genres_in_movies gm ON m.id = gm.movieId\n" +
                    "    JOIN genres g ON gm.genreId = g.id\n" +
                    "    WHERE g.name = ?\n" +
                    ")" +
                    "GROUP BY m.id, m.title, m.year, m.director, m.rating ";

            // accounting for sorting
            if (("asc".equals(rating_sort) || "desc".equals(rating_sort)) && ("asc".equals(title_sort) || "desc".equals(title_sort))) {
                if ("title".equals(order)) {
                    query += "ORDER BY m.title " + title_sort + " , m.rating " + rating_sort;
                } else if ("rating".equals(order)) {
                    query += "ORDER BY m.rating " + rating_sort + " , m.title " + title_sort;
                }
            }
            if (results_per_page != null) {
                query += " LIMIT " + results_per_page;
                if (pageNumber != null) {
                    query += " OFFSET " + Integer.parseInt(results_per_page) * Integer.parseInt(pageNumber);
                }
            }

            query += " ;";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, genre);

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
