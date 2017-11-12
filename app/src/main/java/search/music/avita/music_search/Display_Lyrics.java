package search.music.avita.music_search;

import android.app.ActionBar;
import android.app.ProgressDialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


//Activity used to display lyrics of selected song.
public class Display_Lyrics extends AppCompatActivity {
    //Set up information from Music Object
    ImageView albumCover;
    TextView albumName;
    TextView artistName;
    TextView songName;

    TextView lyrics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display__lyrics);

        //Set up the action bar to display a back button as well as the Lyrics Title Page
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.lyrics_header);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        View v = getSupportActionBar().getCustomView();
        TextView textViewForHeader = (TextView) v.findViewById(R.id.textViewHeader);
        textViewForHeader.setText("Lyrics");
        //Get Music object
        Music music = getIntent().getExtras().getParcelable("album_cover");
        Log.d("Display_Lyrics", music.toString());

        //Set up data from music object
        albumCover = (ImageView) findViewById(R.id.albumCover);
        albumCover.setImageBitmap(music.getAlbumImage().createScaledBitmap(music.getAlbumImage(),350,350 ,true));

        albumName = (TextView) findViewById(R.id.textViewAlbumName);
        albumName.setText(music.getAlbumName());

        artistName = (TextView) findViewById(R.id.artistName);
        artistName.setText(music.getArtistName());

        songName = (TextView) findViewById(R.id.songName);
        songName.setText(music.getTrackName());

        lyrics = (TextView) findViewById(R.id.lyrics);

        //Build uri to request lyrics
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("https")
                .authority(getResources().getString(R.string.lyrics_url))
                .appendPath("api.php")
                .appendQueryParameter("func", "getSong")
                .appendQueryParameter("artist", artistName.getText().toString())
                .appendQueryParameter("song", songName.getText().toString())
                .appendQueryParameter("fmt", "json");
        getLyrics(uriBuilder);


    }
    private void getLyrics(Uri.Builder uriBuilder)
    {
        URL queryURL=null;
        try {
            //Need to surround in try-catch in case URL is malformed
            queryURL = new URL(uriBuilder.toString());
        } catch (MalformedURLException e) {
            //Provide output to user indicating search for lyrics failed.
            Toast.makeText(getApplicationContext(), "Unable to Retrieve Lyrics", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        //Call RetrieveMusicLyrics to retrieve results from query
        new RetrieveMusicLyrics(this).execute(queryURL);
    }
    public void openDrawer(View view) {
        onBackPressed();
    }

    //Helper function to ensure output from call is correct
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    //Create a private class extending AsyncTask to retrieve lyrics;
    private class RetrieveMusicLyrics extends AsyncTask<URL, String, String>
    {

        private Display_Lyrics activity;
        private ProgressDialog dialog;

        public RetrieveMusicLyrics(Display_Lyrics activity)
        {
            this.activity = activity;
            dialog = new ProgressDialog(activity);

        }

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Retrieving Lyrics...");
            this.dialog.setCancelable(false);

            this.dialog.show();
        }

        @Override
        protected String doInBackground(URL... urls) {

            HttpURLConnection httpURLConnection =null;
            try
            {
                URL queryUrl = urls[0];//Getting provided url from query;

                Log.d("Display_Lyrics", queryUrl.toString());
                httpURLConnection = (HttpURLConnection) queryUrl.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                //Check if we are getting a successful response
                if(httpURLConnection.getResponseCode()==200)
                {
                    Log.d("Display_Lyrics", "Successfully able to reach URL");
                    InputStream responseBody = httpURLConnection.getInputStream();
                    InputStreamReader responseBodyReader =
                            new InputStreamReader(responseBody, "UTF-8");


                    //Get JSON from response
                    String json = convertStreamToString(responseBody);
                    Log.d("Display_Lyrics_HTML", json);

                    //Because JSON that retrieved is not in proper format, some work needs to be done
                    //The single quotes need to be replaced with double quotes
                    //The "song = " also needs to be replaced with an empty string
                    json = json.replace("'","\"");
                    json = json.replace("song = ", "");

                    Log.d("Display_Lyrics_NEWJSON", json);

                    //Now that string is in JSONObject form we can convert the string into a JSON object to easily retrieve values.


                    //Also like the iTunes query, it would be wise to create a caching system for the lyrics so some queries can just be found locally
                    //rather than making calls to the api for queries that have been recently made.
                    JSONObject jsonObj = new JSONObject(json);

                    Log.d("Display_Lyrics_Object", jsonObj.toString());


                    //Create object mapper object and map JSON that is retrieved into an Object we created
                    //Called JSON_Lyrics
                    ObjectMapper objectMapper = new ObjectMapper();
                    //If property is unknown, mark it as ok so application does not crash
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    JSON_Lyrics jsonLyrics = objectMapper.readValue(json, JSON_Lyrics.class);

                    //Get the lyrics from the JSON object created by the user.
                    String lyrics = jsonLyrics.getLyrics();


                    return lyrics;
                }
                //Unable to get result
                else
                {
                    //Most likely something went wrong with the connection and seeing the response code can let us know what the error may be.
                    Log.d("Display_Lyrics", Integer.toString(httpURLConnection.getResponseCode()));
                    return null;
                }

            }
            //Something went wrong and we can see what happened by tracing the log and seeing the exception that was thrown.
            catch (Exception e)
            {
                Log.d("Display_Lyrics", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //Check if the dialog is showing. If it is then dismiss it.
            if(dialog.isShowing())
            {
                dialog.dismiss();
            }
            //Check to see if the result is null. If it is not then we know we got some data back and can display it to the user.
            if(result!=null)
            {
                lyrics.setText(result);
            }
            //Something went wrong and we can display a toast to the user detailing this.
            else
            {
                Toast.makeText(getApplicationContext(), "Unable to get lyrics", Toast.LENGTH_LONG).show();
            }
        }
    }
}
