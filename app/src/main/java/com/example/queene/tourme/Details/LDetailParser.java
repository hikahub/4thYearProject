package com.example.queene.tourme.Details;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Queene on 26/01/2016.
 */
public class LDetailParser {
    /** Receives a JSONObject and returns a list */
    public HashMap<String,String> parse(JSONObject jObject){

        JSONObject jLDetails = null;
        try {
            /** Retrieves all the elements in the 'places' array */
            jLDetails = jObject.getJSONObject("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /** Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getLandmarkDetails(jLDetails);
    }

    /** Parsing the Place Details Object object */
    private HashMap<String, String> getLandmarkDetails(JSONObject jLDetails){

        HashMap<String, String> landDetails = new HashMap<String, String>();

        String name = "-NA-";
        //String icon = "-NA-";
        String formatted_address="-NA-";
        String latitude="";
        String longitude="";
        String website="-NA-";
        String rating="-NA-";
        String international_phone_number = "-NA-";


        try {
            // Extracting Place name, if available
            if(!jLDetails.isNull("name")){
                name = jLDetails.getString("name");
            }

            // Extracting Icon, if available
//            if(!jLDetails.isNull("icon")){
//                icon = jLDetails.getString("icon");
//            }

            // Extracting Place formatted_address, if available
            if(!jLDetails.isNull("formatted_address")){
                formatted_address = jLDetails.getString("formatted_address");
            }

            // Extracting website, if available
            if(!jLDetails.isNull("website")){
                website = jLDetails.getString("website");
            }

            // Extracting rating, if available
            if(!jLDetails.isNull("rating")){
                rating = jLDetails.getString("rating");
            }

            // Extracting rating, if available
            if(!jLDetails.isNull("international_phone_number")){
                international_phone_number = jLDetails.getString("international_phone_number");
            }



            latitude = jLDetails.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jLDetails.getJSONObject("geometry").getJSONObject("location").getString("lng");

            landDetails.put("name", name);
           // landDetails .put("icon", icon);
            landDetails.put("lat", latitude);
            landDetails.put("lng", longitude);
            landDetails.put("formatted_address", formatted_address);
            landDetails.put("website", website);
            landDetails.put("rating", rating);
            landDetails.put("international_phone_number", international_phone_number);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return landDetails;
    }
}
