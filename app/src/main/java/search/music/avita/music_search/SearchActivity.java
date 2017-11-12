package search.music.avita.music_search;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    //Adapter for listview which will display results from iTunes
    MusicBaseAdapter musicBaseAdapter;
    //Arraylist holding all of Music objects retrieved from iTunes
    ArrayList<Music> musicArrayList;
    //ListView displaying results from ListView
    ListView queryResults;
    //TextView explaining to user current state of the search
    TextView searchTextView;

    String TAG = "RetrieveMusic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Change title in the action bar
        getSupportActionBar().setTitle(getResources().getString(R.string.supporbar_title));

        //Initialize the arraylist holding the music objects;
        musicArrayList = new ArrayList<>();
        //Get the listview displaying the results
        queryResults = (ListView) findViewById(R.id.queryResults);

        //Set up the baseadapter
        musicBaseAdapter = new MusicBaseAdapter(this,musicArrayList);
        queryResults.setAdapter(musicBaseAdapter);

        //Set up textview displaying results to user.
        searchTextView = (TextView) findViewById(R.id.displayNoResults);
        searchTextView.setText("Go ahead and search for a song, album or artist");


        //Create onItemClickHandler for ListView
        queryResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent mainActivity = new Intent(getApplicationContext(), Display_Lyrics.class);
                //Pass the Music object to the Lyrics activity
                mainActivity.putExtra("album_cover", musicArrayList.get(position));
                startActivity(mainActivity);
            }
        });





    }

    //Override onCreateOptionsMenu to display our search bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem myActionMenuItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                myActionMenuItem.collapseActionView();
                //Check for the query, see if it matches user input
                Log.d(TAG, query);

                //Build URL using URI.Builder
                Uri.Builder uriBuilder = new Uri.Builder();
                uriBuilder.scheme("https")
                .authority(getResources().getString(R.string.apple_url_search))
                .appendPath("search")
                .appendQueryParameter("term", query);
                //Now create URL
                URL queryURL=null;
                try {
                    //Need to surround in try-catch in case URL is malformed
                    queryURL = new URL(uriBuilder.toString());
                } catch (MalformedURLException e) {
                    //Provide output to user indicating search failed.
                    Toast.makeText(getApplicationContext(), "Unable to search", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                //Call RetrieveMusicSearchResults to retrieve results from query
                new RetrieveMusicSearchResults(SearchActivity.this).execute(queryURL);
                return false;
            }
            //Currently no use for this, but perhaps a update that could be made is to cache previous user inputs
            //and as the user types in a new input, if the input matches the current query text they have entered,
            //a dropdown menu would appear below the text showing the previous search that had been made.
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        return true;

    }

    //Create a private class extending AsyncTask to retrieve results from iTunes;
    private class RetrieveMusicSearchResults extends AsyncTask<URL, ArrayList<Music>, ArrayList<Music>>
    {

        private SearchActivity activity;
        private ProgressDialog dialog;
        //Constructor for Class allowing for a progress dialog to be shown as the user waits for results
        public RetrieveMusicSearchResults(SearchActivity activity)
        {
            this.activity = activity;
            dialog = new ProgressDialog(activity);

        }
        //This will run on the UI thread
        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Retrieving Results...");
            this.dialog.setCancelable(false);

            this.dialog.show();
        }
        //After onPreExecute finishes, doInBackground will run on the background thread and will call iTunes search to return the Music Objects
        @Override
        protected ArrayList<Music> doInBackground(URL... urls) {

            HttpURLConnection httpURLConnection =null;
            try
            {
                ImageLoader imageLoader = ImageLoader.getInstance(); // Create singleton instance here to be used to generate bitmaps for album covers
                URL queryUrl = urls[0];//Getting provided url from query;

                Log.d(TAG, queryUrl.toString());
                //Set up the connection
                httpURLConnection = (HttpURLConnection) queryUrl.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                //Check if we are getting a successful response
                if(httpURLConnection.getResponseCode()==200)
                {
                    Log.d(TAG, "Successfully able to reach URL");
                    InputStream responseBody = httpURLConnection.getInputStream();
                    InputStreamReader responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");

                    //Create object mapper object and map JSON that is retrieved into an Arraylist of POJO we created called Music_Results
                    ObjectMapper objectMapper = new ObjectMapper();
                    //If property is unknown, mark it as ok so application does not crash
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    Music_Results musicResultFromQuery = objectMapper.readValue(responseBodyReader, Music_Results.class);

                    //Now that we have musicResult object, query through the result array to get information
                    Results[] musicResultArray = musicResultFromQuery.getResults();

                    //Create arraylist to store all Music objects;
                    ArrayList<Music> storeMusicObjects = new ArrayList<>();

                    //Loop through musicResultArray to create Music objects which will then be
                    //used in ListView to display results to user
                    for(Results results:musicResultArray)
                    {
                        String trackName = results.getTrackName();
                        String artistName = results.getArtistName();
                        String albumName = results.getCollectionName();
                        String albumImage = results.getArtworkUrl100();

                        //Need to convert albumImage into a bitmap to store into imageview
                        /*
                        One thing that, if given more time, would be to implement some sort of caching structure
                        when submitting the user query. Not only to store results from a particular query but also hold the bitmaps
                        for Music objects so these do not have to downloaded every single time the server is being called. This would save
                        some time as currently every time a call is being made, a conversion into a bitmap is being made for all the album cover images
                         */

                        URL url = new URL(albumImage);
                        Bitmap bitmapAlbumImage = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                        //Create Music object which holds the track name, artist name, album name and image bitmap which is the album cover art.
                        Music music = new Music(trackName,artistName,albumName,bitmapAlbumImage);

                        //Store music object in arraylist holding all music objects returned from iTunes
                        storeMusicObjects.add(music);

                    }
                    Log.d(TAG, storeMusicObjects.toString());
                    return storeMusicObjects;
                }
                //Unable to get result
                else
                {
                    Log.d(TAG, Integer.toString(httpURLConnection.getResponseCode()));
                    return null;
                }
            //Something else went wrong and we can see the error through the log.
            }catch (Exception e)
            {
                Log.d(TAG, e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Music> musicList) {
            //First check if the dialog is showing and if it is close it.
            if(dialog.isShowing())
            {
                dialog.dismiss();
            }
            //Now we can check the result we got from onPreExecute
            //If the result is not null then we know we successfully got some information back from the call
            if(musicList!=null)
            {
                //If the size of the arraylist is 0 then we know no results were found for the query and display this information to the user.
                if(musicList.size()==0)
                {
                    searchTextView.setVisibility(View.VISIBLE);
                    searchTextView.setText("No Results Found. Please Try Another Query");
                    queryResults.setVisibility(View.INVISIBLE);
                }
                //The expected case. Remove the textview and notify the adapter that we have new values to display to the user.
                else {
                    queryResults.setVisibility(View.VISIBLE);
                    searchTextView.setVisibility(View.INVISIBLE);
                    musicArrayList.clear();
                    musicArrayList.addAll(musicList);
                    musicBaseAdapter.notifyDataSetChanged();
                }
            }
            //Something went wrong and we can display this information to the user with a Toast message saying we could not retrieve information
            else
            {
                Toast.makeText(getApplicationContext(), "Unable to get results", Toast.LENGTH_LONG).show();
            }
        }
    }

}
