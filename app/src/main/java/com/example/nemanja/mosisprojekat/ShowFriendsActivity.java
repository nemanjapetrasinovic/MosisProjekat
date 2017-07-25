package com.example.nemanja.mosisprojekat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

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

public class ShowFriendsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_friends);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();
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

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            DatabaseReference userRef=mDatabase.child("user").child(user.getUid());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Traveller value = dataSnapshot.getValue(Traveller.class);
                    String userID=user.getUid();
                    for (final String url:value.friends) {
                        DatabaseReference userRef=mDatabase.child("user").child(url);

                        userRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                final Traveller value = dataSnapshot.getValue(Traveller.class);
                                String userID=url;
                                StorageReference storageReference = storageRef.child(userID+".jpg");
                                File localFile=null;
                                try {
                                    localFile = File.createTempFile(userID, "jpg");
                                }
                                catch (Exception e){

                                }
                                final File localfile2=localFile;
                                storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Local temp file has been created
                                        Bitmap myBitmap = BitmapFactory.decodeFile(localfile2.getAbsolutePath());
                                        int width = myBitmap.getWidth();
                                        int height = myBitmap.getHeight();

                                        int maxWidth=150;
                                        int maxHeight=150;

                                        Log.v("Pictures", "Width and height are " + width + "--" + height);

                                        if (width > height) {
                                            // landscape
                                            float ratio = (float) width / maxWidth;
                                            width = maxWidth;
                                            height = (int)(height / ratio);
                                        } else if (height > width) {
                                            // portrait
                                            float ratio = (float) height / maxHeight;
                                            height = maxHeight;
                                            width = (int)(width / ratio);
                                        } else {
                                            // square
                                            height = maxHeight;
                                            width = maxWidth;
                                        }

                                        Bitmap myScaledBitmap=Bitmap.createScaledBitmap(myBitmap,width,height,false);
                                        LatLng sydney = new LatLng(value.latitude, value.longitude);
                                        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(myScaledBitmap);
                                        MarkerOptions markerOptions = new MarkerOptions().position(sydney)
                                                .title(value.getFirstname()+ ' '+value.getLastname())
                                                .snippet(url)
                                                .icon(icon);

                                        Marker mMarker = mMap.addMarker(markerOptions);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                // Failed to read value
                                Log.w(TAG, "Failed to read value.", error.toException());
                            }
                        });

                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(ShowFriendsActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
