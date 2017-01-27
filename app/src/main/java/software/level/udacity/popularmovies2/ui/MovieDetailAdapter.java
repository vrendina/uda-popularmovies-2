package software.level.udacity.popularmovies2.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

import software.level.udacity.popularmovies2.R;
import software.level.udacity.popularmovies2.api.MovieServiceUtils;
import software.level.udacity.popularmovies2.api.model.MovieDetails;
import software.level.udacity.popularmovies2.api.model.MovieDetailsComposite;
import software.level.udacity.popularmovies2.api.model.MovieReview;
import software.level.udacity.popularmovies2.api.model.MovieTrailer;

public class MovieDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = MovieDetailAdapter.class.getSimpleName();

    private MovieDetailsComposite data;

    private int trailerCount = 0;
    private int reviewCount = 0;

    private MovieTrailerOnClickHandler clickHandler;

    private static final int VIEW_HEADER = 100;
    private static final int VIEW_TRAILER = 101;
    private static final int VIEW_REVIEW = 102;


    public MovieDetailAdapter(MovieTrailerOnClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view;
        switch(viewType) {
            case VIEW_HEADER:
                view = inflater.inflate(R.layout.item_movie_detail_header, parent, false);
                return new MovieDetailHeaderViewHolder(view);

            case VIEW_TRAILER:
                view = inflater.inflate(R.layout.item_movie_trailer, parent, false);
                return new MovieTrailerViewHolder(view);

            case VIEW_REVIEW:
                view = inflater.inflate(R.layout.item_movie_review, parent, false);
                return new MovieReviewViewHolder(view);

            default:
                Log.e(TAG, "onCreateViewHolder: Unknown viewType passed");
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder untypedHolder, int position) {

        if(untypedHolder instanceof MovieDetailHeaderViewHolder) {

            bindHeaderViewHolder((MovieDetailHeaderViewHolder) untypedHolder, data.details);

        } else if(untypedHolder instanceof MovieTrailerViewHolder) {

            int trailerPosition = position - 1;
            bindTrailerViewHolder((MovieTrailerViewHolder) untypedHolder, trailerPosition);

        } else if(untypedHolder instanceof MovieReviewViewHolder) {

            int reviewPosition = position - trailerCount - 1;

            bindReviewViewHolder((MovieReviewViewHolder) untypedHolder, reviewPosition);

        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return VIEW_HEADER;
        } else if(position > 0 && position <= trailerCount) {
            return VIEW_TRAILER;
        }
        return VIEW_REVIEW;
    }

    @Override
    public int getItemCount() {
        if(data == null) {
            return 0;
        }
        return trailerCount + reviewCount + 1;
    }

    public void setMovieData(MovieDetailsComposite data) {
        this.data = data;

        this.trailerCount = data.trailerEnvelope.trailers.size();
        this.reviewCount = data.reviewEnvelope.reviews.size();

        notifyDataSetChanged();
    }

    private void bindHeaderViewHolder(MovieDetailHeaderViewHolder holder, MovieDetails movie) {
        Context context = holder.itemView.getContext();

        // Set the movie title and release year
        holder.title.setText(String.format(context.getString(R.string.details_movie_title),
                movie.title,
                Integer.valueOf(data.details.releaseDate.split("-")[0])));

        // Set the rating
        holder.rating.setText(String.format(Locale.ENGLISH, "%.1f", movie.voteAverage));

        // Set the runtime
        holder.runtime.setText(String.format(context.getString(R.string.details_movie_runtime),
                movie.runtime));

        // Set the overview
        holder.overview.setText(movie.overview);

        // Set the poster image
        String imageUrl = MovieServiceUtils.buildImageURL(movie.posterPath, "w185").toString();

        Picasso.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.poster_placeholder)
                .into(holder.poster);
    }

    private void bindTrailerViewHolder(MovieTrailerViewHolder holder, int trailerPosition) {
        Context context = holder.itemView.getContext();

        try {
            MovieTrailer trailer = data.trailerEnvelope.trailers.get(trailerPosition);

            // Set the title of the trailer
            holder.title.setText(trailer.name);

            // Set the video size (standard definition if less than 720p)
            String size = context.getString(R.string.trailer_high_definition);
            if (trailer.size < 720) {
                size = context.getString(R.string.trailer_standard_definition);
            }

            holder.size.setText(String.format(context.getString(R.string.trailer_size),
                    size, trailer.size));

            holder.itemView.setTag(trailerPosition);

        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "onBindViewHolder: Requested trailer out of array bounds", e);
        }
    }

    private void bindReviewViewHolder(MovieReviewViewHolder holder, int reviewPosition) {
        Context context = holder.itemView.getContext();

        try {
            MovieReview review = data.reviewEnvelope.reviews.get(reviewPosition);

            // Set the author header
            holder.title.setText(String.format(context.getString(R.string.review_title),
                    review.author));

            // Set the content of the review
            holder.review.setText(review.content);

        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "onBindViewHolder: Requested review out of array bounds", e);
        }
    }

    private class MovieDetailHeaderViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView rating;
        final TextView runtime;
        final TextView overview;

        ImageView poster;

        MovieDetailHeaderViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.tv_movie_title);
            rating = (TextView) view.findViewById(R.id.tv_rating);
            runtime = (TextView) view.findViewById(R.id.tv_runtime);
            overview = (TextView) view.findViewById(R.id.tv_overview);

            poster = (ImageView) view.findViewById(R.id.iv_movie_poster);
        }
    }

    private class MovieTrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView title;
        final TextView size;

        MovieTrailerViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.tv_trailer_title);
            size = (TextView) view.findViewById(R.id.tv_trailer_size);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int trailerPosition = (int) view.getTag();
            clickHandler.onTrailerClick(data.trailerEnvelope.trailers.get(trailerPosition));
        }
    }

    private class MovieReviewViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView review;

        MovieReviewViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.tv_review_title);
            review = (TextView) view.findViewById(R.id.tv_review);

        }
    }


    public interface MovieTrailerOnClickHandler {
        void onTrailerClick(MovieTrailer trailer);
    }

}
