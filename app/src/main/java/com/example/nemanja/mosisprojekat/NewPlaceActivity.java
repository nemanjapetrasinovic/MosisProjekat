package com.example.nemanja.mosisprojekat;

import android.content.Intent;
import android.graphics.Bitmap;
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

public class NewPlaceActivity extends AppCompatActivity {

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    private static final String TAG = NewPlaceActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    private long numberOfPlaces;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_place);

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
            final Double lon = bundle.getDouble("longitude");
            final Double lat = bundle.getDouble("latitude");

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

                    mDatabase= FirebaseDatabase.getInstance().getReference();

                    DatabaseReference userPlacesRef=mDatabase.child("user").child(mAuth.getCurrentUser().getUid()+"/places");
                    userPlacesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            numberOfPlaces=dataSnapshot.getChildrenCount();
                            mDatabase.child("user").child(mAuth.getCurrentUser().getUid()).child("places/"+numberOfPlaces).setValue(place);
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
}
