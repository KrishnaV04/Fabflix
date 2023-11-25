package edu.uci.ics.fabflixmobile.ui.movie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;

public class SingleMovie extends AppCompatActivity {
//    private final String host = "10.0.2.2";
//    private final String port = "8080";
//    private final String domain = "FullStackProject_war";
//    private final String baseURL = "http://" + host + ":" + port + "/" + domain;

    private final String host = "18.217.244.172";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        // get movie id to display
        Intent intent = getIntent();
        String movie_id = null;
        if (intent != null && intent.hasExtra("id")) {
            movie_id = intent.getStringExtra("id");
        }

        final StringRequest singleMovieRequest = new StringRequest(
            Request.Method.GET,
            baseURL + "/api/single-movie?id=" + movie_id,
            new Response.Listener<String>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(String response) {
                    JsonArray jsonArrayResponse = JsonParser.parseString(response).getAsJsonArray();
                    Log.d("movie-id-received", jsonArrayResponse.get(0).getAsJsonObject().toString());

                    JsonObject movieInfo = jsonArrayResponse.get(0).getAsJsonObject();

                    binding.title.setText(movieInfo.get("movie_title").getAsString());
                    binding.year.setText("Year: " + movieInfo.get("movie_year").getAsString());
                    binding.director.setText("Director: " + movieInfo.get("movie_director").toString());
                    binding.genres.setText("Genres: " + extractElements(movieInfo.get("movie_genres"), "genre_name"));
                    binding.stars.setText("Stars: " + extractElements(movieInfo.get("movie_stars"), "star_name"));

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("display_singlemovie", "error displaying single movie");
                }
        });

        queue.add(singleMovieRequest);
    }

    private String extractElements(JsonElement element, String name) {
        StringBuilder genreNamesBuilder = new StringBuilder();
        JsonArray jsonArray = element.getAsJsonArray();
        for (JsonElement arrayElement : jsonArray) {
            if (arrayElement.isJsonObject()) {
                JsonObject jsonObject = arrayElement.getAsJsonObject();

                if (jsonObject.has(name) && !jsonObject.get(name).isJsonNull()) {
                    String genreName = jsonObject.get(name).getAsString();

                    // Append the genre name to the StringBuilder
                    genreNamesBuilder.append(genreName).append(", ");
                }
            }
        }
        if (genreNamesBuilder.length() > 0) {
            genreNamesBuilder.setLength(genreNamesBuilder.length() - 2);
        }

        return genreNamesBuilder.toString();
    }

}
