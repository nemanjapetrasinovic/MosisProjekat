package com.example.nemanja.mosisprojekat;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class RangListActivity extends AppCompatActivity {

    RangListAdapter rangListadapter;

    private List<Traveller> travellers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rang_list);

        travellers=new ArrayList<Traveller>();

        rangListadapter =new RangListAdapter(this,travellers);
        ListView rangListView=(ListView) findViewById(R.id.listview);
        rangListView.setAdapter(rangListadapter);
        getUserInfo();
    }

    public void getUserInfo(){

        DatabaseReference dref= FirebaseDatabase.getInstance().getReference("user");
        dref.orderByChild("score").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Traveller p=dataSnapshot.getValue(Traveller.class);
                travellers.add(0,p);
                //travellers.add(p);
                rangListadapter.updateList();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Traveller t = dataSnapshot.getValue(Traveller.class);
                String value= t.getFirstname()+" "+t.getLastname() +" "+t.getScore();
                travellers.remove(value);
                rangListadapter.updateList();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
