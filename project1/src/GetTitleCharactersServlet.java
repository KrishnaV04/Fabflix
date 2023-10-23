import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet(name = "GetTitleCharactersServlet", urlPatterns = "/getTitleCharacters")
public class GetTitleCharactersServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Use the predefined list of title characters
        List<String> titleCharacters = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "*");

        // Convert the list to JSON and send it as a response
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(titleCharacters));
    }
}