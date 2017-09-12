package com.example.nemanja.mosisprojekat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static android.location.LocationManager.GPS_PROVIDER;

public class NewPlaceActivity extends AppCompatActivity {

    protected LocationManager mLocationManager;
    protected Context context;

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    private static final String TAG = NewPlaceActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    private Traveller t;

    private long numberOfPlaces;
    private String radioType;

    private double lon;
    private double lat;

    class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;
        Location currLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }


        @Override
        public void onLocationChanged(Location location)
        {
            lat = location.getLatitude();
            lon = location.getLongitude();
            currLocation = location;
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_place);

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        try {
            NewPlaceActivity.LocationListener GPSListener = new NewPlaceActivity.LocationListener(GPS_PROVIDER);
            mLocationManager.requestLocationUpdates(
                    GPS_PROVIDER, 0, 0,
                    GPSListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }


        radioType = "City";
        mAuth=FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        try {
            Intent listIntent = getIntent();
            Bundle bundle = listIntent.getExtras();
            String path = bundle.getString("path");

            ImageView imageView = (ImageView) findViewById(R.id.place_image);
            File file = new File(path);

            final Uri photoURI = FileProvider.getUriForFile(this,
                    FILE_PROVIDER_AUTHORITY,
                    file);

            Bitmap mResultsBitmap = BitmapUtils.resamplePic(this, path);
            imageView.setImageBitmap(mResultsBitmap);

            Button save = (Button) findViewById(R.id.editmyplace_finished_button);

            final EditText nameEdit = (EditText) findViewById(R.id.editmyplace_name_edit);
            final EditText descEdit = (EditText) findViewById(R.id.editmyplace_desc_edit);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Place place = new Place();

                    place.setName(nameEdit.getText().toString());
                    place.setLongitude(lon);
                    place.setLatitude(lat);
                    place.setDescription(descEdit.getText().toString());
                    place.setImage("images/"+photoURI.getLastPathSegment());
                    place.setOwner(mAuth.getCurrentUser().getUid());
                    place.setDate(new Date());
                    place.setType(radioType);

                    mDatabase= FirebaseDatabase.getInstance().getReference();

                    //Date currentTimeDate= Calendar.getInstance().getTime();

                    //final String key=String.valueOf(currentTimeDate.getTime()+currentTimeDate.getDate());

                    //DatabaseReference userPlacesRef=
                    final String keyID=mDatabase.child("place").push().getKey();
                    mDatabase.child("place").child(keyID).setValue(place);

                    /*userPlaceRef=mDatabase.child("user/"+mAuth.getCurrentUser().getUid()+"/myplaces");
                    vl=new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            numberOfPlaces=dataSnapshot.getChildrenCount();
                            DatabaseReference r= mDatabase.child("user/"+mAuth.getCurrentUser().getUid()+"/myplaces/"+numberOfPlaces);
                            r.setValue(keyID);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    userPlaceRef.addListenerForSingleValueEvent(vl);*/


                    final DatabaseReference userRef = mDatabase.child("user").child(mAuth.getCurrentUser().getUid());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Traveller me=dataSnapshot.getValue(Traveller.class);
                            if(me.myPlaces==null)
                                me.myPlaces=new ArrayList<String>();
                            me.myPlaces.add(keyID);
                            userRef.setValue(me);
                            if(me.friends!=null){
                                for(final String friend:me.friends){
                                    DatabaseReference friendRef=mDatabase.child("user/"+friend+"/places");
                                    friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            numberOfPlaces=dataSnapshot.getChildrenCount();
                                            DatabaseReference r= mDatabase.child("user/"+friend+"/places/"+numberOfPlaces);
                                            r.setValue(keyID);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    setResult(RESULT_OK);
                    finish();
                }
            });

            Button cancel = (Button) findViewById(R.id.editmyplace_cancel_button);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioCity:
                if (checked)
                    radioType = "City";
                    break;
            case R.id.radioMount:
                if (checked)
                    radioType = "Mountain";
                    break;
            case R.id.radioRiver:
                if (checked)
                    radioType = "River";
                    break;
            case R.id.radioSea:
                if (checked)
                    radioType = "Sea";
                    break;
        }
    }
}
