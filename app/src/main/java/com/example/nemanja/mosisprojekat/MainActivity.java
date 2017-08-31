package com.example.nemanja.mosisprojekat;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PlacesListAdapter.PlacesListAdapterOnClickHandler
        {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 1;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";
    private static final int ACTIVITY_NEW_PLACE = 8;
    private String mTempPhotoPath;

    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    private int mNotificationId = 001;
    private double longitude;
    private double latitude;

    Intent intentMyService;
    ComponentName service;
    BroadcastReceiver receiver;
    String GPS_FILTER = "com.example.rankovic.mylocationtracker.LOCATION";

    private PlacesListAdapter placesListAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<Place> placesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Location Service start
        intentMyService = new Intent(this, LocationService.class);
        service = startService(intentMyService);

        IntentFilter mainFilter = new IntentFilter(GPS_FILTER);
        receiver = new MyMainLocalReceiver();
        registerReceiver(receiver, mainFilter);
        //Location Service end

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                takePicture();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

        placesArrayList = new ArrayList<Place>();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_places);
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        placesListAdapter = new PlacesListAdapter(this, this);
        mRecyclerView.setAdapter(placesListAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        DatabaseReference userRef = mDatabase.child("user").child(mAuth.getCurrentUser().getUid());
        final Gson gson=new Gson();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                placesListAdapter.freeList();
                Object o=dataSnapshot.getValue();
                String json=gson.toJson(o);
                Traveller t=gson.fromJson(json,Traveller.class);


                List<Place> places=t.getPlaces();
                if(places!=null) {
                    DownloadPicture download=null;
                    for (Place place : places) {
                        download = new DownloadPicture(place, placesListAdapter);
                        download.start();

                    }
                }

                if(t.friends!=null)
                    for(String url:t.friends){
                        DatabaseReference friendRef=mDatabase.child("user").child(url);
                        friendRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Traveller friend=dataSnapshot.getValue(Traveller.class);
                                if(friend.places!=null){
                                    DownloadPicture download=null;
                                    for(Place p:friend.places){
                                        download = new DownloadPicture(p, placesListAdapter);
                                        download.start();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

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
        /*try {
            GetUserData();
        }
        catch (Exception e){
            System.out.print(e.toString());
        }*/

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent map=new Intent(MainActivity.this.getApplicationContext(),MapsActivity.class);
            startActivity(map);
        } else if (id == R.id.nav_gallery) {
            Intent ShowFriendsOnMap=new Intent(MainActivity.this.getApplicationContext(),ShowFriendsActivity.class);
            startActivity(ShowFriendsOnMap);
        } else if (id == R.id.nav_slideshow) {

            final NotificationCompat.Builder mBuilder =
                    (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_menu_camera)
                            .setContentTitle("My notification")
                            .setContentText("Hello World!");
            final NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


            mBuilder.setVibrate(new long[] {125,75,125,275,200,275,125,75,125,275,200,600,200,600});

            mNotifyMgr.notify(mNotificationId, mBuilder.build());
            mNotificationId++;

        } else if (id == R.id.nav_manage) {

            startService(new Intent(this,MyService.class));

        } else if (id == R.id.nav_share) {
            Intent bluetooth = new Intent(MainActivity.this, FriendsSearchActivity.class);
            startActivity(bluetooth);
        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public void GetUserData() throws IOException
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getToken() instead.
            String userName = user.getDisplayName();
            View headerView = navigationView.getHeaderView(0);
            TextView navUsername = (TextView) headerView.findViewById(R.id.userInfo);
            navUsername.setText(userName);

            String userEmail = user.getEmail();
            View headerView1 = navigationView.getHeaderView(0);
            TextView navTextView = (TextView) headerView.findViewById(R.id.textView);
            navTextView.setText(userEmail);

            String userID=user.getUid();
            StorageReference storageReference = storageRef.child(userID+".jpg");


            final File localFile = File.createTempFile(userID, "jpg");

            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Bitmap myBitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    ImageView image=(ImageView) findViewById(R.id.imageView);
                    image.setImageBitmap(myBitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

/*
            mDatabase = FirebaseDatabase.getInstance().getReference();
            DatabaseReference ref =  mDatabase.child("user").child(mAuth.getCurrentUser().getUid());
            //DatabaseReference userRef=mDatabase.child("user").child(user.getUid());
            /*userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Traveller value = dataSnapshot.getValue(Traveller.class);
                    try {
                        placesArrayList = value.getPlaces();
                        for (Place place : placesArrayList) {
                            //placesListAdapter.addToList(place);
                        }

                    }
                    catch (Exception e){

                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
            */
        }
    }

    @Override
    public void onClick(long date) {

    }

    private class MyMainLocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = intent.getDoubleExtra("latitude", -1);
            longitude = intent.getDoubleExtra("longitude", -1);
            /*EditText lon = (EditText) findViewById(R.id.lon);
            EditText lat = (EditText) findViewById(R.id.lat);
            lon.setText(String.valueOf(longitude));
            lat.setText(String.valueOf(latitude));*/
            //Toast.makeText(getApplicationContext(), String.valueOf(latitude) +  " " + String.valueOf(longitude), Toast.LENGTH_LONG).show();
        }
    }

    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                try {
                    // Launch the camera activity
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                catch (Exception e){
                    e.printStackTrace();
                    String m = e.getMessage();
                }
            }
        }
    }

    public void takePicture() {
        // Check for the external storage permission
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // If you do not have permission, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            // Launch the camera if the permission exists
            launchCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // Called when you request permission to read and write to external storage
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If you get permission, launch the camera
                    launchCamera();
                } else {
                    // If you do not get permission, show a Toast
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            if (resultCode == RESULT_OK) {
                // Process the image and set it to the TextView
                processAndSetImage();
            } else {
                // Otherwise, delete the temporary image file
                BitmapUtils.deleteImageFile(this, mTempPhotoPath);
            }
        }
        else if(requestCode == ACTIVITY_NEW_PLACE){
            if (resultCode == RESULT_OK) {
                Snackbar.make(findViewById(R.id.fab), "New place added", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    private void processAndSetImage() {

        Uri file = Uri.fromFile(new File(mTempPhotoPath));
        StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

// Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString("path", mTempPhotoPath);
        bundle.putDouble("longitude", longitude);
        bundle.putDouble("latitude", latitude);
        Intent i = new Intent(MainActivity.this, NewPlaceActivity.class);
        i.putExtras(bundle);
        startActivityForResult(i, ACTIVITY_NEW_PLACE);

    }

        public class DownloadPicture extends Thread{
            public Place place;
            public Bitmap picture;
            public PlacesListAdapter p;
            public Object lock;

            public DownloadPicture(Place place, PlacesListAdapter p) {
                this.place=place;
                this.p=p;
            }

            public Bitmap getPicture(){
                return picture;
            }

            @Override
            public void run() {
                super.run();

                if(storage==null)
                    storage=FirebaseStorage.getInstance();
                if(storageRef==null)
                    storageRef = storage.getReference();
                StorageReference placePictureRef=storageRef.child(place.getImage());
                File localFile=null;
                try {
                    String s[]=place.getImage().split("\\.");
                    String s1[]=s[0].split("/");
                    localFile = File.createTempFile(s1[1], "jpg");
                }
                catch (Exception e){

                }
                final File localfile2=localFile;
                placePictureRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        Bitmap myBitmap = BitmapFactory.decodeFile(localfile2.getAbsolutePath());
                        int width = myBitmap.getWidth();
                        int height = myBitmap.getHeight();

                        int maxWidth=150;
                        int maxHeight=150;


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
                        picture=myScaledBitmap;
                        p.addToHashMap(place.getName(),picture);
                        p.addToList(place);
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
