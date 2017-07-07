package anas.online.moviechamp.rest;

import anas.online.moviechamp.MovieResponse;
import anas.online.moviechamp.Review;
import anas.online.moviechamp.Video;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Define the API endpoints. This interface contains methods we are going to use to execute HTTP
 * requests
 */

public interface ApiInterface {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/{id}")
    Call<MovieResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<Review> getMovieReviews(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<Video> getMovieVideos(@Path("id") int id, @Query("api_key") String apiKey);
}
