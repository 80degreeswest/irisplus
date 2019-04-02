package com.eightydegreeswest.irisplus.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.apiv2.ThermostatApi;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.ThermostatDetailsItem;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ThermostatWidgetConfigureActivity ThermostatWidgetConfigureActivity}
 */
public class ThermostatWidget extends AppWidgetProvider {

    public WidgetThermostatTask widgetThermostatTask = null;
    private IrisPlusLogger logger = new IrisPlusLogger();
    private SharedPreferences mSharedPrefs = null;

    public static final String UPDATE = "update";
    public static final String CONTROL = "control";
    private ThermostatDetailsItem thermostatDetails = null;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));

        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            ThermostatWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));

        if(intent.getAction() != null && intent.getAction().equalsIgnoreCase(UPDATE)) {
            final AppWidgetManager manager = AppWidgetManager.getInstance(context);
            onUpdate(context, manager, manager.getAppWidgetIds(new ComponentName(context, ThermostatWidget.class)));
        } else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(CONTROL)) {
            super.onReceive(context, intent);
            //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Receiving Thermostat widget command from user.");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views;
            ComponentName watchWidget;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_thermostat);
            watchWidget = new ComponentName(context, ThermostatWidget.class);

            widgetThermostatTask = new WidgetThermostatTask(context, views, appWidgetManager, watchWidget, intent.getStringExtra("DEVICE_NAME"), intent.getStringExtra("DEVICE_ID"), intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1), intent.getAction(), intent.getStringExtra("DEVICE_COMMAND"));
            TaskHelper.execute(widgetThermostatTask);

            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.buttonFeedback();

            appWidgetManager.updateAppWidget(watchWidget, views);

        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Updating Thermostat widget.");

        CharSequence widgetText = ThermostatWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_thermostat);
        //views.setTextViewText(R.id.appwidget_text, widgetText);

        CharSequence widgetId = ThermostatWidgetConfigureActivity.loadIdPref(context, appWidgetId, widgetText.toString());
        if(widgetId == null) widgetId = "";

        views.setOnClickPendingIntent(R.id.widget_thermostat_btn_up, getPendingSelfIntent(context, CONTROL, widgetText.toString(), widgetId.toString(), appWidgetId, 1001, "UP"));
        views.setOnClickPendingIntent(R.id.widget_thermostat_btn_down, getPendingSelfIntent(context, CONTROL, widgetText.toString(), widgetId.toString(), appWidgetId, 1002, "DOWN"));

        widgetThermostatTask = new WidgetThermostatTask(context, views, appWidgetId, appWidgetManager, widgetText.toString(), widgetId.toString(), null);
        TaskHelper.execute(widgetThermostatTask);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action, String deviceName, String deviceId, int appWidgetId, int intentId, String upDown) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra("DEVICE_NAME", deviceName);
        intent.putExtra("DEVICE_COMMAND", upDown);
        intent.putExtra("DEVICE_ID", deviceId);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, intentId, intent, 0);
    }

    public class WidgetThermostatTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private RemoteViews mViews;
        private int mAppWidgetID;
        private AppWidgetManager mAppWidgetManager;
        private ComponentName mWatchWidget;
        private String mDeviceName;
        private String mDeviceID;
        private String mCommand;
        private String mAction;

        //View task
        public WidgetThermostatTask(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, String deviceName, String deviceId, String action) {
            mContext = context;
            mViews = views;
            mAppWidgetID = appWidgetID;
            mAppWidgetManager = appWidgetManager;
            mDeviceName = deviceName;
            mDeviceID = deviceId;
            mAction = action;
        }

        //Do task
        public WidgetThermostatTask(Context context, RemoteViews views, AppWidgetManager appWidgetManager, ComponentName watchWidget, String deviceName, String deviceId, int appWidgetId, String action, String command) {
            mContext = context;
            mViews = views;
            mWatchWidget = watchWidget;
            mAppWidgetManager = appWidgetManager;
            mDeviceName = deviceName;
            mCommand = command;
            mDeviceID = deviceId;
            mAppWidgetID = appWidgetId;
            mAction = action;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            if(true) {
                ThermostatApi irisApi = new ThermostatApi(mContext);
                thermostatDetails = irisApi.getThermostatDetails(mDeviceID);
            }

            if(mAction != null && mAction.equalsIgnoreCase(CONTROL) && mDeviceID != null) {
                if(mDeviceName != null && !"".equalsIgnoreCase(mDeviceName)) {
                    try {
                        if(thermostatDetails.getTargetTemperature() != null) {
                            if ("UP".equalsIgnoreCase(mCommand)) {
                                thermostatDetails.setHeatTargetTemperature(Integer.toString(Integer.parseInt(thermostatDetails.getHeatTargetTemperature()) + 1));
                                thermostatDetails.setCoolTargetTemperature(Integer.toString(Integer.parseInt(thermostatDetails.getCoolTargetTemperature()) + 1));
                            } else if ("DOWN".equalsIgnoreCase(mCommand)) {
                                thermostatDetails.setHeatTargetTemperature(Integer.toString(Integer.parseInt(thermostatDetails.getHeatTargetTemperature()) - 1));
                                thermostatDetails.setCoolTargetTemperature(Integer.toString(Integer.parseInt(thermostatDetails.getCoolTargetTemperature()) - 1));
                            }
                            ThermostatApi irisApi = new ThermostatApi(mContext);
                            irisApi.setTemperature(mDeviceID, thermostatDetails.getHeatTargetTemperature(), "HEAT");
                            irisApi.setTemperature(mDeviceID, thermostatDetails.getCoolTargetTemperature(), "COOL");
                            return true;
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                if(thermostatDetails.getTargetTemperature() == null) {
                    thermostatDetails.setTargetTemperature("--");
                }
                if(thermostatDetails.getCurrentTemperature() == null) {
                    thermostatDetails.setCurrentTemperature("--");
                }
                if(thermostatDetails.getHumidity() == null) {
                    thermostatDetails.setHumidity("--");
                }
                mViews.setTextViewText(R.id.widget_thermostat_mode, thermostatDetails.getMode());
                mViews.setTextViewText(R.id.widget_thermostat_current_temp, thermostatDetails.getCurrentTemperature() + (char) 0x00B0);
                mViews.setTextViewText(R.id.widget_thermostat_target_and_humidity, thermostatDetails.getHeatTargetTemperature() + (char) 0x00B0 + "/" + thermostatDetails.getCoolTargetTemperature() + (char) 0x00B0);
                if(mAppWidgetID >= 0) {
                    mAppWidgetManager.updateAppWidget(mAppWidgetID, mViews);
                }
            }
        }
    }
}

