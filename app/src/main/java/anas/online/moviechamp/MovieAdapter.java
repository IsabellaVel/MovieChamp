package anas.online.moviechamp;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

import static anas.online.moviechamp.MainActivity.INDEX_MOVIE_EXTERNAL_STORAGE_POSTER_PATH;

class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;
    private List<Movie> mMovies;
    private MovieAdapterOnClickHandler mClickHandler;
    private int mRowLayout;

    private Cursor mCursor;

    /*We need to load data from two different sources. We have two cases: We either load live data
    from the API for the Popular and Top Rated movies or we load data from the
    database (cursor) for the Favorites. This boolean is used to tell our adapter from which source does it have
    to load data*/
    private boolean mLoadfromCursor;


     /*
      * Below, we've defined an interface to handle clicks on items within this Adapter. In the
      * constructor of our MovieAdapter, we receive an instance of a class that has implemented
      * said interface. We store that instance in this variable to call the onClick method whenever
      * an item is clicked in the list.
      */

    public MovieAdapter(List<Movie> movies, int rowLayout, Context context,
                        MovieAdapterOnClickHandler clickHandler) {
        mMovies = movies;
        mContext = context;
        mRowLayout = rowLayout;
        mClickHandler = clickHandler;
    }

    public MovieAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_movie, parent, false);

        view.setFocusable(true);

        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {

        if (!mLoadfromCursor) {

            Movie movie = mMovies.get(position);

            String posterPath = movie.getPosterPath();

            String fullImagePath = "http://image.tmdb.org/t/p/w342" + posterPath;

            Picasso.with(mContext).load(fullImagePath).into(holder.poster);
        } else {
            // We are loading data from the cursor (database)
            mCursor.moveToPosition(position);

            // Read poster path from the cursor
            String localPosterPath = mCursor.getString(INDEX_MOVIE_EXTERNAL_STORAGE_POSTER_PATH);
            Picasso.with(mContext)
                    .load(new File(localPosterPath))
                    .into(holder.poster);
        }

    }

    @Override
    public int getItemCount() {
        if (!mLoadfromCursor) {
            if (mMovies == null) {
                return -1;
            }
            return mMovies.size();
        } else {
            if (mCursor == null) {
                return 0;
            }
            return mCursor.getCount();
        }
    }

    public void swapList(List<Movie> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    void swapCursor(Cursor newCursor) {
        mLoadfromCursor = true;
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView poster;

        MovieAdapterViewHolder(View view) {
            super(view);

            poster = (ImageView) view.findViewById(R.id.poster_image);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie movie = mMovies.get(adapterPosition);
            mClickHandler.onClick(movie);
        }

    }

}
