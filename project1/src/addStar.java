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
import java.io.PrintWriter;
import java.sql.*;
import java.util.List;

@WebServlet(name = "addStar", urlPatterns = "/_dashboard/api/add-star")
public class addStar extends HttpServlet {
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
            JsonObject starData = jsonData.getAsJsonObject();

            String starName = starData.get("starName").getAsString();
            String birthYear = starData.get("birthYear").getAsString();

            System.out.println("Entered add Start Post");
            System.out.println(birthYear);
            System.out.println(starName);

            if (starName.isEmpty()) {
                return;
            }

            try (Connection conn = dataSource.getConnection()) {

                // Construct a query with parameter represented by "?"
                String query = "CALL AddStar(?,?);";

                // Declare our statement
                PreparedStatement statement = conn.prepareStatement(query);

                statement.setString(1, starName);

                if (birthYear.isEmpty())
                {
                    statement.setNull(2, java.sql.Types.INTEGER);
                }
                else
                {
                    statement.setInt(2, Integer.parseInt(birthYear));
                }

                // Perform the query
                ResultSet rs = statement.executeQuery();

                System.out.println("Star Added Successfully.");
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
