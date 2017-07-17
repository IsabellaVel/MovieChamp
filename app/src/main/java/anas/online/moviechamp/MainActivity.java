package anas.online.moviechamp;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import anas.online.moviechamp.data.MovieContract;
import anas.online.moviechamp.rest.ApiInterface;
import anas.online.moviechamp.rest.RetrofitClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String[] FAVORITES_PROJECTION = {
            MovieContract.FavoritesEntry._ID,
            MovieContract.FavoritesEntry.COLUMN_MOVIE_ID,
            MovieContract.FavoritesEntry.COLUMN_TITLE,
            MovieContract.FavoritesEntry.COLUMN_EXTERNAL_STORAGE_POSTER_PATH,
            MovieContract.FavoritesEntry.COLUMN_OVERVIEW,
            MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE,
            MovieContract.FavoritesEntry.COLUMN_EXTERNAL_STORAGE_BACKDROP_PATH,
            MovieContract.FavoritesEntry.COLUMN_VOTE_AVERAGE
    };
    /*
    * We store the indices of the values in the array of Strings above to more quickly be able to
    * access the data from our query. If the order of the Strings above changes, these indices
    * must be adjusted to match the order of the Strings.
    */

    public static final int INDEX_ID = 0;
    public static final int INDEX_MOVIE_ID = 1;
    public static final int INDEX_TITLE = 2;
    public static final int INDEX_MOVIE_EXTERNAL_STORAGE_POSTER_PATH = 3;
    public static final int INDEX_OVERVIEW = 4;
    public static final int INDEX_RELEASE_DATE = 5;
    public static final int INDEX_EXTERNAL_STORAGE_BACKDROP_PATH = 6;
    public static final int INDEX_VOTE_AVERAGE = 7;


    private static final String BASE_URL = "http://api.themoviedb.org/3/";
    private final static String API_KEY = BuildConfig.TMDB_API_KEY;
    // This ID is for the cursor loader that loads favorites data from DB
    private static final int CURSOR_MOVIE_LOADER_ID = 1;
    private final static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int MOVIE_LOADER_ID = 1; // Constant value for the Movie loader ID.
    RecyclerView mRecyclerView;
    ApiInterface apiService = RetrofitClient.getClient().create(ApiInterface.class);
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    SharedPreferences.Editor editor;
    private Call<Movie> call;
    private Movie movie;
    private List<Movie> mMovies;
    private MovieAdapter mMovieAdapter;
    private MovieAdapter.MovieAdapterOnClickHandler mListener = this;
    private String mSortBy;
    private int mPosition = RecyclerView.NO_POSITION;
    private Cursor mCursor;
    private String clickedItemType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clickedItemType = "popular";

        SharedPreferences prefs = getSharedPreferences("sort", MODE_PRIVATE);
        editor = prefs.edit();

        ButterKnife.bind(this);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mRecyclerView = (RecyclerView) findViewById(R.id.movies_recycler_view);
        setupGridLayout();

        if (getPreference().equals("popular")) {
            Toast.makeText(this, "popular", Toast.LENGTH_SHORT).show();
        } else if (getPreference().equals("top_rated")) {
            Toast.makeText(this, "top rated", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "favorites", Toast.LENGTH_SHORT).show();


        loadMovies("popular");

    }

    public void loadMovies(String sortBy) {

        if (sortBy.equals("popular")) {
            clickedItemType = "popular";
            Call<MovieResponse> call = apiService.getPopularMovies(API_KEY);

            call.enqueue(new Callback<MovieResponse>() {

                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                    mMovies = response.body().getResults();
                    mMovieAdapter = new MovieAdapter(mMovies, R.layout.item_movie, getApplicationContext(), mListener);
                    mRecyclerView.setAdapter(mMovieAdapter);
                    mRecyclerView.setHasFixedSize(true);
                    mLoadingIndicator.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable error) {
                    // Log error here since request failed
                    showErrorMessage();
                    Log.e("ERROR", error.toString());
                }
            });
        }

        if (sortBy.equals("top_rated")) {
            clickedItemType = "top_rated";
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

        if (sortBy.equals("favorites")) {
            clickedItemType = "favorites";
            mMovieAdapter = new MovieAdapter(mMovies, R.layout.item_movie, getApplicationContext(), mListener);
            mRecyclerView.setAdapter(mMovieAdapter);

            getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);


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
                editor.putString("sort_by", "top_rated");
                editor.apply();
                item.setChecked(true);
                if (actionBar != null) {
                    actionBar.setTitle("Top Rated");
                }

                loadMovies("top_rated");
                return true;

            case R.id.sorty_by_popular:
                editor.putString("sort_by", "popular");
                editor.apply();
                item.setChecked(true);
                if (actionBar != null) {
                    actionBar.setTitle("Popular");
                }

                loadMovies("popular");
                return true;

            case R.id.sort_by_favorites:
                editor.putString("sort_by", "favorites");
                editor.apply();
                item.setChecked(true);
                if (actionBar != null) {
                    actionBar.setTitle("Favorites");
                }

                loadMovies("favorites");
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
    public void onClick(Movie movie, int movieType, int id) {

        if (movieType == 1) {

            Intent intent = new Intent(MainActivity.this, DetailActivity.class);

            // Put the Parcelable Movie object into an intent
            intent.putExtra("EXTRA_MOVIE", movie);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this, FavoriteDetailActivity.class);

            Uri movieUri = ContentUris.withAppendedId(MovieContract.FavoritesEntry.CONTENT_URI, id);

            // Put the Parcelable Movie object into an intent
            intent.putExtra("EXTRA_MOVIE", movie);
            intent.putExtra("MOVIE_URI", movieUri.toString());
            startActivity(intent);
        }


    }

    private void showErrorMessage() {
        mLoadingIndicator.setVisibility(View.GONE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private boolean networkConnected() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkStatus = connMgr.getActiveNetworkInfo();

        // If network is available then return true, else, false is returned
        return (networkStatus != null && networkStatus.isConnected());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case CURSOR_MOVIE_LOADER_ID:
                return new CursorLoader(
                        this,
                        MovieContract.FavoritesEntry.CONTENT_URI,
                        FAVORITES_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        // Return to same scrolling position on orientation change
        //  if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        //  mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showMovieDataView();

        // mLoadingIndicator.setVisibility(View.GONE);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);
    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message
     */
    public void showMovieDataView() {
        // First, make sure the error is invisible.
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Then, make sure the movie data is visible.
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    @NonNull
    private String getPreference() {
        SharedPreferences shared = getSharedPreferences("sort", MODE_PRIVATE);
        String pref = (shared.getString("sort_by", ""));
        Log.v("Main", "value is: " + pref);
        return pref;
    }

}
