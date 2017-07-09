package anas.online.moviechamp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Video {

    @SerializedName("results")
    private List<Video> videosList;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("key")
    @Expose
    private String key;

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public List<Video> getVideosList() {
        return videosList;
    }

}