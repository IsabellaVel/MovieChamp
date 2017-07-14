package anas.online.moviechamp;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import anas.online.moviechamp.data.MovieContract;
import anas.online.moviechamp.rest.ApiInterface;
import anas.online.moviechamp.rest.RetrofitClient;
import anas.online.moviechamp.sync.BackdropImageDownloadTask;
import anas.online.moviechamp.sync.PosterImageDownloadTask;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailActivity extends AppCompatActivity implements VideoAdapter.VideoAdapterOnClickHandler {

    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private static final String POSTER_SIZE = "w185";
    private static final String BACKDROP_SIZE = "w780";
    static Movie mMovieData;
    private final String BASE_YOUTUBE_URL = "http://www.youtube.com/watch?v=";
    ApiInterface apiService = RetrofitClient.getClient().create(ApiInterface.class);
    int mMovieId;
    @BindView(R.id.tv_title)
    TextView title;
    @BindView(R.id.iv_backdrop)
    ImageView backdrop;
    @BindView(R.id.iv_poster)
    ImageView poster;
    @BindView(R.id.iv_star)
    ImageView star;
    @BindView(R.id.tv_plot)
    TextView plot;
    @BindView(R.id.tv_releaseDate)
    TextView releaseDate;
    @BindView(R.id.tv_rating)
    TextView rating;
    @BindView(R.id.fab_favorite)
    FloatingActionButton fab_favorite;
    @BindView(R.id.tv_no_reviews_message)
    TextView noReviewsMessage;
    String fullPosterPath;
    String fullBackdropPath;
    String localImagePathString;
    private VideoAdapter.VideoAdapterOnClickHandler mListener = this;
    private List<Review> mReviews;
    private List<Video> mVideos;
    private ReviewAdapter mReviewAdapter;
    private VideoAdapter mVideoAdapter;
    private RecyclerView mVideosRecyclerView;
    private RecyclerView mReviewRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        mReviewRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        mVideosRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);

        mMovieData = getIntent().getExtras().getParcelable("EXTRA_MOVIE");

        title.setText(mMovieData.getTitle());
        plot.setText(mMovieData.getOverview());
        releaseDate.setText(mMovieData.getReleaseDate());
        rating.setText(mMovieData.getVoteAverage().toString());

        fullBackdropPath = BASE_IMAGE_URL + BACKDROP_SIZE + mMovieData.getBackdropPath();
        Picasso.with(this).load(fullBackdropPath).into(backdrop);

        // Get poster path and load the image with Picasso
        fullPosterPath = BASE_IMAGE_URL + POSTER_SIZE + mMovieData.getPosterPath();
        Picasso.with(this).load(fullPosterPath).into(poster);

        mMovieId = mMovieData.getId();

        ApiInterface apiService = RetrofitClient.getClient().create(ApiInterface.class);

        loadReviews();
        loadTrailers();

        fab_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveTofavorites();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void saveTofavorites() throws ExecutionException, InterruptedException {

        String localPosterPath = new PosterImageDownloadTask().execute(fullPosterPath).get();
        String localBackdropPath = new BackdropImageDownloadTask().execute(fullBackdropPath).get();

        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_MOVIE_ID, mMovieData.getId());
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_TITLE, mMovieData.getTitle());
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_BACKDROP_PATH, mMovieData.getBackdropPath());
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_OVERVIEW, mMovieData.getOverview());
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_POSTER_PATH, mMovieData.getPosterPath());
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_EXTERNAL_STORAGE_POSTER_PATH, localPosterPath);
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_EXTERNAL_STORAGE_BACKDROP_PATH, fullBackdropPath);
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE, mMovieData.getReleaseDate());
        movieValues.put(MovieContract.FavoritesEntry.COLUMN_VOTE_AVERAGE, mMovieData.getVoteAverage());

        if (movieValues != null && movieValues.size() != 0) {
                /* Get a handle on the ContentResolver to delete and insert data */
            ContentResolver moviesContentResolver = getContentResolver();

            moviesContentResolver.insert(
                    MovieContract.FavoritesEntry.CONTENT_URI,
                    movieValues);

            Toast.makeText(this, "Saved to favorites!", Toast.LENGTH_SHORT).show();
        }


    }

    public void downloadPosterImage(final String ExternalPosterPath) {
        //  FileDownloadService downloadService = ServiceGenerator.create(FileDownloadService.class);

        Call<ResponseBody> call = apiService.downloadImage(ExternalPosterPath);

        Log.v("TAG", ExternalPosterPath);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("TAG", "server contacted and has file");

                    boolean writtenToDisk = writeResponseBodyToDisk(response.body(), ExternalPosterPath);

                    Log.d("TAG", "file download was a success? " + writtenToDisk);
                } else {
                    Log.d("TAG", "server contact failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("TAG", "error");
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String ExternalImagePath) {
        try {

            // Give every file a different file name, according to their pass-in url string.
            String[] parts = ExternalImagePath.split("/");
            String filename = parts[6];

            // File localPosterPath = new File(getExternalFilesDir(
            //         Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Posters/" + filename);

            File localImagePath = new File(getExternalFilesDir(null) + File.separator + filename);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(localImagePath);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d("TAG", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                // Log.v("TAG", pathString);

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

/*    private static class SaveTask extends AsyncTask<Movie, Void, Void> {

        ContentValues movieValues = new ContentValues();


        @Override
        protected Void doInBackground(Movie... movieData) {

            movieValues.put(MovieContract.FavoritesEntry.COLUMN_MOVIE_ID, mMovieData.getId());
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_TITLE, mMovieData.getTitle());
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_BACKDROP_PATH, backdropPath);
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_OVERVIEW, overview);
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_POSTER_PATH, posterPath);
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_EXTERNAL_STORAGE_POSTER_PATH, localPosterPath );
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_EXTERNAL_STORAGE_BACKDROP_PATH, localBackdropPath );
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_RELEASE_DATE, releaseDate);
            movieValues.put(MovieContract.FavoritesEntry.COLUMN_VOTE_AVERAGE, voteAverage);

            return null;
        }
    }*/

    public void loadTrailers() {
        Call<Video> call = apiService.getMovieVideos(mMovieId, BuildConfig.TMDB_API_KEY);

        call.enqueue(new Callback<Video>() {

            @Override
            public void onResponse(Call<Video> call, Response<Video> response) {
                mVideos = response.body().getVideosList();

                mVideoAdapter = new VideoAdapter(mVideos, R.layout.item_trailer, getApplicationContext(), mListener);
                mVideosRecyclerView.setAdapter(mVideoAdapter);
                mVideosRecyclerView.setHasFixedSize(false);
                mVideosRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));

            }

            @Override
            public void onFailure(Call<Video> call, Throwable error) {
                // Log error here since request failed
                Log.e("ERROR", error.toString());
            }
        });
    }

    public void loadReviews() {
        Call<Review> call = apiService.getMovieReviews(mMovieId, BuildConfig.TMDB_API_KEY);

        call.enqueue(new Callback<Review>() {

            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                mReviews = response.body().getReviewsList();

                mReviewAdapter = new ReviewAdapter(mReviews, R.layout.item_review, getApplicationContext());
                if (mReviewAdapter.getItemCount() > 0) {
                    mReviewRecyclerView.setAdapter(mReviewAdapter);
                    mReviewRecyclerView.setHasFixedSize(false);
                    mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    mReviewRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getApplicationContext()));
                    mReviewRecyclerView.setNestedScrollingEnabled(false);
                } else {
                    noReviewsMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable error) {
                // Log error here since request failed
                Log.e("ERROR", error.toString());
            }
        });
    }

    @Override
    public void onClick(String trailerKey) {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BASE_YOUTUBE_URL + trailerKey));
        startActivity(intent);

    }

}
