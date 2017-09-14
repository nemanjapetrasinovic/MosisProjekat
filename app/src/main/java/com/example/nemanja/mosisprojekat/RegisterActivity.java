package com.example.nemanja.mosisprojekat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    private Uri selectedImage;

    private static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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

        final Button buttonCreateAccount=(Button) findViewById(R.id.buttonCreateAccount);
        buttonCreateAccount.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        final Button buttonSelectPicture=(Button) findViewById(R.id.buttonAddPicture);
        buttonSelectPicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void createAccount(){
        EditText emailEdit=(EditText) findViewById(R.id.editTextEmail);
        EditText passwordEdit=(EditText) findViewById(R.id.editTextPassword);

        final String username=emailEdit.getText().toString();
        final String password=passwordEdit.getText().toString();

        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            singIn(username,password);
                            UpdateUserProfile();
                            Intent i=new Intent(RegisterActivity.this.getApplicationContext(),MainActivity.class);
                            RegisterActivity.this.startActivity(i);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void singIn(String username, String password){
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(RegisterActivity.this, "Nije uspelo",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void UpdateUserProfile()
    {
        EditText nameEdit=(EditText) findViewById(R.id.editTextName);
        EditText lastnameEdit=(EditText) findViewById(R.id.editTextLastname);
        EditText phoneEdit=(EditText) findViewById(R.id.editTextPhone);
        EditText addresEdit=(EditText) findViewById(R.id.editTextAddress);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        String userID=user.getUid();
        StorageReference sRef = storageRef.child(userID+".jpg");

        ImageView imageView = (ImageView) findViewById(R.id.imageView2);
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = sRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });


        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(nameEdit.getText().toString()+' '+lastnameEdit.getText().toString())
                .setPhotoUri(Uri.parse(userID+".jpg"))
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });


        Traveller t=new Traveller();
        t.setFirstname(nameEdit.getText().toString());
        t.setLastname(lastnameEdit.getText().toString());
        t.setPhonenumber(phoneEdit.getText().toString());
        t.setEmail(user.getEmail());
        t.setImage(userID+".jpg");
        t.setAddress(addresEdit.getText().toString());
        Geocoder coder=new Geocoder(RegisterActivity.this.getApplicationContext());
        List<Address> address;

        try {
            address = coder.getFromLocationName(t.getAddress(), 5);
            if (address != null) {
                Address location = address.get(0);
                t.homelat=location.getLatitude();
                t.homelon=location.getLongitude();
            }
        }
        catch (Exception e){

        }
        mDatabase= FirebaseDatabase.getInstance().getReference();
        /*mDatabase.child("user").child(user.getUid()).child("firstname").setValue(nameEdit.getText().toString());
        mDatabase.child("user").child(user.getUid()).child("lastname").setValue(lastnameEdit.getText().toString());
        mDatabase.child("user").child(user.getUid()).child("phone").setValue(phoneEdit.getText().toString());*/
        mDatabase.child("user").child(user.getUid()).setValue(t);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            ImageView imageView = (ImageView) findViewById(R.id.imageView2);
            imageView.setImageURI(selectedImage);

        }

    }

}