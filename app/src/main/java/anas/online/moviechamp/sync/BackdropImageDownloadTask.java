package anas.online.moviechamp.sync;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import anas.online.moviechamp.MovieChamp;

public class BackdropImageDownloadTask extends AsyncTask<String, Void, String> {

    private static final String LOG_TAG = BackdropImageDownloadTask.class.getSimpleName();
    private static Context context = MovieChamp.getContext();

    public BackdropImageDownloadTask() {

    }

    @Override
    protected String doInBackground(String... urls) {
        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();

        if (urls.length == 0) {
            return null;
        }

        String mExternalBackdropPath = urls[0];

        Log.v(LOG_TAG, "path = " + mExternalBackdropPath);

        try {

            URL url = new URL(mExternalBackdropPath);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            // Give every file a different file name, according to their pass-in url string.
            String[] parts = mExternalBackdropPath.split("/");
            String filename = parts[6];
            Log.i(LOG_TAG, "filename: " + filename);

            File file = new File(BackdropImageDownloadTask.context.getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Backdrops/" + filename);

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                boolean fileCreated = file.createNewFile();
                Log.i(LOG_TAG, "creating new file: " + file.getAbsolutePath() + ", result: " + fileCreated);
            } else {
                Log.i(LOG_TAG, "File already exists: " + file.getAbsolutePath());
            }

            FileOutputStream fileOutput = new FileOutputStream(file);
            InputStream inputStream = urlConnection.getInputStream();
            int totalSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            byte[] buffer = new byte[1024];
            int bufferLength;
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                fileOutput.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                Log.i(LOG_TAG, "Progress: downloaded size "
                        + downloadedSize + "Total size: "
                        + totalSize);
            }
            fileOutput.close();
            if (downloadedSize == totalSize) {
                filepath = file.getPath();
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage());
        } catch (IOException e) {
            filepath = null;
            Log.e(LOG_TAG, e.getMessage());
        }
        Log.i(LOG_TAG, "File path: " + filepath);
        return filepath;

    }
}
