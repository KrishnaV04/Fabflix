import com.google.gson.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        @SuppressWarnings("unchecked")
        ArrayList<JsonObject> cart = (ArrayList<JsonObject>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<JsonObject>();
        }
        System.out.println(cart);
        JsonArray cartJsonArray = new JsonArray();

        for (JsonObject item : cart) {
            cartJsonArray.add(item);
        }

        response.setContentType("application/json");
        response.getWriter().write(cartJsonArray.toString());
        System.out.println("HERE");
        System.out.println(cartJsonArray);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("WE IN HERE POSTING");
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        ArrayList<JsonObject> cart = (ArrayList<JsonObject>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        try {
            JsonElement jsonData = JsonConverter.convertInputStreamToJson(request.getReader());
            JsonObject movieData = jsonData.getAsJsonObject();
            System.out.println(jsonData);
            System.out.println(movieData);

            boolean movieExistsInCart = false;
            for (JsonObject item : cart) {
                if (Objects.equals(item.get("id").getAsString(), movieData.get("id").getAsString())) {
                    if (movieData.has("quantity")) {
                        int newQuantity = movieData.get("quantity").getAsInt();
                        item.addProperty("quantity", newQuantity);
                    } else {
                        int newQuantity = item.get("quantity").getAsInt() + 1;
                        item.addProperty("quantity", newQuantity);
                    }
                    System.out.println("IN THE LOOP ADDING QUANTITY");
                    System.out.println(item);
                    movieExistsInCart = true;
                    break;
                }
            }

            if (!movieExistsInCart) {
                System.out.println("NEW ELEMENT ALERT!!!");
                movieData.addProperty("quantity", 1);
                cart.add(movieData);
            }
            System.out.println(cart);
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.add("cart", cartAsJsonArray(cart));
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

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        @SuppressWarnings("unchecked")
        ArrayList<JsonObject> cart = (ArrayList<JsonObject>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }
        System.out.println("CART BEFORE:");
        System.out.println(cart);
        try {
            JsonElement jsonData = JsonConverter.convertInputStreamToJson(request.getReader());
            JsonObject movieData = jsonData.getAsJsonObject();

            cart.removeIf(item -> Objects.equals(item.get("id").getAsString(), movieData.get("id").getAsString()));
            System.out.println("CART AFTER:");
            System.out.println(cart);
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.add("cart", cartAsJsonArray(cart));
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


    private JsonArray cartAsJsonArray(ArrayList<JsonObject> cart) {
        JsonArray cartJsonArray = new JsonArray();
        for (JsonObject item : cart) {
            cartJsonArray.add(item);
        }
        return cartJsonArray;
    }


}

