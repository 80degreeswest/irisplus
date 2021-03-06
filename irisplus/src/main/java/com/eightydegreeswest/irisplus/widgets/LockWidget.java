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
import com.eightydegreeswest.irisplus.apiv2.LockApi;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.LockItem;

import java.util.List;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link LockWidgetConfigureActivity LockWidgetConfigureActivity}
 */
public class LockWidget extends AppWidgetProvider {

    public WidgetLockTask widgetLockTask = null;
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
            LockWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
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
            onUpdate(context, manager, manager.getAppWidgetIds(new ComponentName(context, LockWidget.class)));
        } else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(CONTROL)) {
            super.onReceive(context, intent);
            //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Receiving Lock widget command from user.");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views;
            ComponentName watchWidget;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_lock);
            watchWidget = new ComponentName(context, LockWidget.class);

            widgetLockTask = new WidgetLockTask(context, views, appWidgetManager, watchWidget, intent.getStringExtra("DEVICE_NAME"), intent.getStringExtra("DEVICE_ID"), intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1), intent.getAction());
            TaskHelper.execute(widgetLockTask);

            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.buttonFeedback();

            appWidgetManager.updateAppWidget(watchWidget, views);

        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Updating Lock widget.");

        CharSequence widgetText = LockWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_lock);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        CharSequence widgetId = LockWidgetConfigureActivity.loadIdPref(context, appWidgetId, widgetText.toString());

        if(widgetId == null) widgetId = "";

        views.setOnClickPendingIntent(R.id.widget_lock_button, getPendingSelfIntent(context, CONTROL, widgetText.toString(), widgetId.toString(), appWidgetId));

        widgetLockTask = new WidgetLockTask(context, views, appWidgetId, appWidgetManager, widgetText.toString(), widgetId.toString(), null);
        TaskHelper.execute(widgetLockTask);

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

    public class WidgetLockTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private RemoteViews mViews;
        private int mAppWidgetID;
        private AppWidgetManager mAppWidgetManager;
        private String mDeviceName;
        private String mDeviceID;
        private String mState;
        private String mAction;
        private String mLockType;
        private String mStatus;

        //View task
        public WidgetLockTask(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, String deviceName, String deviceId, String action) {
            mContext = context;
            mViews = views;
            mAppWidgetID = appWidgetID;
            mAppWidgetManager = appWidgetManager;
            mDeviceName = deviceName;
            mDeviceID = deviceId;
            mAction = action;
        }

        //Do task
        public WidgetLockTask(Context context, RemoteViews views, AppWidgetManager appWidgetManager, ComponentName watchWidget, String deviceName, String deviceId, int appWidgetId, String action) {
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
            LockApi irisApi = new LockApi(mContext);
            //logger.log(IrisPlusConstants.LOG_INFO, "Widget loading: " + mDeviceName + "(" + mDeviceID + ", " + mDeviceType + ") - " + mState);
            try {
                List<LockItem> devices = irisApi.getAllLocks();
                for (LockItem device : devices) {
                    if (device.getLockName().equalsIgnoreCase(mDeviceName)) {
                        mDeviceID = device.getId();
                        mState = device.getState();
                        mStatus = device.getState();
                        mLockType = device.getType();
                        break;
                    }
                }
            } catch (Exception e) { }

            //logger.log(IrisPlusConstants.LOG_INFO, "Widget loaded: " + mDeviceName + "(" + mDeviceID + ") - " + mState);

            if(mAction != null && mAction.equalsIgnoreCase(CONTROL) && mDeviceID != null) {
                //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Lock widget state. Current state is " + mState);
                if("Lock".equalsIgnoreCase(mLockType)) {
                    mStatus = "UNLOCKED".equalsIgnoreCase(mState) ? "LOCKED" : "UNLOCKED";
                    mState = "UNLOCKED".equalsIgnoreCase(mState) ? "\"doorlock:lockstate\":\"LOCKED\"" : "\"doorlock:lockstate\":\"UNLOCKED\"";
                } else if("Garage Door".equalsIgnoreCase(mLockType)) {
                    mStatus = "OPEN".equals(mState) ? "CLOSED" : "OPEN";
                    mState = "OPEN".equalsIgnoreCase(mState) ? "\"motdoor:doorstate\":\"CLOSED\"" : "\"motdoor:doorstate\":\"OPEN\"";
                }

                if(mDeviceName != null && !"".equalsIgnoreCase(mDeviceName)) {
                    try {
                        irisApi.setLockState(mDeviceID, mState);
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
            //logger.log(IrisPlusConstants.LOG_DEBUG, "Widget: Lock widget state for " + mDeviceName + " (" + mDeviceID + "). New status is " + mStatus);
            if ("locked".equalsIgnoreCase(mStatus) || "locking".equalsIgnoreCase(mStatus) || "closed".equalsIgnoreCase(mStatus)) {
                mViews.setInt(R.id.widget_lock_button, "setBackgroundResource", R.drawable.ic_locked);
            } else if ("unlocked".equalsIgnoreCase(mStatus) || "open".equalsIgnoreCase(mStatus)) {
                mViews.setInt(R.id.widget_lock_button, "setBackgroundResource", R.drawable.ic_unlocked);
            }

            if (mAppWidgetID >= 0) {
                mAppWidgetManager.updateAppWidget(mAppWidgetID, mViews);
            }
        }
    }
}

