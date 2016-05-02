package com.example.queene.tourme;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * Created by Queene on 02/12/2015.
 */
public class LandmarkParser {
    /** Receives a JSONObject and returns a list */
    public List<HashMap<String,String>> parse(JSONObject jObject){

        JSONArray jArray = null;
        try {
            /** Retrieves all the elements in the 'landmark' array */
            jArray  = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getLandmarks(jArray);
    }


    private List<HashMap<String, String>> getLandmarks(JSONArray jArrays){
        List<HashMap<String, String>> landmarksList = new ArrayList<HashMap<String,String>>();
        /** Taking each landmark, parses and adds to list object */
        for(int i=0; i<jArrays.length();i++){
            try {
                HashMap<String, String> landmark = getLandmark((JSONObject)jArrays.get(i));
                landmarksList.add(landmark);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return landmarksList;
    }

    /** Parsing the Landmark JSON object */
    private HashMap<String, String> getLandmark(JSONObject jObj){

        HashMap<String, String> landmark = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity="-NA-";

        try {
            // Extracting Place name, if available
            if(!jObj.isNull("name")){
                placeName = jObj.getString("name");
            }

            // Extracting Place Vicinity, if available
            if(!jObj.isNull("vicinity")){
                vicinity = jObj.getString("vicinity");
            }

            String latitude = jObj.getJSONObject("geometry").getJSONObject("location").getString("lat");
            String longitude = jObj.getJSONObject("geometry").getJSONObject("location").getString("lng");
            String reference = jObj.getString("reference");


            landmark.put("place_name", placeName);
            landmark.put("vicinity", vicinity);
            landmark.put("lat", latitude);
            landmark.put("lng", longitude);
            landmark.put("reference",reference);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return landmark;
    }
}
