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
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="MovieAutocompleteServlet", urlPatterns = "/movie-autocomplete-servlet")
public class MovieAutocompleteServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("query");
        JsonArray jsonArray = new JsonArray();

        if (query != null && !query.trim().isEmpty() && query.length() >= 3) {
            List<MovieSuggestion> suggestions = getMovieSuggestions(query);

            String json = convertToJson(suggestions);
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.print(json);
            out.flush();
        } else {
            response.getWriter().write(jsonArray.toString());
        }
    }

    private List<MovieSuggestion> getMovieSuggestions(String query) {
        List<MovieSuggestion> suggestions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, title FROM movies WHERE MATCH (title) AGAINST (? IN BOOLEAN MODE) LIMIT 10";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                String[] keywords = query.split("\\s+");

                StringBuilder fullTextSearch = new StringBuilder();
                for (String keyword : keywords) {
                    fullTextSearch.append("+").append(keyword).append("* ");
                }
                statement.setString(1, fullTextSearch.toString());

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String title = resultSet.getString("title");
                        String movieId = resultSet.getString("id");
                        MovieSuggestion suggestion = new MovieSuggestion(movieId, title);
                        suggestions.add(suggestion);
                    }
                }
            }
            System.out.println(suggestions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return suggestions;
    }

    private String convertToJson(List<MovieSuggestion> suggestions) {
        JsonArray jsonArray = new JsonArray();
        for (MovieSuggestion suggestion : suggestions) {
            JsonObject movieObject = new JsonObject();
            movieObject.addProperty("id", suggestion.getId());
            movieObject.addProperty("title", suggestion.getTitle());
            jsonArray.add(movieObject);
        }
        return jsonArray.toString();
    }

}
