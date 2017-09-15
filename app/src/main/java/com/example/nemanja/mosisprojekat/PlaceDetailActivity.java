package com.example.nemanja.mosisprojekat;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PlaceDetailActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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


        String placekey=getIntent().getStringExtra("placeKey");
        FirebaseDatabase mDatabase=FirebaseDatabase.getInstance();
        DatabaseReference placeRef=mDatabase.getReference().child("place/"+placekey);
        placeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Place p=dataSnapshot.getValue(Place.class);

                LatLng placeLoc=new LatLng(p.getLatitude(),p.getLongitude());
                mMap.addMarker(new MarkerOptions().position(placeLoc).title("Place location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placeLoc,12.0f));

                TextView placeInfoName=(TextView) findViewById(R.id.place_info_name);
                placeInfoName.setText(p.getName());
                TextView placeInfoDesc=(TextView) findViewById(R.id.place_info_desc);
                placeInfoDesc.setText(p.getDescription());
                TextView placeInfoType=(TextView) findViewById(R.id.place_info_type);
                placeInfoType.setText(p.getType());

                ImageView picture=(ImageView) findViewById(R.id.place_info_icon);

                StorageReference storageRef= FirebaseStorage.getInstance().getReference().child(p.getImage());
                Glide.with(PlaceDetailActivity.this.getApplicationContext())
                        .using(new FirebaseImageLoader())
                        .load(storageRef)
                        .override(100,100)
                        .into(picture);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
