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
                query = "SELECT title FROM movies WHERE title REGEXP '^[^A-Za-z0-9]';";
            } else {
                query = "SELECT title FROM movies WHERE LOWER(title) LIKE ?";
            }

            PreparedStatement statement = conn.prepareStatement(query);
            if (!"*".equals(titleChar)) {
                statement.setString(1, titleChar.toLowerCase() + "%");
            }

            ResultSet rs = statement.executeQuery();

            JsonArray movieList = new JsonArray();

            while (rs.next()) {
                String title = rs.getString("title");
                movieList.add(title);
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
