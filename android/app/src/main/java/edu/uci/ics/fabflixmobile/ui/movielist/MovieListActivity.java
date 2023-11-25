package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.*;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.movie.SingleMovie;

import java.util.ArrayList;

public class MovieListActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_movielist);
        Button btnNext = findViewById(R.id.btnNext);
        Button btnPrev = findViewById(R.id.btnPrev);


        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        Intent intent = getIntent();
        int page_number = 0;
        if (intent != null && intent.hasExtra("page_number")) {
            page_number = intent.getIntExtra("page_number", 0);
        }

        int finalPage_number = page_number;
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent nextPage = new Intent(MovieListActivity.this, MovieListActivity.class);
                nextPage.putExtra("page_number", finalPage_number + 1);
                startActivity(nextPage);
            }
        });

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent nextPage = new Intent(MovieListActivity.this, MovieListActivity.class);
                nextPage.putExtra("page_number", Math.max(finalPage_number - 1, 0));
                startActivity(nextPage);
            }
        });

        final StringRequest listRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/movieSearch" + "?page_number=" + finalPage_number,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                        display(jsonResponse);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("loadmovies-error", "error loading movies");
            }
        });

        queue.add(listRequest);
    }


    public void display(JsonObject data) {

        ArrayList<Movie> movies = new ArrayList<>();
        JsonArray json_movies = data.getAsJsonArray("movies");
        for (JsonElement json_movie_element : json_movies) {
            JsonObject json_movie = json_movie_element.getAsJsonObject();

            movies.add(new Movie(
                    json_movie.get("movie_id").getAsString(),
                    json_movie.get("movie_title").getAsString(),
                    json_movie.get("movie_year").getAsShort(),
                    json_movie.get("movie_director").getAsString(),
                    json_movie.get("movie_stars").getAsString(),
                    json_movie.get("movie_genres").getAsString()
            ));
        }

        // listview adapter
        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
        ListView listView = findViewById(R.id.list);
        listView.setAdapter(adapter);

        // listener
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Movie movie = movies.get(position);
            @SuppressLint("DefaultLocale") String message = String.format("Clicked on position: %d, name: %s", position, movie.getName());
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

            finish();
            Intent MovieListPage = new Intent(MovieListActivity.this, SingleMovie.class);
            MovieListPage.putExtra("id", movie.getId());
            startActivity(MovieListPage);

        });
    }
}