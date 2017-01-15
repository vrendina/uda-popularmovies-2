package software.level.udacity.popularmovies2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import software.level.udacity.popularmovies2.api.MovieService;
import software.level.udacity.popularmovies2.api.model.MovieTrailerEnvelope;

public class MovieGridActivity extends AppCompatActivity {

    public static final String TAG = MovieGridActivity.class.getSimpleName();

    // Constants to identify the type of movie request based on what is selected
    private static final int REQUEST_POPULAR = 0;
    private static final int REQUEST_TOPRATED = 1;
    private static final int REQUEST_FAVORITE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);

        OkHttpClient client = new OkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/movie/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        MovieService service = retrofit.create(MovieService.class);

        String apiKey = getResources().getString(R.string.API_KEY);
//        Call<MovieEnvelope> envelope = service.topRated(apiKey);

        Call<MovieTrailerEnvelope> envelope = service.trailers(278, apiKey);


        envelope.enqueue(new Callback<MovieTrailerEnvelope>() {

            @Override
            public void onResponse(Call<MovieTrailerEnvelope> call, Response<MovieTrailerEnvelope> response) {
                Log.d(TAG, "onResponse: " + response.body().trailers.toString());
            }

            @Override
            public void onFailure(Call<MovieTrailerEnvelope> call, Throwable t) {

            }
        });

    }

    /**
     * Inflates the menu resource for this activity.
     *
     * @param menu Interface that manages the menu
     * @return True if the menu is to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_movie_grid, menu);

        // Set the default selection to popular
        menu.findItem(R.id.action_popular).setChecked(true);

        return true;
    }

    /**
     * Handles selection of items in the menu.
     *
     * @param item The menu item that was selected
     * @return Return false to allow normal menu processing, true to consume it here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If the item is already selected don't do anything
        if(item.isChecked()) return false;

        item.setChecked(true);

        int id = item.getItemId();
        switch(id) {
            case R.id.action_favorite:
                fetchMovieData(REQUEST_FAVORITE);
                return true;

            case R.id.action_popular:
                fetchMovieData(REQUEST_POPULAR);
                return true;

            case R.id.action_toprated:
                fetchMovieData(REQUEST_TOPRATED);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Kicks off the loading of movie data
     *
     * @param requestType Constant to identify the type of movie request
     */
    private void fetchMovieData(int requestType) {
        Log.d(TAG, "fetchMovieData: Movie request type -- " + requestType);
    }
}
