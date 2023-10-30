import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class JsonConverter {
    public static JsonElement convertInputStreamToJson(Reader reader) {
        try {
            JsonParser jsonParser = new JsonParser();
            return jsonParser.parse(reader);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static JsonArray convertListToJson(List<JsonElement> elements) {
        JsonArray jsonArray = new JsonArray();
        for (JsonElement element : elements) {
            jsonArray.add(element);
        }
        return jsonArray;
    }

    public static List<JsonElement> convertJsonArrayToList(JsonArray jsonArray) {
        List<JsonElement> elements = new ArrayList<>();
        jsonArray.forEach(elements::add);
        return elements;
    }
}
