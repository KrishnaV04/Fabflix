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

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT title FROM movies WHERE 1=1";
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
                String title = resultSet.getString("title");
                movieList.add(title);
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
