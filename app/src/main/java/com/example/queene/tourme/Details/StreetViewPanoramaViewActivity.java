package com.example.queene.tourme.Details;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.queene.tourme.R;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Queene on 06/04/2016.
 */
public class StreetViewPanoramaViewActivity extends FragmentActivity
        implements OnStreetViewPanoramaReadyCallback {

   double lat,lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_street_view);

        //Access to the StreetViewPanorama object and place it to the fragment
        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);
        //set the callback on the fragment
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
        //retrieve the latand long from the LandmarkDetails Activity
        Bundle b =getIntent().getExtras();
        lat = b.getDouble("latitude");
        lon = b.getDouble("longtitude");

    }

    //Retrieve an instance of StreetViewPanorama and set the latitude and longtitude
    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        streetViewPanorama.setPosition(new LatLng(lat, lon));
    }
}
