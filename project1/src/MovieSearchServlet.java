import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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


/*
MYSQL CODE FOR FUTURE:
SELECT m.id, substring_index(GROUP_CONCAT(g.name, ':', g.id ORDER BY g.name ASC SEPARATOR ','), ',', 3) AS three_genres
    FROM movies m
    RIGHT JOIN genres_in_movies gim ON m.id = gim.movieId
    JOIN genres g ON g.id = gim.genreId
WHERE m.title LIKE ?
GROUP BY m.id;

SELECT m.id, substring_index(GROUP_CONCAT(s.name, ':', s.id ORDER BY s.numMovies DESC SEPARATOR ','), ',', 3) as stars
FROM movies m
        RIGHT JOIN stars_in_movies sim ON m.id = sim.movieId
        JOIN stars s ON s.id = sim.starId
WHERE m.title LIKE '%%'
GROUP BY m.id;
 */

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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        // Retrieve search parameters from the request
        String searchTitle = request.getParameter("title");
        String searchYear = request.getParameter("year");
        String searchDirector = request.getParameter("director");
        String searchStar = request.getParameter("star");

        String order = request.getParameter("order");
        String rating_sort = request.getParameter("rating_sort");
        String title_sort = request.getParameter("title_sort");
        String results_per_page = request.getParameter("page_results");
        String pageNumber = request.getParameter("page_number");

        String searchText = request.getParameter("search_text");
        if (searchText != null && !searchText.isEmpty()) {
            try (Connection conn = dataSource.getConnection()) {
                String fuzzySearchQuery = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                        "(SELECT GROUP_CONCAT(g.name ORDER BY g.name ASC SEPARATOR ',') " +
                        "FROM genres_in_movies gim " +
                        "JOIN genres g ON gim.genreId = g.id " +
                        "WHERE gim.movieId = m.id LIMIT 3) AS three_genres, " +
                        "SUBSTRING_INDEX(GROUP_CONCAT(s.name, ':', s.id ORDER BY s.numMovies DESC SEPARATOR ','), ',', 3) AS three_stars " +
                        "FROM movies m " +
                        "JOIN ratings r ON r.movieId = m.id " +
                        "JOIN genres_in_movies gim ON gim.movieId = m.id " +
                        "JOIN genres g ON gim.genreId = g.id " +
                        "JOIN stars_in_movies sim ON sim.movieId = m.id " +
                        "JOIN stars s ON s.id = sim.starId " +
                        "WHERE MATCH(m.title) AGAINST(? IN BOOLEAN MODE) ";

                if (searchText.length() >= 8) {
                    fuzzySearchQuery += "OR m.title LIKE ? OR edth(m.title, ?, ?) ";
                }

                fuzzySearchQuery += "GROUP BY m.id, m.director, m.year, m.title, r.rating ";

                if (("asc".equals(rating_sort) || "desc".equals(rating_sort)) && ("asc".equals(title_sort) || "desc".equals(title_sort))) {
                    if ("title".equals(order)) {
                        fuzzySearchQuery += "ORDER BY m.title " + title_sort + " , r.rating " + rating_sort;
                    } else if ("rating".equals(order)) {
                        fuzzySearchQuery += "ORDER BY r.rating " + rating_sort + " , m.title " + title_sort;
                    }
                }

                fuzzySearchQuery += " LIMIT ? OFFSET ?;";

                PreparedStatement fuzzySearchStatement = conn.prepareStatement(fuzzySearchQuery);
                int parameterIndex = 1;

                String[] keywords = searchText.split("\\s+");
                StringBuilder fullTextSearch = new StringBuilder();
                for (String keyword : keywords) {
                    fullTextSearch.append("+").append(keyword).append("* ");
                }

                fuzzySearchStatement.setString(parameterIndex++, fullTextSearch.toString());
                if (searchText.length() >= 8) {
                    fuzzySearchStatement.setString(parameterIndex++, "%" + searchText + "%"); // For SQL LIKE
                    fuzzySearchStatement.setString(parameterIndex++, searchText); // For edth
                    fuzzySearchStatement.setInt(parameterIndex++, 2); // Edit distance threshold
                }

                if (results_per_page == null) {
                    results_per_page = "10";
                }
                if (pageNumber == null) {
                    pageNumber = "0";
                }
                fuzzySearchStatement.setInt(parameterIndex++, Integer.parseInt(results_per_page));
                fuzzySearchStatement.setInt(parameterIndex++, Integer.parseInt(results_per_page) * Integer.parseInt(pageNumber));

                ResultSet fuzzySearchResultSet = fuzzySearchStatement.executeQuery();

                JsonArray fullTextMovieList = new JsonArray();
                while (fuzzySearchResultSet.next()) {
                    JsonObject fullTextMovie = new JsonObject();
                    fullTextMovie.addProperty("movie_id", fuzzySearchResultSet.getString("id"));
                    fullTextMovie.addProperty("movie_title", fuzzySearchResultSet.getString("title"));
                    fullTextMovie.addProperty("movie_year", fuzzySearchResultSet.getInt("year"));
                    fullTextMovie.addProperty("movie_director", fuzzySearchResultSet.getString("director"));
                    fullTextMovie.addProperty("movie_rating", fuzzySearchResultSet.getDouble("rating"));
                    fullTextMovie.addProperty("movie_genres", fuzzySearchResultSet.getString("three_genres"));
                    fullTextMovie.addProperty("movie_stars", fuzzySearchResultSet.getString("three_stars"));
                    fullTextMovieList.add(fullTextMovie);
                }

                JsonObject fullTextJsonResponse = new JsonObject();
                fullTextJsonResponse.add("movies", fullTextMovieList);

                out.write(new Gson().toJson(fullTextJsonResponse));

                fuzzySearchResultSet.close();
                fuzzySearchStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("errorMessage", "Failed to perform the full-text search.");
                out.write(new Gson().toJson(errorJson));
                response.setStatus(500);
            } finally {
                out.close();
            }
        } else {
            try (Connection conn = dataSource.getConnection()) {
                String query = "SELECT m.id, m.title, m.year, m.director, r.rating,\n" +
                        "(SELECT GROUP_CONCAT(g.name ORDER BY g.name ASC SEPARATOR ',') \n" +
                        "        FROM genres_in_movies gim\n" +
                        "        JOIN genres g ON gim.genreId = g.id\n" +
                        "        WHERE gim.movieId = m.id\n" +
                        "        LIMIT 3) AS three_genres,\n" +
                        "substring_index(GROUP_CONCAT(s.name, ':', s.id ORDER BY s.numMovies DESC SEPARATOR ','), ',', 3) as three_stars\n" +
                        "    FROM movies m\n" +
                        "    JOIN ratings r ON r.movieId = m.id\n" +
                        "    JOIN genres_in_movies gim ON gim.movieId = m.id\n" +
                        "    JOIN genres g ON gim.genreId = g.id\n" +
                        "    JOIN stars_in_movies sim ON sim.movieId = m.id\n" +
                        "    JOIN stars s ON s.id = sim.starId\n" +
                        "WHERE m.title LIKE ?";

                if (!searchYear.isEmpty()) { query += " AND m.year = ?";}

                query += " AND m.director LIKE ? AND s.name LIKE ?\n" +
                        "GROUP BY m.id, m.director, m.year, m.title, r.rating\n";

                if (("asc".equals(rating_sort) || "desc".equals(rating_sort)) && ("asc".equals(title_sort) || "desc".equals(title_sort))) {
                    if ("title".equals(order)) {
                        query += "ORDER BY m.title " + title_sort + " , r.rating " + rating_sort;
                    } else if("rating".equals(order)) {
                        query += "ORDER BY r.rating " + rating_sort + " , m.title " + title_sort;
                    }
                }

                query += " LIMIT ? OFFSET ?;";

                PreparedStatement statement = conn.prepareStatement(query);
                int parameterIndex = 1;

                statement.setString(parameterIndex++, "%" + searchTitle + "%");
                if (!searchYear.isEmpty()) {statement.setInt(parameterIndex++, Integer.parseInt(searchYear));}
                statement.setString(parameterIndex++, "%" + searchDirector + "%");
                statement.setString(parameterIndex++, "%" + searchStar + "%");


                if (results_per_page == null) {results_per_page = "10";}
                if (pageNumber == null) {pageNumber = "0";}
                statement.setInt(parameterIndex++, Integer.parseInt(results_per_page));
                statement.setInt(parameterIndex++, Integer.parseInt(results_per_page) * Integer.parseInt(pageNumber));

                ResultSet resultSet = statement.executeQuery();

                JsonArray movieList = new JsonArray();
                while (resultSet.next()) {
                    JsonObject movie = new JsonObject();
                    movie.addProperty("movie_id", resultSet.getString("id"));
                    movie.addProperty("movie_title", resultSet.getString("title"));
                    movie.addProperty("movie_year", resultSet.getInt("year"));
                    movie.addProperty("movie_director", resultSet.getString("director"));
                    movie.addProperty("movie_genres", resultSet.getString("three_genres"));
                    movie.addProperty("movie_stars", resultSet.getString("three_stars"));
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
}