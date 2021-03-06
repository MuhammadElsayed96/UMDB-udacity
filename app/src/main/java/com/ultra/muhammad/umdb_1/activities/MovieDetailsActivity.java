package com.ultra.muhammad.umdb_1.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.ultra.muhammad.umdb_1.R;
import com.ultra.muhammad.umdb_1.database.AppDatabase;
import com.ultra.muhammad.umdb_1.database.MovieEntry;
import com.ultra.muhammad.umdb_1.models.Genre;
import com.ultra.muhammad.umdb_1.models.Movie;
import com.ultra.muhammad.umdb_1.models.MovieDetails;
import com.ultra.muhammad.umdb_1.models.MovieReviews;
import com.ultra.muhammad.umdb_1.models.MovieTrailer;
import com.ultra.muhammad.umdb_1.movie_utils.Utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.ultra.muhammad.umdb_1.movie_utils.Constants.UMDB_API_KEY;
import static com.ultra.muhammad.umdb_1.network.RetrofitClientInstance.BACKGROUND_BASE_URL;
import static com.ultra.muhammad.umdb_1.network.RetrofitClientInstance.POSTER_BASE_URL;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    @BindView(R.id.details_movie_poster_img)
    ImageView mPosterImage;
    @BindView(R.id.details_movie_background_img)
    ImageView mBackgroundImage;
    @BindView(R.id.details_movie_name)
    TextView mTitle;
    @BindView(R.id.details_movie_rating)
    TextView mRating;
    @BindView(R.id.production_year_tv)
    TextView mYear;
    @BindView(R.id.movie_type_tv)
    TextView mType;
    @BindView(R.id.overview_tv)
    TextView mOverview;
    @BindView(R.id.activity_movie_details_layout)
    ConstraintLayout mDetailsActivity;
    @BindView(R.id.loading_progress_bar)
    ProgressBar mLoadingProgressBar;
    @BindView(R.id.trailers_layout)
    ConstraintLayout mTrailersConstraintLayout;
    @BindView(R.id.see_all_trailers_tv)
    TextView mSeeAllTrailersTv;
    @BindView(R.id.reviews_layout)
    ConstraintLayout mReviewsConstraintLayout;
    @BindView(R.id.see_all_tv)
    TextView mSeeAllReviewsTv;
    @BindView(R.id.add_to_favorite_btn)
    Button mAddToFavorite;

    private AppDatabase mDb;

    final Map<String, Object> options = new HashMap<>();
    Movie movie = null;
    boolean bool, f1 = false;
    private String title, poster, background, productionYear, rate, genre, year, movieId, overview;
    boolean mIsFavorite = false;

    private static String formatTime(Context context, String tripTime) throws ParseException {
        Log.d(TAG, "formatTime() has been instantiated");
        DateFormat formatter
                = new SimpleDateFormat(context.getString(R.string.date_pattern));
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = originalFormat.parse(tripTime);
        return formatter.format(date);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);
        Log.d(TAG, "onCreate() has been instantiated");

        mDb = AppDatabase.getsInstance(getApplicationContext());

        showProgressBar();

        Intent intent = getIntent();
        if (intent != null)
            if (intent.hasExtra("selected_movie"))
                movie = (Movie) intent.getSerializableExtra("selected_movie");
            else if (intent.hasExtra("selected_upcoming_movie"))
                movie = (Movie) intent.getSerializableExtra("selected_upcoming_movie");
        title = movie.getTitle();
        poster = movie.getPosterPath();
        background = movie.getBackdropPath();
        productionYear = movie.getReleaseDate();
        rate = String.valueOf(movie.getVoteAverage());
        overview = movie.getOverview();
        movieId = String.valueOf(movie.getId());

        boolean alreadyInTheDatabase = movieAlreadyInTheDatabase(movieId);

        if (alreadyInTheDatabase) {
            Log.d(TAG, "Movie Already in the Database");
            mAddToFavorite.setBackground(getResources().getDrawable(R.drawable.transparent_button));
            mAddToFavorite.setText(getResources().getString(R.string.remove_from_favorite));
            mIsFavorite = true;
        }

        try {
            year = formatTime(getApplicationContext(), movie.getReleaseDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        getExtraMovieDetailsFromInternet(movieId);

    }

    private void getExtraMovieDetailsFromInternet(String movieId) {
        Log.d(TAG, "getExtraMovieDetailsFromInternet() has been instantiated");
        bool = false;
        options.put("api_key", UMDB_API_KEY);
        Utils.getMovieDetails(movieId, options, new Utils.retrofitCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                // Do nothing
            }

            @Override
            public void onSuccess(MovieDetails movieDetails) {
                Log.i(TAG, movieDetails.toString());
                f1 = true;
                genre = getMovieGenres(movieDetails.getGenres());
                hideProgressBar();
            }

            @Override
            public void onFailure(Throwable t) {
                // Do nothing
            }

            @Override
            public void onSuccess(MovieTrailer movieTrailer) {
                // Do nothing
            }

            @Override
            public void onSuccess(MovieReviews movieReviews) {
                // Do nothing
            }
        });
    }

    private void fillViewsWithData() {
        Log.d(TAG, "fillViewsWithData() has been instantiated");
        Picasso.get().load(POSTER_BASE_URL + poster).placeholder(R.drawable.no_image).error(R.drawable.no_image).fit().centerInside().into(mPosterImage);
        Picasso.get().load(BACKGROUND_BASE_URL + background).placeholder(R.drawable.no_image).error(R.drawable.no_image).fit().centerInside().into(mBackgroundImage);
        mTitle.setText(title);
        mRating.setText(rate);
        mYear.setText(year);
        mType.setText(genre);
        mOverview.setText(overview);
    }

    private String getMovieGenres(List<Genre> genres) {
        Log.d(TAG, "getMovieGenres() has been instantiated");
        StringBuilder movieGenre = new StringBuilder();

        for (int i = 0; i < genres.size() && i < 3; i++) {
            movieGenre.append(genres.get(i).getName());
            movieGenre.append(", ");
        }

        movieGenre.deleteCharAt(movieGenre.length() - 1);
        movieGenre.deleteCharAt(movieGenre.length() - 1);
        return movieGenre.toString();
    }

    private void showProgressBar() {
        Log.d(TAG, "showProgressBar() has been instantiated");
        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mDetailsActivity.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        Log.d(TAG, "hideProgressBar() has been instantiated");
        bool = f1;
        if (bool) {
            mLoadingProgressBar.setVisibility(View.GONE);
            mDetailsActivity.setVisibility(View.VISIBLE);
            fillViewsWithData();
        }
    }


    @OnClick({R.id.trailers_layout, R.id.see_all_trailers_tv})
    public void handleTrailersClick() {
        Log.i(TAG, "User Trailer::has been clicked");
        Intent intent = new Intent(getApplicationContext(), MovieTrailersActivity.class);
        intent.putExtra("movie_id", movieId);
        Log.i(TAG, "Movie ID --> " + movieId);
        startActivity(intent);
    }

    @OnClick({R.id.reviews_layout, R.id.see_all_tv})
    public void handleReviewsClick() {
        Log.i(TAG, "User Reviews view::has been clicked");
        Intent intent = new Intent(getApplicationContext(), UserReviewsActivity.class);
        intent.putExtra("movie_id", movieId);
        Log.i(TAG, "Movie ID --> " + movieId);
        startActivity(intent);
    }

    @OnClick(R.id.add_to_favorite_btn)
    public void handleFavoriteMovie(Button b) {
        if (mIsFavorite) {
            b.setBackground(getResources().getDrawable(R.drawable.blue_trading_button));
            mAddToFavorite.setText(getResources().getString(R.string.add_to_favorite));
            MovieEntry movieEntry = new MovieEntry(movieId, title, Double.parseDouble(rate), overview, year, genre);
            mDb.movieDao().deleteMovie(movieEntry);
            mIsFavorite = false;
        } else {
            b.setBackground(getResources().getDrawable(R.drawable.transparent_button));
            mAddToFavorite.setText(getResources().getString(R.string.remove_from_favorite));
            // Get movie details {id, name, rate, type, date, overview}
            MovieEntry movieEntry = new MovieEntry(movieId, title, Double.parseDouble(rate), overview, year, genre);
            mDb.movieDao().insertMovie(movieEntry);
            mIsFavorite = true;
        }
    }


    /**
     * I only use this method to check if the movie is in the database or not
     * I don't need to use LiveData or ViewModel to observe any changes to anything...
     * That's why I've allowed allowMainThreadQueries() method in the AppDatabase class/getInstance method
     */
    private boolean movieAlreadyInTheDatabase(final String movieId) {
        Log.d(TAG, "getMovieGenres() has been instantiated");

        MovieEntry movieEntry = mDb.movieDao().loadMovieById(movieId);
        return movieEntry != null;
    }
}
