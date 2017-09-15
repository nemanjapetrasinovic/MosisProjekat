package com.example.nemanja.mosisprojekat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import android.location.LocationListener;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;

public class PlacesMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private HashMap<String, String> markersMap;
    private HashMap<String, Marker> markersOnMap;
    String seekBarValue;
    DownloadThread d;
    private double latitude;
    private double longitude;
    private static final int LOCATION_INTERVAL = 5 * 60 * 1000;//5min
    //private static final int LOCATION_INTERVAL = 10000;//1sec
    private static final float LOCATION_DISTANCE = 10f;//meters
    LocationManager mLocationManager;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
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

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL,
                LOCATION_DISTANCE, mLocationListener);

        TextView radius=(TextView) findViewById(R.id.textView3);
        radius.setText(String.valueOf(0));

        Button filters=(Button) findViewById(R.id.button4);
        filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetPlaces("200000");
            }
        });

        SeekBar seekBar=(SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView radius=(TextView) findViewById(R.id.textView3);
                radius.setText(String.valueOf(progress));
                seekBarValue=String.valueOf(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

        GetPlaces("200000");

    }

    public void GetPlaces(final String radius)
    {
        DatabaseReference userRef=mDatabase.child("user/"+mAuth.getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Traveller t=dataSnapshot.getValue(Traveller.class);

                if(t.places!=null){
                    for(final String place:t.places){
                        d=new DownloadThread(place,radius);
                        d.start();

                        try {
                            d.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class DownloadThread extends Thread{
        public String p;
        Place place;
        String radius;

        public DownloadThread(String p,String radius){
            this.p=p;
            this.radius=radius;
            place=new Place();
        }

        @Override
        public void run() {
            super.run();


            final DatabaseReference placeRef=FirebaseDatabase.getInstance().getReference().child("place/"+p);
            placeRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Place p = dataSnapshot.getValue(Place.class);
                    place=p;

                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL,
                            LOCATION_DISTANCE, mLocationListener);

                    Location placeLocation=new Location(LocationManager.GPS_PROVIDER);
                    placeLocation.setLatitude(p.getLatitude());
                    placeLocation.setLongitude(p.getLongitude());

                    Location myLoc=new Location(LocationManager.GPS_PROVIDER);
                    myLoc.setLatitude(latitude);
                    myLoc.setLongitude(longitude);

                    if(myLoc.distanceTo(placeLocation)<=Double.parseDouble(radius)) {
                        String placeimage = place.getImage();
                        StorageReference storageReference = storageRef.child(placeimage);
                        File localFile = null;
                        try {
                            String s[] = placeimage.split("/");
                            localFile = File.createTempFile(s[1], "jpg");
                        } catch (Exception e) {

                        }
                        final File localfile2 = localFile;
                        storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                // Local temp file has been created
                                Bitmap myBitmap = BitmapFactory.decodeFile(localfile2.getAbsolutePath());
                                int width = myBitmap.getWidth();
                                int height = myBitmap.getHeight();

                                int maxWidth = 150;
                                int maxHeight = 150;

                                Log.v("Pictures", "Width and height are " + width + "--" + height);

                                if (width > height) {
                                    // landscape
                                    float ratio = (float) width / maxWidth;
                                    width = maxWidth;
                                    height = (int) (height / ratio);
                                } else if (height > width) {
                                    // portrait
                                    float ratio = (float) height / maxHeight;
                                    height = maxHeight;
                                    width = (int) (width / ratio);
                                } else {
                                    // square
                                    height = maxHeight;
                                    width = maxWidth;
                                }

                                Bitmap myScaledBitmap = Bitmap.createScaledBitmap(myBitmap, width, height, false);
                                LatLng sydney = new LatLng(place.latitude, place.longitude);
                                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(myScaledBitmap);
                                MarkerOptions markerOptions = new MarkerOptions().position(sydney)
                                        .title(place.getName())
                                        .snippet(place.getType())
                                        .icon(icon);

                                Marker mMarker = mMap.addMarker(markerOptions);
                            /*if(markersMap==null)
                                markersMap=new HashMap<String, String>();
                            markersMap.put(value.getEmail(),dataSnapshot.getKey());

                            if(markersOnMap==null)
                                markersOnMap=new HashMap<String, Marker>();
                            markersOnMap.put(value.getEmail(),mMarker);*/

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
}
