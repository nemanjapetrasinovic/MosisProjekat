package com.example.nemanja.mosisprojekat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nemanja on 9/12/2017.
 */

public class TabFriends extends Fragment {
    @Nullable
    private static final String TAG = NewPlaceActivity.class.getSimpleName();
    private List<TravellerWrapper> travellers;
    FriendsListAdapter friendsListAdapter;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference mDatabase;
    private Traveller t;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_friends, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        travellers = new ArrayList<TravellerWrapper>();

        friendsListAdapter = new FriendsListAdapter(TabFriends.this.getContext(), travellers);
        ListView friendListView = (ListView) getActivity().findViewById(R.id.friendslist);
        friendListView.setAdapter(friendsListAdapter);
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
        mDatabase = FirebaseDatabase.getInstance().getReference();
        getFriends();

        friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object p=parent.getItemAtPosition(position);
                Gson gson=new Gson();
                String json=gson.toJson(p);
                TravellerWrapper tw=gson.fromJson(json,TravellerWrapper.class);

                Intent i=new Intent(getActivity(),InfoActivity.class);
                i.putExtra("key",tw.travellerKey);
                startActivity(i);
            }
        });
    }

    public void getFriends() {
        String key=getActivity().getIntent().getStringExtra("key");
        final DatabaseReference userRef = mDatabase.child("user").child(key);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Traveller me = dataSnapshot.getValue(Traveller.class);
                if (me.friends != null) {
                    for (final String friend : me.friends) {
                        DatabaseReference friendRef = mDatabase.child("user/" + friend);
                        friendRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Traveller f = dataSnapshot.getValue(Traveller.class);
                                TravellerWrapper tw=new TravellerWrapper();
                                tw.traveller=f;
                                tw.travellerKey=friend;
                                travellers.add(tw);
                                friendsListAdapter.updateList();
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
    }
}
