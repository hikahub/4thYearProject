package com.example.queene.tourme;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.InputStream;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    // Google client to communicate with Google
    private GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress, userSigned;
    private ConnectionResult mConnectionResult;
    private SignInButton loginButton;
    private ImageView profImg;
    private TextView username,city;
    private LinearLayout GProfile;
    private RelativeLayout GSignin;
    private Button landmarksButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (SignInButton) findViewById(R.id.signin);
        loginButton.setOnClickListener(this);
        landmarksButton = (Button) findViewById(R.id.mainlandmarks);

        profImg = (ImageView) findViewById(R.id.image);
        username = (TextView) findViewById(R.id.username);
        city  = (TextView) findViewById(R.id.pCity);

        GProfile = (LinearLayout) findViewById(R.id.GProfile);
        GSignin = (RelativeLayout) findViewById(R.id.GSignin);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(Plus.API, Plus.PlusOptions.builder().build()).addScope(Plus.SCOPE_PLUS_LOGIN).build();

        landmarksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(),LandmarksActivity.class);
                startActivity(intent);
            }
        });

    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;

            if (userSigned) {
                resolveSignInError();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (responseCode == RESULT_OK) {
                    userSigned = false;

                }
                mIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        userSigned = false;
        Toast.makeText(this, "User is Connected", Toast.LENGTH_LONG).show();
        getProfileInformation();
    }

    private void updateProf(boolean isSignedIn) {
        if (isSignedIn) {
            GSignin.setVisibility(View.GONE);
            GProfile.setVisibility(View.VISIBLE);

        } else {
            GSignin.setVisibility(View.VISIBLE);
            GProfile.setVisibility(View.GONE);
        }
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String userPhoto = currentPerson.getImage().getUrl();
                String lCity= "";
                List<Person.PlacesLived> personCity= currentPerson.getPlacesLived();
                if (personCity != null) {
                    for (Person.PlacesLived place : personCity) {
                        lCity = place.getValue();
                    }
                }
                username.setText(personName);
                city.setText(lCity);

                userPhoto = userPhoto.substring(0,userPhoto.length() -2) + 400;

                new LoadProfileImage(profImg).execute(userPhoto);

                // update profile frame with new info about Google Account
                // profile
                updateProf(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        updateProf(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signin:
                googlePlusLogin();
                break;
        }
    }

    public void signIn(View v) {
        googlePlusLogin();
    }

    public void logout(View v) {
        googlePlusLogout();
    }

    private void googlePlusLogin() {
        if (!mGoogleApiClient.isConnecting()) {
            userSigned = true;
            resolveSignInError();
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            updateProf(false);
        }
    }

    // download Google Account profile image, to complete profile
    private class LoadProfileImage extends AsyncTask<String,Void,Bitmap> {
        ImageView profImage;

        public LoadProfileImage(ImageView image) {
            this.profImage = image;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap iconB = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                iconB = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return iconB;
        }

        protected void onPostExecute(Bitmap result) {
            profImage.setImageBitmap(result);
        }


    }
}
