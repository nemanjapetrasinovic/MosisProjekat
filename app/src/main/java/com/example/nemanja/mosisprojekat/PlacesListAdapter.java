package com.example.nemanja.mosisprojekat;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Nikola on 2017-08-20.
 */

class PlacesListAdapter extends RecyclerView.Adapter<PlacesListAdapter.PlacesListAdapterViewHolder>{
    private final Context mContext;
    final private PlacesListAdapterOnClickHandler mClickHandler;
    private Geocoder geocoder;

    public interface PlacesListAdapterOnClickHandler {
        void onClick(long date);
    }

    //private Cursor mCursor;
    private ArrayList<Place> lista;
    public PlacesListAdapter(@NonNull Context context, PlacesListAdapterOnClickHandler clickHandler, ArrayList<Place> l) {
        mContext = context;
        mClickHandler = clickHandler;
        lista = l;
        geocoder = new Geocoder(mContext, Locale.getDefault());
    }

    public PlacesListAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutId = R.layout.place_list_item;

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);

        view.setFocusable(true);

        return new PlacesListAdapterViewHolder(view);
    }

    public void onBindViewHolder(PlacesListAdapterViewHolder placesListAdapterViewHolder, int position) {
        //mCursor.moveToPosition(position);

        Place place = lista.get(position);

        /****************
         * Profile Icon *
         ****************/
        int profileImageId = 0;
        placesListAdapterViewHolder.iconView.setImageResource(R.drawable.art_clouds);

        /****************
         * Name *
         ****************/

        placesListAdapterViewHolder.nameView.setText(place.getName());


        /*************************
         * COUNTRY *
         *************************/


        List<Address> addressList = null;
        try {
            addressList = geocoder.getFromLocation(44, 45, 1);
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

    void swapCursor(ArrayList<Place> newList) {
        lista = newList;
        notifyDataSetChanged();
    }

    void addToList(Place t){
        lista.add(t);
        notifyDataSetChanged();
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
            Toast.makeText(mContext,"sss", Toast.LENGTH_SHORT);
        }
    }


}
