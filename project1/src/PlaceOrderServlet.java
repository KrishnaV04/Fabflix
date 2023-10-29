import com.google.gson.*;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import java.io.IOException;

@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/api/place-order")
public class PlaceOrderServlet extends HttpServlet {
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
        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonData = parser.parse(request.getReader());
            JsonObject paymentData = jsonData.getAsJsonObject();

            boolean paymentSuccessful = verifyPayment(paymentData);

            if (paymentSuccessful) {
                List<JsonObject> cartItems = (List<JsonObject>) request.getSession().getAttribute("cart");

                if (cartItems != null && !cartItems.isEmpty()) {
                    recordTransaction(paymentData, cartItems);
                    cartItems.clear();

                    JsonObject successResponse = new JsonObject();
                    successResponse.addProperty("success", true);
                    response.setContentType("application/json");
                    response.getWriter().write(successResponse.toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("success", false);
                errorResponse.addProperty("message", "Payment information is incorrect.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write(errorResponse.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean verifyPayment(JsonObject paymentData) {
        try (Connection connection = dataSource.getConnection()) {

            String creditCardId = paymentData.get("creditCardId").getAsString();
            String firstName = paymentData.get("firstName").getAsString();
            String lastName = paymentData.get("lastName").getAsString();
            Date expiration = Date.valueOf(paymentData.get("expiration").getAsString());

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?"
            );
            statement.setString(1, creditCardId);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setDate(4, expiration);

            ResultSet result = statement.executeQuery();

            boolean paymentMatch = result.next();

            statement.close();

            return paymentMatch;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void recordTransaction(JsonObject paymentData, List<JsonObject> cartItems) {
        try (Connection connection = dataSource.getConnection()) {

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO sales (customerId, movieId, saleDate) VALUES (?, ?, ?)"
            );

            int customerId = paymentData.get("customerId").getAsInt();

            for (JsonObject cartItem : cartItems) {
                String movieId = cartItem.get("id").getAsString();
                Date saleDate = new Date(System.currentTimeMillis());

                statement.setInt(1, customerId);
                statement.setString(2, movieId);
                statement.setDate(3, saleDate);
                statement.addBatch();
            }

            statement.executeBatch();

            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
