package com.example.sticklemako.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieGridFragment extends Fragment {
    ImageAdapter mImageAdapter;
    JSONObject queryObject;
    String[] imageUrls = {}, overviewArray = {}, releaseDateArray = {}, titleArray = {}, voteAverageArray = {};

    public static final String MOV_LOG = MovieGridFragment.class.getSimpleName();

    public MovieGridFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateMovieList();
        mImageAdapter = new ImageAdapter(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieList();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateMovieList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View movieRootView = inflater.inflate(R.layout.fragment_main, container, false);
        queryObject = new JSONObject();
        GridView movieGridView = (GridView) movieRootView.findViewById(R.id.movieGrid);
        movieGridView.setAdapter(mImageAdapter);

        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent movieDetailIntent = new Intent(getActivity(), MovieDetailActivity.class);
                movieDetailIntent.putExtra("urls", imageUrls[position]);
                movieDetailIntent.putExtra("overview", overviewArray[position]);
                movieDetailIntent.putExtra("release_date", releaseDateArray[position]);
                movieDetailIntent.putExtra("title", titleArray[position]);
                movieDetailIntent.putExtra("vote_average", voteAverageArray[position]);
                startActivity(movieDetailIntent);
            }
        });

        return movieRootView;
    }

    public void updateMovieList() {
        GetMovieListTask listTask = new GetMovieListTask();
        String sorting = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.sorting_key),getString(R.string.default_sorting_value));
        listTask.execute(sorting);
    }

    public void getMovieData(JSONObject object) {
        try {
            JSONArray jsonArray = object.getJSONArray("results");
            imageUrls = new String[jsonArray.length()];
            overviewArray = new String [jsonArray.length()];
            releaseDateArray = new String[jsonArray.length()];
            titleArray = new String[jsonArray.length()];
            voteAverageArray = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length();i++) {
                JSONObject temp = jsonArray.getJSONObject(i);
                imageUrls[i] = temp.getString("poster_path");
                overviewArray[i] = temp.getString("overview");
                releaseDateArray[i] = temp.getString("release_date");
                titleArray[i] = temp.getString("title");
                voteAverageArray[i] = temp.getString("vote_average");
            }
        } catch (Exception e) {
            Log.e(MOV_LOG,"Error: ", e);
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private Context imageContext;

        public int getCount() {
            return imageUrls.length;
        }

        public ImageAdapter(Context c) {
            imageContext = c;
        }

        public String getItem(int position) {
            return imageUrls[position];
        }

        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = (ImageView) convertView;
            int width = parent.getWidth();
            if (imageView == null) {
                imageView = new ImageView(imageContext);
            }
            String url = "http://image.tmdb.org/t/p/w185/" + getItem(position);
            Log.v(MOV_LOG,url);
            Picasso.with(imageContext)
                    .load(url)
                    .resize(width/3, 2*width/3)
                    .centerInside()
                    .into(imageView);
            return imageView;
        }
    }

    public class GetMovieListTask extends AsyncTask<String, Void, JSONObject> {

        private final String TASK_CLASS = GetMovieListTask.class.getSimpleName();

        @Override
        protected JSONObject doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String api_key = "";
            String sort_by = params[0];

            Uri.Builder movieUriBuilder = new Uri.Builder();
            movieUriBuilder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter("sort_by",sort_by)
                    .appendQueryParameter("api_key",api_key);

            Log.v(TASK_CLASS,movieUriBuilder.build().toString());

            try {
                URL url = new URL(movieUriBuilder.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream movieInputStream = urlConnection.getInputStream();
                StringBuffer movieBuffer = new StringBuffer();

                if(movieInputStream == null) { return null; }
                reader = new BufferedReader(new InputStreamReader(movieInputStream));

                //Pushing stream data into buffer
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    movieBuffer.append(inputLine + '\n');
                }

                if(movieBuffer.length() == 0) { return null; }

                return new JSONObject(movieBuffer.toString());
            } catch (Exception err) {
                Log.e(TASK_CLASS,"Error: ",err);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(TASK_CLASS, "Error closing stream", e);
                    }
                }
            }
            Log.v(TASK_CLASS, "Output URi:" + movieUriBuilder.toString());

            return null;
        }

        @Override
        protected void onPostExecute (JSONObject result) {
            super.onPostExecute(result);
            if(result != null) {
                getMovieData(result);
                queryObject = result;
            }
        }
    }
}
