package anas.online.moviechamp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DetailActivity extends AppCompatActivity {

    private static final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private static final String POSTER_SIZE = "w185";
    private static final String BACKDROP_SIZE = "w780";

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        Movie mMovieData = getIntent().getExtras().getParcelable("EXTRA_MOVIE");

        title.setText(mMovieData.getTitle());
        plot.setText(mMovieData.getOverview());
        releaseDate.setText(mMovieData.getReleaseDate());
        rating.setText(mMovieData.getVoteAverage().toString());

        String fullBackdropPath = BASE_IMAGE_URL + BACKDROP_SIZE + mMovieData.getBackdropPath();
        Picasso.with(this).load(fullBackdropPath).into(backdrop);

        // Get poster path and load the image with Picasso
        String fullPosterPath = BASE_IMAGE_URL + POSTER_SIZE + mMovieData.getPosterPath();
        Picasso.with(this).load(fullPosterPath).into(poster);

    }
}
