package com.example.kathleenbenavides.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by kathleenbenavides on 10/26/15.
 */
public class DetailActivity extends AppCompatActivity {

    TextView title;
    ImageView poster;
    TextView release;
    TextView rating;
    TextView overView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        MovieDetailDO movieSelected = intent.getParcelableExtra("movieSelected");

        setContentView(R.layout.detail_view);
        title = (TextView) findViewById(R.id.title);
        poster = (ImageView) findViewById(R.id.poster);
        release = (TextView) findViewById(R.id.release);
        rating = (TextView) findViewById(R.id.rating);
        overView = (TextView) findViewById(R.id.overview);

        title.setText(movieSelected.getOriginal_title());
        Picasso.with(DetailActivity.this).load(movieSelected.getPoster_path()).into(poster);
        release.setText(movieSelected.getRelease_date());
        rating.setText(movieSelected.getVote_average());
        overView.setText(movieSelected.getOverview());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
