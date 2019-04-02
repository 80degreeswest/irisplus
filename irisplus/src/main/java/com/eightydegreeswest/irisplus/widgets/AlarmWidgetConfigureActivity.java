package com.eightydegreeswest.irisplus.widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.BasicSpinnerAdapter;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.TaskHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * The configuration screen for the {@link AlarmWidget AlarmWidget} AppWidget.
 */
public class AlarmWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    Spinner mAppWidgetText;
    private static final String PREFS_NAME = "com.eightydegreeswest.irisplus.widgets.AlarmWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    private static final String PREF_PREFIX_ID = "appwidget_id_";
    AlarmWidget alarmWidget = new AlarmWidget();
    public WidgetAlarmConfigureTask widgetAlarmConfigureTask = null;
    private IrisPlusLogger logger = new IrisPlusLogger();

    public AlarmWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.widget_alarm_configure);
        mAppWidgetText = (Spinner) findViewById(R.id.appwidget_text);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        TextView message = (TextView) findViewById(R.id.appwidget_text_message);
        message.setText("Loading alarm modes...");

        widgetAlarmConfigureTask = new WidgetAlarmConfigureTask(this.getApplicationContext());
        TaskHelper.execute(widgetAlarmConfigureTask);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = AlarmWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetText = mAppWidgetText.getSelectedItem().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            alarmWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.commit();
    }

    static void saveIdPref(Context context, int appWidgetId, String name, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_ID + appWidgetId + "_" + name, text);
        prefs.commit();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static String loadIdPref(Context context, int appWidgetId, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_ID + appWidgetId + "_" + name, "");
        return titleValue;
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }

    public class WidgetAlarmConfigureTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private List<String> alarms = new ArrayList<String>();

        public WidgetAlarmConfigureTask(Context context) {
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            alarms.add("Off");
            alarms.add("On");
            alarms.add("Partial");
            alarms.add("Panic");

            try {
                for (String item : alarms) {
                    saveIdPref(mContext, mAppWidgetId, item, item.toUpperCase());
                }
            } catch (Exception e) {
                //No alarms
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            BasicSpinnerAdapter spinnerArrayAdapter = new BasicSpinnerAdapter(mContext, android.R.layout.simple_spinner_item, alarms);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner deviceNames = (Spinner) findViewById(R.id.appwidget_text);
            deviceNames.setAdapter(spinnerArrayAdapter);
            deviceNames.setSelection(0);

            TextView message = (TextView) findViewById(R.id.appwidget_text_message);
            message.setText("");
        }
    }
}

