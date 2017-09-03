package com.example.nemanja.mosisprojekat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

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
        mDatabase = FirebaseDatabase.getInstance().getReference();
        try{
            GetUserData();
        }
        catch (Exception e){

        }
    }

    public void GetUserData() throws IOException
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            DatabaseReference userRef=mDatabase.child("user").child(user.getUid());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Traveller currentUser=dataSnapshot.getValue(Traveller.class);
                    TextView fName=(TextView) findViewById(R.id.firstName);
                    TextView lName=(TextView) findViewById(R.id.lastName);
                    TextView phone=(TextView) findViewById(R.id.phoneNumber);
                    TextView email=(TextView) findViewById(R.id.email);

                    fName.setText(currentUser.firstname);
                    lName.setText(currentUser.lastname);
                    phone.setText(currentUser.phonenumber);
                    email.setText(currentUser.email);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            String userID=user.getUid();
            StorageReference storageReference = storageRef.child(userID+".jpg");


            final File localFile = File.createTempFile(userID, "jpg");

            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    ImageView image=(ImageView) findViewById(R.id.profilePicture);
                    image.setImageBitmap(myBitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }
    }
}
