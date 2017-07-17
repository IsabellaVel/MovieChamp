package anas.online.moviechamp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.util.List;

import anas.online.moviechamp.rest.ApiInterface;
import anas.online.moviechamp.rest.RetrofitClient;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteDetailActivity extends AppCompatActivity implements VideoAdapter.VideoAdapterOnClickHandler {

    static Movie mMovieData;
    private final String BASE_YOUTUBE_URL = "http://www.youtube.com/watch?v=";
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
    ApiInterface apiService = RetrofitClient.getClient().create(ApiInterface.class);
    private List<Review> mReviews;
    private List<Video> mVideos;
    private ReviewAdapter mReviewAdapter;
    private VideoAdapter mVideoAdapter;
    private RecyclerView mVideosRecyclerView;
    private RecyclerView mReviewRecyclerView;
    private VideoAdapter.VideoAdapterOnClickHandler mListener = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_detail);

        ButterKnife.bind(this);

        mReviewRecyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        mVideosRecyclerView = (RecyclerView) findViewById(R.id.rv_trailers);

        mMovieData = getIntent().getExtras().getParcelable("EXTRA_MOVIE");

        title.setText(mMovieData.getTitle());
        plot.setText(mMovieData.getOverview());
        releaseDate.setText(mMovieData.getReleaseDate());
        rating.setText(mMovieData.getVoteAverage().toString());
        Picasso.with(this)
                .load(new File(mMovieData.getPosterPath()))
                .into(poster);
        Picasso.with(this)
                .load(new File(mMovieData.getBackdropPath()))
                .into(backdrop);

        mMovieId = mMovieData.getId();
        apiService = RetrofitClient.getClient().create(ApiInterface.class);

        if (networkConnected()) {

            loadReviews();
            loadTrailers();

        } else {
            Toast.makeText(this, "You can view trailers and reviews when you're online",
                    Toast.LENGTH_SHORT).show();
        }
    }


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


    private boolean networkConnected() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkStatus = connMgr.getActiveNetworkInfo();

        // If network is available then return true, else, false is returned
        return (networkStatus != null && networkStatus.isConnected());
    }
}
