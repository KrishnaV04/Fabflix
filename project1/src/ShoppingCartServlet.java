import com.google.gson.*;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        ArrayList<JsonObject> cart = (ArrayList<JsonObject>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<JsonObject>();
        }

        JsonArray cartJsonArray = new JsonArray();

        for (JsonObject item : cart) {
            cartJsonArray.add(item);
        }

        response.setContentType("application/json");
        response.getWriter().write(cartJsonArray.toString());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        ArrayList<JsonObject> cart = (ArrayList<JsonObject>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        try {
            JsonParser parser = new JsonParser();
            JsonElement jsonData = parser.parse(request.getReader());
            JsonObject movieData = jsonData.getAsJsonObject();

            boolean movieExistsInCart = false;
            for (JsonObject item : cart) {
                if (item.get("id").getAsInt() == movieData.get("id").getAsInt()) {
                    int newQuantity = item.get("quantity").getAsInt() + 1;
                    item.addProperty("quantity", newQuantity);
                    movieExistsInCart = true;
                    break;
                }
            }

            if (!movieExistsInCart) {
                cart.add(movieData);
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


}

