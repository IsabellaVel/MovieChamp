package anas.online.moviechamp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import anas.online.moviechamp.rest.ApiInterface;
import anas.online.moviechamp.rest.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private final static String API_KEY = BuildConfig.TMDB_API_KEY;

    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int MOVIE_LOADER_ID = 1; // Constant value for the Movie loader ID.
    RecyclerView mRecyclerView;
    ApiInterface apiService = RetrofitClient.getClient().create(ApiInterface.class);
    private Call<Movie> call;
    private Movie movie;
    private List<Movie> mMovies;
    private MovieAdapter mMovieAdapter;
    private MovieAdapter.MovieAdapterOnClickHandler mListener = this;
    private String mSortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.movies_recycler_view);

        setupGridLayout();

        loadMovies("popular");

    }

    public void loadMovies(String sortBy) {

        if (sortBy.equals("popular")) {

            Call<MovieResponse> call = apiService.getPopularMovies(API_KEY);

            call.enqueue(new Callback<MovieResponse>() {

                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    mMovies = response.body().getResults();
                    mMovieAdapter = new MovieAdapter(mMovies, R.layout.item_movie, getApplicationContext(), mListener);
                    mRecyclerView.setAdapter(mMovieAdapter);
                    mRecyclerView.setHasFixedSize(true);
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable error) {
                    // Log error here since request failed
                    Log.e("ERROR", error.toString());
                }
            });
        }

        if (sortBy.equals("top_rated")) {
            Call<MovieResponse> call = apiService.getTopRatedMovies(API_KEY);

            call.enqueue(new Callback<MovieResponse>() {

                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    mMovies = response.body().getResults();
                    mMovieAdapter = new MovieAdapter(mMovies, R.layout.item_movie, getApplicationContext(), mListener);
                    mRecyclerView.setAdapter(mMovieAdapter);
                    mRecyclerView.setHasFixedSize(true);
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable error) {
                    // Log error here since request failed
                    Log.e("ERROR", error.toString());
                }
            });
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.activity_main, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ActionBar actionBar = getSupportActionBar();
        switch (item.getItemId()) {
            case R.id.sort_by_top_rated:
                item.setChecked(true);
                if (actionBar != null) {
                    actionBar.setTitle("Top Rated");
                }

                loadMovies("top_rated");

                return true;

            case R.id.sorty_by_popular:
                item.setChecked(true);
                if (actionBar != null) {
                    actionBar.setTitle("Popular");
                }

                loadMovies("popular");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setupGridLayout() {
        int screenWidth = getResources().getConfiguration().screenWidthDp;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        if (screenWidth > 500) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
    }

    @Override
    public void onClick(Movie movie) {

        Intent movieDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
        movieDetailIntent.putExtra("EXTRA_MOVIE", movie);
        startActivity(movieDetailIntent);

    }
}
