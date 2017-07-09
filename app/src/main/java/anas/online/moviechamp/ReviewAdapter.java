package anas.online.moviechamp;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {
    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;
    private List<Review> mReviews;
    private int mRowLayout;

    public ReviewAdapter(List<Review> reviews, int rowLayout, Context context) {
        mReviews = reviews;
        mContext = context;
        mRowLayout = rowLayout;
    }

    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_review, parent, false);

        view.setFocusable(true);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewAdapterViewHolder holder, int position) {

        Review review = mReviews.get(position);

        holder.reviewContent.setText(review.getContent());
        holder.reviewAuthor.setText(review.getAuthor());
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) {
            return -1;
        }
        return mReviews.size();
    }

    public void swapList(List<Review> reviews) {
        mReviews = reviews;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView reviewContent;
        final TextView reviewAuthor;

        ReviewAdapterViewHolder(View view) {
            super(view);
            reviewContent = (TextView) view.findViewById(R.id.review_content);
            reviewAuthor = (TextView) view.findViewById(R.id.review_author);
        }

    }
}
