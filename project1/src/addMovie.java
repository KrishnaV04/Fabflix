import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

@WebServlet(name = "addMovie", urlPatterns = "/_dashboard/api/add-movie")
public class addMovie extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        try {
            JsonElement jsonData = JsonConverter.convertInputStreamToJson(request.getReader());
            JsonObject movieData = jsonData.getAsJsonObject();

            String title = movieData.get("title").getAsString();
            String birthYear = movieData.get("year").getAsString();
            String director = movieData.get("director").getAsString();
            String starName = movieData.get("starName").getAsString();
            String genre = movieData.get("genre").getAsString();


            try (Connection conn = dataSource.getConnection()) {

                // Construct a query with parameter represented by "?"
                String query = "CALL AddMovie(?,?,?,?,?);";

                // Declare our statement
                PreparedStatement statement = conn.prepareStatement(query);

                statement.setString(1, title);
                statement.setString(2, birthYear);
                statement.setString(3, director);
                statement.setString(4, starName);
                statement.setString(5, genre);

                // Perform the query
                ResultSet rs = statement.executeQuery();

                System.out.println("Movie Added Successfully.");
            }

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            response.setContentType("application/json");
            response.getWriter().write(responseJson.toString());
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", false);
            response.setContentType("application/json");
            response.getWriter().write(responseJson.toString());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }
}
