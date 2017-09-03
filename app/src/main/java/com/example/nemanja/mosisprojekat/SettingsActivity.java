package com.example.nemanja.mosisprojekat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;

public class SettingsActivity extends AppCompatActivity {

    private String radioType;
    private int radius;

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
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
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
        }
    }
}

