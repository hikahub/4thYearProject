package com.example.queene.tourme;

import android.app.Dialog;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.queene.tourme.Details.LandmarkDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.PlusShare;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

//link to details

public class LandmarksActivity extends FragmentActivity implements LocationListener{
    private String API_KEY = "AIzaSyBVc4wVf-geMQBwcoD8zBTisSLil3i6SUc";
    private GoogleMap mMap;
    private Spinner landmarkName;

    double mLatitude=0, mLongtitude=0;
    String[] landmarkTypes={"establishment","park","art_gallery","church","museum","zoo","stadium"};
    String[] landmarkCategory=null;

    HashMap<String,String> markerLink = new HashMap<String,String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landmarks);

        landmarkCategory = getResources().getStringArray(R.array.landmark_name);
        // Creating an array adapter with an array of Landmarks types to populate the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, landmarkCategory);

        // Getting reference to the Spinner
        landmarkName = (Spinner) findViewById(R.id.landmarks_category);
        landmarkName.setAdapter(adapter);



        // Getting reference to Find Button
        //Button btnFind = ( Button ) findViewById(R.id.btn_find);


        // Getting Google Play availability status
       int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if(status!=ConnectionResult.SUCCESS){ // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();

        }else { // Google Play Services are available
            //Google Maps
            SupportMapFragment fragment = ( SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = fragment.getMap();
            // Enabling phone location in Google Map
            mMap.setMyLocationEnabled(true);



            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            // Getting Current Location From GPS
            Location location = locationManager.getLastKnownLocation(provider);

            if(location!=null){
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 20000, 10, this);

            //landmarkdetails
            mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker arg0) {
                    Intent intent = new Intent(getBaseContext(), LandmarkDetails.class);
                    String reference = markerLink.get(arg0.getId());
                    intent.putExtra("reference", reference);

                    // Starting the Place Details Activity
                    startActivity(intent);
                }
            });

            landmarkName.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int selectedPosition = landmarkName.getSelectedItemPosition();
                    String category = landmarkTypes[selectedPosition];

                    StringBuilder urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                    urlString.append("location="+mLatitude+","+mLongtitude);
                    urlString.append("&radius=10000");
                    urlString.append("&types=" + category);
                    urlString.append("&sensor=true");
                    urlString.append("&key="+ API_KEY);

                    // Creating a new non-ui thread task to download Google place json data
                    LandmarksTask LTask = new LandmarksTask();

                    // Invokes the "doInBackground()" method of the class PlaceTask
                    LTask.execute(urlString.toString());

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            // Setting click event lister for the find button
//            btnFind.setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    int selectedPosition = landmarkName.getSelectedItemPosition();
//                    String category = landmarkTypes[selectedPosition];
//
//                    StringBuilder urlString = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
//                    urlString.append("location="+mLatitude+","+mLongtitude);
//                    urlString.append("&radius=10000");
//                    urlString.append("&types=" + category);
//                    urlString.append("&sensor=true");
//                    urlString.append("&key="+ API_KEY);
//
//                    // Creating a new non-ui thread task to download Google place json data
//                    LandmarksTask LTask = new LandmarksTask();
//
//                    // Invokes the "doInBackground()" method of the class PlaceTask
//                    LTask.execute(urlString.toString());
//
//
//                }
//            });


            Button shareButton  = (Button) findViewById(R.id.share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch the Google+ share dialog with attribution to your app.
                    Intent shareIntent = new PlusShare.Builder(LandmarksActivity.this)
                            .setType("text/plain")
                            .setText("Welcome to the Google+ platform.")
                            .setContentUrl(Uri.parse("https://developers.google.com/+/"))
                            .getIntent();

                    startActivityForResult(shareIntent, 0);
                }
            });

        }

    }

    /** A class, to download Google Places */
    private class LandmarksTask extends AsyncTask<String, Integer, String>{

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

        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            parserTask.execute(result);
        }

    }

    /** A method to download json data from url */
    private String downloadUrl(String theUrl) throws IOException{
        String data = null;
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

            StringBuffer sBuffer  = new StringBuffer();

            String line = "";
            while( ( line = bReader.readLine())  != null){
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




    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        JSONObject jObject;

        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> landmarks = null;
            LandmarkParser placeLandmarkParser = new LandmarkParser();

            try{
                jObject = new JSONObject(jsonData[0]);

                /** Getting the parsed data as a List construct */
                landmarks = placeLandmarkParser.parse(jObject);

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return landmarks;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){
            String name;
            String vicinity;

            mMap.clear();// Clears all the existing markers

            for(int i=0;i<list.size();i++){

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a landmark from the landmarks list
                HashMap<String, String> pLandmark = list.get(i);

                // latitude of the place
                double lat = Double.parseDouble(pLandmark.get("lat"));

                // longitude of the place
                double lng = Double.parseDouble(pLandmark.get("lng"));

                // name
                name = pLandmark.get("place_name");

                // vicinity
                vicinity = pLandmark.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title(name + " \t " + vicinity);

                // Placing a marker on the touched position
                Marker mark = mMap.addMarker(markerOptions);


                //linking the marker and landmark ref
                markerLink.put(mark.getId(),pLandmark.get("reference"));

            }

        }

    }


    @Override
    public void onLocationChanged(Location location) {
        mLatitude = location.getLatitude();
        mLongtitude= location.getLongitude();
        LatLng latLng = new LatLng(mLatitude, mLongtitude);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

    }

    public void changeType(View view)
    {
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL)
        {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

}
