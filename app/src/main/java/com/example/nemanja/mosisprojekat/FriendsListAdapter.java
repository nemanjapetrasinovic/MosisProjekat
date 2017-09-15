package com.example.nemanja.mosisprojekat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

/**
 * Created by Nemanja on 9/13/2017.
 */

public class FriendsListAdapter extends ArrayAdapter<TravellerWrapper> {

    private FirebaseStorage storage;
    private StorageReference storageRef;
    View customview;
    private Context context;

    public FriendsListAdapter(Context context, List<TravellerWrapper> travellers) {
        super(context,R.layout.friends_custom_list, travellers);
        this.context=context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater friendsListInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customview=friendsListInflater.inflate(R.layout.friends_custom_list,parent,false);

        TravellerWrapper t=getItem(position);
        TextView fname=(TextView) customview.findViewById(R.id.firstname1);
        fname.setText(t.traveller.getFirstname());
        TextView lname=(TextView) customview.findViewById(R.id.lastname1);
        lname.setText(t.traveller.getLastname());

        ImageView picture=(ImageView) customview.findViewById(R.id.picture1);
        storageRef=FirebaseStorage.getInstance().getReference().child(t.traveller.getImage());
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(storageRef)
                .override(80,80)
                .into(picture);
        return customview;
    }

    public void updateList(){
        notifyDataSetChanged();;
    }
}
