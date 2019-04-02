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
import com.eightydegreeswest.irisplus.apiv2.ControlApi;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.ControlItem;

import java.util.List;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ControlWidgetConfigureActivity ControlWidgetConfigureActivity}
 */
public class ControlWidget extends AppWidgetProvider {

    public WidgetControlTask widgetControlTask = null;
    private IrisPlusLogger logger = new IrisPlusLogger();
    private SharedPreferences mSharedPrefs = null;

    public static final String UPDATE = "update";
    public static final String CONTROL = "control";

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
            ControlWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
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
            onUpdate(context, manager, manager.getAppWidgetIds(new ComponentName(context, ControlWidget.class)));
        } else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(CONTROL)) {
            super.onReceive(context, intent);
            //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Receiving Control widget command from user.");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views;
            ComponentName watchWidget;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_control);
            watchWidget = new ComponentName(context, ControlWidget.class);

            widgetControlTask = new WidgetControlTask(context, views, appWidgetManager, watchWidget, intent.getStringExtra("DEVICE_NAME"), intent.getStringExtra("DEVICE_ID"), intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1), intent.getAction());
            TaskHelper.execute(widgetControlTask);

            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.buttonFeedback();

            appWidgetManager.updateAppWidget(watchWidget, views);

        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Updating Control widget.");

        CharSequence widgetText = ControlWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_control);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        CharSequence widgetId = ControlWidgetConfigureActivity.loadIdPref(context, appWidgetId, widgetText.toString());

        if(widgetId == null) widgetId = "";

        views.setOnClickPendingIntent(R.id.widget_control_button, getPendingSelfIntent(context, CONTROL, widgetText.toString(), widgetId.toString(), appWidgetId));

        widgetControlTask = new WidgetControlTask(context, views, appWidgetId, appWidgetManager, widgetText.toString(), widgetId.toString(), null);
        TaskHelper.execute(widgetControlTask);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action, String deviceName, String deviceId, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra("DEVICE_NAME", deviceName);
        intent.putExtra("DEVICE_ID", deviceId);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
    }

    public class WidgetControlTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private RemoteViews mViews;
        private int mAppWidgetID;
        private AppWidgetManager mAppWidgetManager;
        private String mDeviceName;
        private String mDeviceID;
        private String mState;
        private String mAction;

        //View task
        public WidgetControlTask(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, String deviceName, String deviceId, String action) {
            mContext = context;
            mViews = views;
            mAppWidgetID = appWidgetID;
            mAppWidgetManager = appWidgetManager;
            mDeviceName = deviceName;
            mDeviceID = deviceId;
            mAction = action;
        }

        //Do task
        public WidgetControlTask(Context context, RemoteViews views, AppWidgetManager appWidgetManager, ComponentName watchWidget, String deviceName, String deviceId, int appWidgetId, String action) {
            mContext = context;
            mViews = views;
            mAppWidgetManager = appWidgetManager;
            mDeviceName = deviceName;
            mDeviceID = deviceId;
            mAppWidgetID = appWidgetId;
            mAction = action;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ControlApi irisApi = new ControlApi(mContext);
            //logger.log(IrisPlusConstants.LOG_INFO, "Widget loading: " + mDeviceName + "(" + mDeviceID + ", " + mDeviceType + ") - " + mState);
            try {
                List<ControlItem> devices = irisApi.getAllControls();
                for (ControlItem device : devices) {
                    if (device.getControlName().equalsIgnoreCase(mDeviceName)) {
                        mDeviceID = device.getId();
                        mState = device.getStatus();
                        break;
                    }
                }
            } catch (Exception e) { }

            //logger.log(IrisPlusConstants.LOG_INFO, "Widget loaded: " + mDeviceName + "(" + mDeviceID + ", " + mDeviceType + ") - " + mState);

            if(mAction != null && mAction.equalsIgnoreCase(CONTROL) && mDeviceID != null) {
                //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Controlling widget state. Current state is " + mState);
                if("on".equalsIgnoreCase(mState)) {
                    mState = "off";
                } else if("off".equalsIgnoreCase(mState)) {
                    mState = "on";
                }

                if(mDeviceName != null && !"".equalsIgnoreCase(mDeviceName)) {
                    try {
                        irisApi.setDeviceState(mDeviceID, mState);
                        return true;
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
                //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Controlling widget state for " + mDeviceName + " (" + mDeviceID + ", " + mDeviceType + "). New state is " + mState);
                if ("on".equalsIgnoreCase(mState)) {
                    mViews.setInt(R.id.widget_control_button, "setBackgroundResource", R.drawable.ic_power_on);
                } else if("off".equalsIgnoreCase(mState)) {
                    mViews.setInt(R.id.widget_control_button, "setBackgroundResource", R.drawable.ic_power_off);
                }

                if(mAppWidgetID >= 0) {
                    mAppWidgetManager.updateAppWidget(mAppWidgetID, mViews);
                }
            }
        }
    }
}

