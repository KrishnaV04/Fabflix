import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "RequestDispatcherServlet", urlPatterns = "/requestDispatch")
public class RequestDispatcherServlet extends HttpServlet {
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

        HttpSession session = request.getSession();
        String urlParams = (String) session.getAttribute("url");

        System.out.println(request.getQueryString());

        if (urlParams.startsWith("title")) {
            String newUrl = "/movieSearch?" + urlParams;
            System.out.println(newUrl + "     --- new url");
            RequestDispatcher dispatcher = request.getRequestDispatcher(newUrl);
            dispatcher.forward(request, response);
        }
        else if (urlParams.startsWith("browseGenre")) {
            String newUrl = "/movieListGenre?" + urlParams;
            RequestDispatcher dispatcher = request.getRequestDispatcher(newUrl);
            dispatcher.forward(request, response);
        }
        else if (urlParams.startsWith("browseTitle")) {
            String newUrl = "/movieListTitle?" + urlParams;
            RequestDispatcher dispatcher = request.getRequestDispatcher(newUrl);
            dispatcher.forward(request, response);
        }
        else{
            System.out.println("Error occured when dispatching from session info");
        }

    }
}
