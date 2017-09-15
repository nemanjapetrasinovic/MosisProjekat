package com.example.nemanja.mosisprojekat;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nikola on 2017-08-20.
 */

class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.PlacesListAdapterViewHolder> implements Filterable{
    private final Context mContext;
    final private PlacesListAdapterOnClickHandler mClickHandler;
    private Geocoder geocoder;
    private ItemFilter mFilter = new ItemFilter();

    private ArrayList<PlaceWrapper> lista;
    private ArrayList<PlaceWrapper> originalnaLista = null;
    private String type = "all";
    private int radius = 20000;
    private double myLongitude;
    private double myLatitude;

    public Context getmContext() {
        return mContext;
    }

    public double getMyLongitude() {
        return myLongitude;
    }

    public void setMyLongitude(double myLongitude) {
        this.myLongitude = myLongitude;
    }

    public double getMyLatitude() {
        return myLatitude;
    }

    public void setMyLatitude(double myLatitude) {
        this.myLatitude = myLatitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    private final Object lock = new Object();
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private HashMap<String,Bitmap> pictures;

    public interface PlacesListAdapterOnClickHandler {
        void onClick(String placeKey);
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();

            FilterResults results = new FilterResults();

            final List<PlaceWrapper> list = originalnaLista;

            int count = list.size();
            final ArrayList<PlaceWrapper> nlist = new ArrayList<PlaceWrapper>(count);

            String filterableString ;
            PlaceWrapper filterablePlace;
            for (int i = 0; i < count; i++) {
                filterablePlace = list.get(i);
                filterableString = filterablePlace.place.getName();
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(filterablePlace);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            lista = (ArrayList<PlaceWrapper>) results.values;
            notifyDataSetChanged();
        }

    }
    @Override
    public Filter getFilter() {
        return mFilter;
    }
    //private Cursor mCursor;

    public PlacesListAdapter(@NonNull Context context, PlacesListAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
        lista = new ArrayList<PlaceWrapper>();
        originalnaLista = new ArrayList<PlaceWrapper>();
        geocoder = new Geocoder(mContext, Locale.getDefault());
        pictures=new HashMap<String,Bitmap>();
    }

    public void freeList(){
        lista.clear();
    }

    public void addToHashMap(String placeName,Bitmap placePicture){
        if(pictures==null)
            pictures=new HashMap<String, Bitmap>();
        pictures.put(placeName,placePicture);
    }

    public PlacesListAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutId = R.layout.place_list_item;

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);

        view.setFocusable(true);

        return new PlacesListAdapterViewHolder(view);
    }

    public void onBindViewHolder(PlacesListAdapterViewHolder placesListAdapterViewHolder, int position) {
        //mCursor.moveToPosition(position);

        Place place = lista.get(position).place;

        /****************
         * Profile Icon *
         ****************/
        int profileImageId = 0;
        //DownloadPictureThread t=new DownloadPictureThread(place.getImage(),placesListAdapterViewHolder);

        /*try {
            t.join();
            t.start();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        StorageReference storageRef;
        storageRef=FirebaseStorage.getInstance().getReference().child(place.getImage());

        Glide.with(mContext)
                .using(new FirebaseImageLoader())
                .load(storageRef)
                .override(80,80)
                .into(placesListAdapterViewHolder.iconView);
        //placesListAdapterViewHolder.iconView.setImageBitmap(pictures.get(place.getName()));
        /****************
         * Name *
         ****************/

        placesListAdapterViewHolder.nameView.setText(place.getName());


        /*************************
         * COUNTRY *
         *************************/


        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(place.getLatitude(), place.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addressList != null && addressList.size() > 0) {
            Address address = addressList.get(0);
            placesListAdapterViewHolder.countryView.setText(address.getCountryName());
        }

    }


    @Override
    public int getItemCount() {
        if (null == lista) return 0;
        return lista.size();
    }

    void swapCursor(ArrayList<PlaceWrapper> newList) {
        lista = newList;
        notifyDataSetChanged();
    }

    void addToList(PlaceWrapper t){
        double distance = CalculationByDistance(t.place.getLatitude(), t.place.getLongitude(), myLatitude, myLongitude);
        if(distance < radius) {
            lista.add(t);
            originalnaLista.add(t);
            notifyDataSetChanged();
        }
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

    class PlacesListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView iconView;
        final TextView nameView;
        final TextView countryView;

        PlacesListAdapterViewHolder(View view) {
            super(view);

            iconView = (ImageView) view.findViewById(R.id.place_item_icon);
            nameView = (TextView) view.findViewById(R.id.place_item_name);
            countryView = (TextView) view.findViewById(R.id.place_item_county);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param v the View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            //mCursor.moveToPosition(adapterPosition);
            //long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            //long dateInMillis = mCursor.getLong(0);//hard fix
            //mClickHandler.onClick(dateInMillis);
            String a = lista.get(adapterPosition).placeKey;
            Toast.makeText(mContext,"Iz adaptera " + a, Toast.LENGTH_SHORT).show();
            mClickHandler.onClick(a);
        }


    }

    /*public class DownloadPictureThread extends Thread{
        public String url;
        public Bitmap picture;
        public PlacesListAdapterViewHolder p;
        public Object lock;

        public DownloadPictureThread(String image, PlacesListAdapterViewHolder p) {
            this.url=image;
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
            StorageReference placePictureRef=storageRef.child(url);
            File localFile=null;
            try {
                String s[]=url.split("\\.");
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
                    p.iconView.setImageBitmap(picture);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });

        }
    }*/


}
