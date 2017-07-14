package anas.online.moviechamp;

import android.app.Application;
import android.content.Context;

public class MovieChamp extends Application {
    private static MovieChamp appInstance;

    public MovieChamp() {
        appInstance = this;
    }

    public static Context getContext() {
        return appInstance;
    }
}
