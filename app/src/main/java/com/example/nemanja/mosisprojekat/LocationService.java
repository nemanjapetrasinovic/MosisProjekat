package com.example.nemanja.mosisprojekat;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationService extends Service {

    private static final String TAG = "LOCATION_SERVICE";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 5*60*1000;//5min
    //private static final int LOCATION_INTERVAL = 10000;//1sec
    private static final float LOCATION_DISTANCE = 10f;//meters

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    private int mNotificationId = 001;
    NotificationManager mNotifyMgr;
    NotificationCompat.Builder mBuilder;


    class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;
        Location currLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        private double CalculationByDistance(double lat1, double lon1, double lat2, double lon2) {
            int Radius = 6371;// radius of earth in Km
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);
            double c = 2 * Math.asin(Math.sqrt(a));
            double valueResult = Radius * c;
            double km = valueResult / 1;
        /*DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        */
            return Radius * c;
        }

        @Override
        public void onLocationChanged(final Location location)
        {
            currLocation = location;
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);


            final DatabaseReference userRef = mDatabase.child("user").child(mAuth.getCurrentUser().getUid());
            //double distance = CalculationByDistance(location.getLatitude(), location.getLongitude(), lat, lon);

            final Gson gson=new Gson();
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

                @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {
                      // This method is called once with the initial value and again
                      // whenever data at this location is updated.

                      Object o = dataSnapshot.getValue();
                      String json = gson.toJson(o);
                      final Traveller t = gson.fromJson(json, Traveller.class);
                      t.latitude=location.getLatitude();
                      t.longitude=location.getLongitude();
                      if(t.places!=null) {
                          for (final String url : t.places) {
                              DatabaseReference friendRef = mDatabase.child("place/").child(url);
                              friendRef.addValueEventListener(new ValueEventListener() {
                                  Location l=location;
                                  @Override
                                  public void onDataChange(DataSnapshot dataSnapshot) {
                                      Place place = dataSnapshot.getValue(Place.class);
                                      Location tasklocation=new Location(LocationManager.GPS_PROVIDER);
                                      tasklocation.setLatitude(place.getLatitude());
                                      tasklocation.setLongitude(place.getLongitude());
                                      double distance = l.distanceTo(tasklocation);

                                      if(distance<=1000){
                                          mBuilder.setContentText("Congratulations! You reached location - "+place.getName()+"!");
                                          mNotifyMgr.notify(mNotificationId, mBuilder.build());
                                          mNotificationId++;

                                          t.places.remove(url);
                                          if(t.visitedPlaces==null)
                                              t.visitedPlaces=new ArrayList<String>();
                                          t.visitedPlaces.add(url);

                                          Location home=new Location("home");
                                          home.setLatitude(t.homelat);
                                          home.setLongitude(t.homelon);

                                          Location placeLoc=new Location("place");
                                          home.setLatitude(place.getLatitude());
                                          home.setLongitude(place.getLongitude());

                                          double points = home.distanceTo(placeLoc);

                                          if(t.score==-1)
                                              t.score=0;
                                          t.score=t.score+points;
                                          userRef.setValue(t);
                                      }
                                  }

                                  @Override
                                  public void onCancelled(DatabaseError databaseError) {

                                  }
                              });
                          }
                      }
                    userRef.setValue(t);
                  }
            });


                                                  ExecutorService transThread = Executors.newSingleThreadExecutor();
            transThread.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        callBroadcastReceiver();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void callBroadcastReceiver(){
            Intent myFilteredResponse = new
                    Intent("com.example.rankovic.mylocationtracker.LOCATION");
            myFilteredResponse.putExtra("latitude", currLocation.getLatitude());
            myFilteredResponse.putExtra("longitude", currLocation.getLongitude());

            sendBroadcast(myFilteredResponse);
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

    // LocationListener GPSListener;
    //LocationListener NetworkListener;
    LocationListener GPSListener =  new LocationListener(LocationManager.GPS_PROVIDER);
    LocationListener NetworkListener =  new LocationListener(LocationManager.NETWORK_PROVIDER);
    public LocationService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        Log.e(TAG, "initializeLocationManager");

        mAuth= FirebaseAuth.getInstance();
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

        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    GPSListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    NetworkListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_near_me_white_48px)
                        .setContentTitle("MosisProjekat notification")
                        .setContentText("")
                        .setSound(uri);
        mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mBuilder.setVibrate(new long[] {1000,200,1000,200});

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {

            try {
                mLocationManager.removeUpdates(GPSListener);
                mLocationManager.removeUpdates(NetworkListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
