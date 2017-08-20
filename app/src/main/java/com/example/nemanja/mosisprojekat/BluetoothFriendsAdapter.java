package com.example.nemanja.mosisprojekat;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Nikola on 2017-08-20.
 */

class BluetoothFriendsAdapter extends RecyclerView.Adapter<BluetoothFriendsAdapter.BluetoothFriendsAdapterViewHolder>{
    private final Context mContext;
    final private BluetoothFriendsAdapterOnClickHandler mClickHandler;

    public interface BluetoothFriendsAdapterOnClickHandler {
        void onClick(long date);
    }

    //private Cursor mCursor;
    private ArrayList<Traveller> lista;
    public BluetoothFriendsAdapter(@NonNull Context context, BluetoothFriendsAdapterOnClickHandler clickHandler, ArrayList<Traveller> l) {
        mContext = context;
        mClickHandler = clickHandler;
        lista = l;
    }

    public BluetoothFriendsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        int layoutId = R.layout.bluetooth_list_item;

        View view = LayoutInflater.from(mContext).inflate(layoutId, viewGroup, false);

        view.setFocusable(true);

        return new BluetoothFriendsAdapterViewHolder(view);
    }

    public void onBindViewHolder(BluetoothFriendsAdapterViewHolder bluetoothFriendsAdapterViewHolder, int position) {
        //mCursor.moveToPosition(position);

        Traveller traveller = lista.get(position);

        /****************
         * Profile Icon *
         ****************/
        int profileImageId = 0;
        bluetoothFriendsAdapterViewHolder.iconView.setImageResource(R.drawable.art_clouds);

        /****************
         * Name *
         ****************/

        bluetoothFriendsAdapterViewHolder.nameView.setText("Nikola Rankovic");

        /***********************
         * Bluetooth id *
         ***********************/

        bluetoothFriendsAdapterViewHolder.bluetooth_idView.setText("22:22:6D:98:C2:00");

        /*************************
         * FRIEND *
         *************************/
        bluetoothFriendsAdapterViewHolder.friendView.setText("FRIEND");
    }


    @Override
    public int getItemCount() {
        if (null == lista) return 0;
        return lista.size();
    }

    void swapCursor(ArrayList<Traveller> newList) {
        lista = newList;
        notifyDataSetChanged();
    }

    void addToList(Traveller t){
        lista.add(t);
        notifyDataSetChanged();
    }

    class BluetoothFriendsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView iconView;

        final TextView nameView;
        final TextView bluetooth_idView;
        final TextView friendView;

        BluetoothFriendsAdapterViewHolder(View view) {
            super(view);

            iconView = (ImageView) view.findViewById(R.id.profile_icon);
            nameView = (TextView) view.findViewById(R.id.name);
            bluetooth_idView = (TextView) view.findViewById(R.id.bluetooth_id);
            friendView = (TextView) view.findViewById(R.id.friend);

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
