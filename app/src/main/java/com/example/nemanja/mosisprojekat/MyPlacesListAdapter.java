package com.example.nemanja.mosisprojekat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nemanja on 9/13/2017.
 */

public class MyPlacesListAdapter extends ArrayAdapter<Place> {

    private FirebaseStorage storage;
    private StorageReference storageRef;
    View customview;
    private Context context;

    public MyPlacesListAdapter(Context context, List<Place> places) {
        super(context,R.layout.myplaces_custom_list, places);
        this.context=context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater myplacesListInflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        customview=myplacesListInflater.inflate(R.layout.myplaces_custom_list,parent,false);

        Place p=getItem(position);
        TextView text1=(TextView) customview.findViewById(R.id.placetext1);
        text1.setText(p.getName());
        TextView text2=(TextView) customview.findViewById(R.id.placetext2);
        text1.setText(p.getName());

        ImageView picture=(ImageView) customview.findViewById(R.id.placePicture);

        storageRef=FirebaseStorage.getInstance().getReference().child(p.getImage());
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
