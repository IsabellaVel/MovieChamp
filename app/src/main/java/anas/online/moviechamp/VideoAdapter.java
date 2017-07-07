package anas.online.moviechamp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by anas on 07.07.17.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoAdapterViewHolder> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;
    private List<Video> mVideos;
    private int mRowLayout;

    public VideoAdapter(List<Video> videos, int rowLayout, Context context) {
        mVideos = videos;
        mContext = context;
        mRowLayout = rowLayout;
    }

    @Override
    public VideoAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(mContext)
                .inflate(R.layout.item_trailer, parent, false);

        view.setFocusable(true);

        return new VideoAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoAdapterViewHolder holder, int position) {

        Video video = mVideos.get(position);

        String key = video.getKey();

        holder.trailerTitle.setText(key);


    }

    @Override
    public int getItemCount() {
        if (mVideos == null) {
            return -1;
        }
        return mVideos.size();
    }

    public void swapList(List<Video> videos) {
        mVideos = videos;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    class VideoAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView trailerTitle;

        VideoAdapterViewHolder(View view) {
            super(view);
            trailerTitle = (TextView) view.findViewById(R.id.trailer_title);
        }

    }
}
