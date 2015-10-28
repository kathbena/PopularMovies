package com.example.kathleenbenavides.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    GridView gridView;
    private MovieAdapter myAdapter = new MovieAdapter(MainActivity.this, null);
    private String posterPath;
    private String requestURL;
    ArrayList<MovieDetailDO> details = new ArrayList<MovieDetailDO>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.gridView);

        //download data from below url
        new FetchMoviesTask().execute();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MovieDetailDO movieSelected = details.get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("movieSelected", movieSelected);
                startActivity(intent);
            }
        });
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

        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_sortpopular:
                details.clear();
                myAdapter.notifyDataSetChanged();
                requestURL = getString(R.string.popular_url);
                new FetchMoviesTask().execute(requestURL);
                return true;
            case R.id.action_sortrated:
                details.clear();
                myAdapter.notifyDataSetChanged();
                requestURL = getString(R.string.top_rate_url);
                new FetchMoviesTask().execute(requestURL);
                return true;
            default:
             return super.onOptionsItemSelected(item);
        }

    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<MovieDetailDO>>
    {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        /**
         * Take the String representing the result of movies in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         */
        private ArrayList<MovieDetailDO> getMovieDataFromJson(String movieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String results = "results";
            final String MOVIEID = "id";
            final String ORIGINAL_TITLE = "original_title";
            final String OVERVIEW = "overview";
            final String RELEASE_DATE = "release_date";
            final String POSTER_PATH = "poster_path";
            final String VOTE_AVERAGE = "vote_average";


            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(results);

            for (int i = 0; i < movieArray.length(); i++) {
                MovieDetailDO detail = new MovieDetailDO();

                // Set details for each movie and add to arraylist
                JSONObject movie = movieArray.getJSONObject(i);
                detail.setId(movie.getString(MOVIEID));
                detail.setOriginal_title(movie.getString(ORIGINAL_TITLE));
                detail.setOverview(movie.getString(OVERVIEW));
                detail.setRelease_date(movie.getString(RELEASE_DATE));
                // Get the poster url and construct it
                String poster = movie.getString(POSTER_PATH);
                detail.setPoster_path(constructPosterURL(poster));
                detail.setVote_average(movie.getString(VOTE_AVERAGE));

                details.add(detail);
            }
            return details;
        }

        @Override
        protected ArrayList<MovieDetailDO> doInBackground(String... params) {

            if (params.length == 0) {
                requestURL = getString(R.string.movie_main_url);
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;
            try {
                // Construct the URL for the MovieDB query
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(requestURL).buildUpon()
                        .appendQueryParameter(API_KEY, getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to MovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    Log.v(LOG_TAG, "InputStream is null");
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    Log.v(LOG_TAG, "Buffer length is 0");
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
                Log. v(LOG_TAG, "Movie string: " + movieJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieDataFromJson(movieJsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<MovieDetailDO> result) {
            myAdapter = new MovieAdapter(MainActivity.this, result);
            gridView.setAdapter(myAdapter);
        }
    }

    //This function constructs the poster url and size for requesting in Picasso
    public String constructPosterURL(String poster) {
        String baseURL = getString(R.string.base_poster_url);
        String posterSize = "w500";
        posterPath = baseURL + posterSize + poster;

        return posterPath;
    }
}
