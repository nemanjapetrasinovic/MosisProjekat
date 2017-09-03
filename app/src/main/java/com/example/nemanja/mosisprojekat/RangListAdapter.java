package com.example.nemanja.mosisprojekat;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Nemanja on 9/2/2017.
 */

public class RangListAdapter extends ArrayAdapter<Traveller> {

    public RangListAdapter(Context context,List<Traveller> travellers) {
        super(context, R.layout.ranglist_custom ,travellers);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater ranglistInflater=LayoutInflater.from(getContext());
        View customView=ranglistInflater.inflate(R.layout.ranglist_custom,parent,false);

        Traveller t=getItem(position);
        TextView Name=(TextView) customView.findViewById(R.id.textViewFirstName);
        TextView LastName=(TextView) customView.findViewById(R.id.textViewLastName);
        TextView Score=(TextView) customView.findViewById(R.id.textViewScore);

        Name.setText(t.getFirstname());
        LastName.setText(t.getLastname());
        Score.setText(t.getScore()+"pts");

        return customView;
    }

    public void updateList(){
        notifyDataSetChanged();;
    }
}
