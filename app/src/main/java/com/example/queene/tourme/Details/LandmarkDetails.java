package com.example.queene.tourme.Details;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.queene.tourme.R;
import com.google.android.gms.plus.PlusShare;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Queene on 26/01/2016.
 */
public class LandmarkDetails extends Activity {
    //WebView LandmDetails;
    double lat,lng;
    String name, formatted_address, website;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmark_details);

        // Getting reference to WebView ( wv_place_details ) of the layout activity_place_details
       // LandmDetails = (WebView) findViewById(R.id.landmark_details);

        //LandmDetails.getSettings().setUseWideViewPort(false);

        //street view
        Button street = (Button) findViewById(R.id.streetview);
        street.setOnClickListener(new StreetV());

        Button shareButton  = (Button) findViewById(R.id.share_button);
        shareButton.setOnClickListener(new ShareG());

        // Getting place reference from the map
        String reference = getIntent().getStringExtra("reference");

        StringBuilder urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        urlString.append("reference="+reference);
        urlString.append("&sensor=true");
        urlString.append("&key=AIzaSyBVc4wVf-geMQBwcoD8zBTisSLil3i6SUc");

        // Creating a new non-ui thread task to download Google place details
        DetailsTask detailsTask = new DetailsTask();

        // Invokes the "doInBackground()" method of the class PlaceTask
        detailsTask.execute(urlString.toString());


    };

    /** A method to download json data from url */
    private String downloadUrl(String theUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        BufferedReader bReader;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(theUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            bReader = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sBuffer = new StringBuffer();

            String line = "";
            while( ( line = bReader.readLine()) != null){
                sBuffer.append(line);
            }

            data = sBuffer.toString();
            bReader.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    /** A class, to download Google Place Details */
    private class DetailsTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();
            // Start parsing the Google place details in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Place Details in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, HashMap<String,String>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected HashMap<String, String> doInBackground(String... jsonData) {

            HashMap<String, String> landDetails = null;
            LDetailParser lDetailsParser = new LDetailParser();

            try {
                jObject = new JSONObject(jsonData[0]);

                // Start parsing Google place details in JSON format
                landDetails = lDetailsParser.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return landDetails;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(HashMap<String, String> landDetails) {

            //location of the place
            lat = Double.parseDouble(landDetails.get("lat"));
            lng = Double.parseDouble(landDetails.get("lng"));
           // LatLng latLng = new LatLng(lat, lng);


            name = landDetails.get("name");
            formatted_address = landDetails.get("formatted_address");
            website = landDetails.get("website");
            String rating = landDetails.get("rating");
            String international_phone_number = landDetails.get("international_phone_number");


            TextView lm_name = (TextView) findViewById(R.id.name);
            TextView lm_address = (TextView) findViewById(R.id.address);
            TextView lm_website = (TextView) findViewById(R.id.website);
            TextView lm_rating = (TextView) findViewById(R.id.rating);
            TextView lm_phone = (TextView) findViewById(R.id.phone);


            lm_name.setText(name);
            lm_address.setText(formatted_address);
            lm_website.setText(website);
            lm_rating.setText(rating);
            lm_phone.setText(international_phone_number);

        }


    }

    //Street View
    public class StreetV implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LandmarkDetails.this, StreetViewPanoramaViewActivity.class);
            Bundle b = new Bundle();
            b.putDouble("latitude", lat);
            b.putDouble("longtitude", lng);
            intent.putExtras(b);
            startActivity(intent);
        }
    }

    //share
    public class ShareG implements View.OnClickListener
    {
        @Override
        public void onClick(View v) {
            // Launch the Google+ share dialog with attribution to your app.
            Intent shareIntent = new PlusShare.Builder(LandmarkDetails.this)
                    .setType("text/plain")
                    .setText(" checked in - " + name + "," + formatted_address + ". More info - visit " + website)
                    .getIntent();

            startActivityForResult(shareIntent, 0);
        }
    }

}
