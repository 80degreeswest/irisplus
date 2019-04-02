package com.eightydegreeswest.irisplus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

import java.util.Date;

public class PinActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pin);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final EditText enteredPin = (EditText) findViewById(R.id.pin);

        enteredPin.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if(enteredPin.getText().toString().length() == 4) {

                    SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String pin = mSharedPrefs.getString(IrisPlusConstants.PREF_PIN, "");

                    if(pin.equalsIgnoreCase(enteredPin.getText().toString())) {
                        mSharedPrefs.edit().putString(IrisPlusConstants.PREF_PIN_ENTERED, Double.toString((double) new Date().getTime())).commit();
                        finish();
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    } else {
                        enteredPin.setText("");
                        Toast.makeText(getApplicationContext(), "Invalid PIN. Please try again!", Toast.LENGTH_LONG).show();
                    }
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.pin, menu);
        return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_login) {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
    protected void onResume() {
        super.onResume();
    }
}
