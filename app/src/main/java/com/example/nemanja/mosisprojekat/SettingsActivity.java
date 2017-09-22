package com.example.nemanja.mosisprojekat;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;


public class SettingsActivity extends AppCompatActivity {

    private String radioType;
    private int radius;
    private static Date dateFrom;
    private static Date dateTo;

    private DatePicker datePicker;
    private Calendar calendar;
    private static TextView dateViewFrom;
    private static TextView dateViewTo;
    private int year, month, day;
    static boolean from = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        radioType = "City";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar2);
        final EditText radiusEdit = (EditText) findViewById(R.id.radiusEdit);
        Button button = (Button) findViewById(R.id.advanced_search);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = progress;
                radiusEdit.setText(progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("radius", radius);
                bundle.putString("type", radioType);

                bundle.putInt("dateFromYear", dateFrom.getYear());
                bundle.putInt("dateFromMonth", dateFrom.getMonth());
                bundle.putInt("dateFromDate", dateFrom.getDate());

                bundle.putInt("dateToYear", dateTo.getYear());
                bundle.putInt("dateToMonth", dateTo.getMonth());
                bundle.putInt("dateToDate", dateTo.getDate());

                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });



        //howDialog(999);

        dateViewFrom = (TextView) findViewById(R.id.edit_from);
        dateViewTo = (TextView) findViewById(R.id.edit_to);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /*
         * Normally, calling setDisplayHomeAsUpEnabled(true) (we do so in onCreate here) as well as
         * declaring the parent activity in the AndroidManifest is all that is required to get the
         * up button working properly. However, in this case, we want to navigate to the previous
         * screen the user came from when the up button was clicked, rather than a single
         * designated Activity in the Manifest.
         *
         * We use the up button's ID (android.R.id.home) to listen for when the up button is
         * clicked and then call onBackPressed to navigate to the previous Activity when this
         * happens.
         */
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioCitySearch:
                if (checked)
                    radioType = "City";
                break;
            case R.id.radioMountSearch:
                if (checked)
                    radioType = "Mountain";
                break;
            case R.id.radioRiverSearch:
                if (checked)
                    radioType = "River";
                break;
            case R.id.radioSeaSearch:
                if (checked)
                    radioType = "Sea";
                break;
            case R.id.radioOtherSearch:
                if (checked)
                    radioType = "Other";
                break;
            case R.id.radioAllSearch:
                if (checked)
                    radioType = "all";
                break;
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            showDate(year, month, day);
        }
    }

    public void datePickerFrom(View view){
        from  = true;
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "Date picker");
    }

    public void datePickerTo(View view){
        from = false;
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "Date picker");
    }
    public void datePicker(View view){
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.show(getSupportFragmentManager(), "Date picker");
    }


/*
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setDate(final Calendar calendar) {
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

        ((TextView) findViewById(R.id.edit_from))
                .setText(dateFormat.format(calendar.getTime()));

    }*/
/*
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar cal = new GregorianCalendar(year, month, day);
        setDate(cal);
    }*/

    private static void showDate(int year, int month, int day) {
        if(from == true){
            dateViewFrom.setText(new StringBuilder().append(day).append("/")
                    .append(month + 1).append("/").append(year));
            dateFrom = new Date(year, month, day);
        }
        else{
            dateViewTo.setText(new StringBuilder().append(day).append("/")
                    .append(month + 1).append("/").append(year));
            dateTo = new Date(year, month, day);
        }
    }
    /*
    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
        Toast.makeText(getApplicationContext(), "ca",
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }*/
}


