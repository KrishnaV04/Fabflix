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
import java.util.Objects;

@WebServlet(name = "MovieSearchServlet", urlPatterns = "/movieSearch")
public class MovieSearchServlet extends HttpServlet {
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

        // Retrieve search parameters from the request
        String searchTitle = request.getParameter("title");
        String searchYear = request.getParameter("year");
        String searchDirector = request.getParameter("director");
        String searchStar = request.getParameter("star");

        // these lines don't work
        String order = request.getParameter("order");
        String rating_sort = request.getParameter("rating_sort");
        String title_sort = request.getParameter("title_sort");

        //int results_per_page = Integer.parseInt(request.getParameter("results_per_page"));


        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT m.id, m.title, m.year, m.director," +
                    "(SELECT GROUP_CONCAT(DISTINCT g.name ORDER BY g.name ASC) FROM ( SELECT DISTINCT g.name FROM genres_in_movies gim RIGHT JOIN genres g ON gim.genreId = g.id WHERE gim.movieId = m.id LIMIT 3 ) AS g)AS genres, " +
                    "(SELECT GROUP_CONCAT(CONCAT(s.name, ':', s.id) ) \n" +
                    "           FROM ( SELECT s.id, s.name, COUNT(stars_in_movies.movieId) AS count_movies \n" +
                    "           FROM stars_in_movies JOIN (SELECT stars.id, stars.name FROM stars JOIN stars_in_movies ON stars.id = stars_in_movies.starId WHERE stars_in_movies.movieId = m.id) AS s \n" +
                    "            ON s.id = stars_in_movies.starId \n" +
                    "            GROUP BY s.id, s.name \n" +
                    "            ORDER BY count_movies DESC, s.name ASC \n" +
                    "            LIMIT 3) AS s) AS stars, "+
                    "m.rating FROM (SELECT m.id, m.title, m.year, m.director, r.rating FROM ratings r JOIN movies m ON m.id = r.movieId ORDER BY r.rating DESC) AS m " +
                    "WHERE 1=1";
//            String query = "SELECT title FROM movies WHERE 1=1";
            if (!searchTitle.isEmpty()) {
                query += " AND title LIKE ?";
            }
            if (!searchYear.isEmpty()) {
                query += " AND year = ?";
            }
            if (!searchDirector.isEmpty()) {
                query += " AND director LIKE ?";
            }
            if (!searchStar.isEmpty()) {
                query += " AND starName LIKE ?";
            }

            query += " GROUP BY m.id, m.title, m.year, m.director, m.rating ";

            String sort_query = "";

            // accounting for sorting
            if (("asc".equals(rating_sort) || "desc".equals(rating_sort)) && ("asc".equals(title_sort) || "desc".equals(title_sort)))
            {
                System.out.println("reached");
                if ("title".equals(order))       {sort_query += "ORDER BY m.title " + title_sort + " , m.rating " + rating_sort; System.out.println(sort_query);}

                else if ("rating".equals(order)) {sort_query += "ORDER BY m.rating " + rating_sort + " , m.title " + title_sort; System.out.println(sort_query);}
            }
            query += sort_query + " ;";
            //query += sort_query + " LIMIT " + results_per_page + " ;";


            PreparedStatement statement = conn.prepareStatement(query);
            int parameterIndex = 1;
            if (!searchTitle.isEmpty()) {
                statement.setString(parameterIndex++, "%" + searchTitle + "%");
            }
            if (!searchYear.isEmpty()) {
                statement.setInt(parameterIndex++, Integer.parseInt(searchYear));
            }
            if (!searchDirector.isEmpty()) {
                statement.setString(parameterIndex++, "%" + searchDirector + "%");
            }
            if (!searchStar.isEmpty()) {
                statement.setString(parameterIndex++, "%" + searchStar + "%");
            }


            // Execute the query and retrieve search results
            ResultSet resultSet = statement.executeQuery();

            JsonArray movieList = new JsonArray();
            while (resultSet.next()) {
                JsonObject movie = new JsonObject();
                movie.addProperty("movie_id", resultSet.getString("id"));
                movie.addProperty("movie_title", resultSet.getString("title"));
                movie.addProperty("movie_year", resultSet.getInt("year"));
                movie.addProperty("movie_director", resultSet.getString("director"));
                movie.addProperty("movie_genres", resultSet.getString("genres"));
                movie.addProperty("movie_stars", resultSet.getString("stars"));
                movie.addProperty("movie_rating", resultSet.getDouble("rating"));
                movieList.add(movie);
            }

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("movies", movieList);

            // Write the search results as a JSON response
            out.write(new Gson().toJson(jsonResponse));

            resultSet.close();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Write an error message JSON object to the output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set the response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
