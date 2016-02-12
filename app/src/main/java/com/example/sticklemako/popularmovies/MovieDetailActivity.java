package com.example.sticklemako.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends AppCompatActivity {

    protected TextView titleView, overView, releaseDateView, voteView;
    protected ImageView thumbView;
    protected GridLayout gridLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Text Views intilization
        titleView = (TextView) findViewById(R.id.movieTitle);
        overView = (TextView) findViewById(R.id.movieOverview);
        releaseDateView = (TextView) findViewById(R.id.relDate);
        voteView = (TextView) findViewById(R.id.rating);
        thumbView = (ImageView) findViewById(R.id.moviePosterThumbnail);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent parentIntent = getIntent();
        titleView.setText(parentIntent.getStringExtra("title"));
        overView.setText(parentIntent.getStringExtra("overview"));
        releaseDateView.setText(parentIntent.getStringExtra("release_date"));
        voteView.setText(parentIntent.getStringExtra("vote_average"));

        Picasso.with(thumbView.getContext())
                .load("http://image.tmdb.org/t/p/w185/" + parentIntent.getStringExtra("urls"))
                .into(thumbView);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_movie_detail, container, false);

        return rootView;
    }
}
