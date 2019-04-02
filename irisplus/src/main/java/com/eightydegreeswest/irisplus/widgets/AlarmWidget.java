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
import com.eightydegreeswest.irisplus.apiv2.SecurityApi;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link AlarmWidgetConfigureActivity AlarmWidgetConfigureActivity}
 */
public class AlarmWidget extends AppWidgetProvider {

    public WidgetAlarmTask widgetAlarmTask = null;
    private IrisPlusLogger logger = new IrisPlusLogger();
    private SharedPreferences mSharedPrefs = null;

    public static final String UPDATE = "update";
    public static final String ALARM = "alarm";

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
            AlarmWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
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
            onUpdate(context, manager, manager.getAppWidgetIds(new ComponentName(context, AlarmWidget.class)));
        } else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(ALARM)) {
            super.onReceive(context, intent);
            //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Receiving Alarm widget command from user.");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views;
            ComponentName watchWidget;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_alarm);
            watchWidget = new ComponentName(context, AlarmWidget.class);

            widgetAlarmTask = new WidgetAlarmTask(context, views, appWidgetManager, watchWidget, intent.getStringExtra("ALARM_NAME"), intent.getStringExtra("ALARM_ID"), intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1), intent.getAction());
            TaskHelper.execute(widgetAlarmTask);

            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.buttonFeedback();

            appWidgetManager.updateAppWidget(watchWidget, views);

        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Updating Alarm widget.");

        CharSequence widgetText = AlarmWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_alarm);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setInt(R.id.widget_alarm_button, "setBackgroundResource", R.drawable.ic_security);

        CharSequence widgetId = AlarmWidgetConfigureActivity.loadIdPref(context, appWidgetId, widgetText.toString());

        if(widgetId == null) widgetId = "";

        views.setOnClickPendingIntent(R.id.widget_alarm_button, getPendingSelfIntent(context, ALARM, widgetText.toString(), widgetId.toString(), appWidgetId));

        widgetAlarmTask = new WidgetAlarmTask(context, views, appWidgetId, appWidgetManager, widgetText.toString(), widgetId.toString(), null);
        TaskHelper.execute(widgetAlarmTask);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action, String alarmName, String alarmId, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra("ALARM_NAME", alarmName);
        intent.putExtra("ALARM_ID", alarmId);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
    }

    public class WidgetAlarmTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private RemoteViews mViews;
        private int mAppWidgetID;
        private AppWidgetManager mAppWidgetManager;
        private String mAlarmName;
        private String mAlarmID;
        private String mAction;

        //View task
        public WidgetAlarmTask(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, String alarmName, String alarmId, String action) {
            mContext = context;
            mViews = views;
            mAppWidgetID = appWidgetID;
            mAppWidgetManager = appWidgetManager;
            mAlarmName = alarmName;
            mAlarmID = alarmId;
            mAction = action;
        }

        //Do task
        public WidgetAlarmTask(Context context, RemoteViews views, AppWidgetManager appWidgetManager, ComponentName watchWidget, String alarmName, String alarmId, int appWidgetId, String action) {
            mContext = context;
            mViews = views;
            mAppWidgetManager = appWidgetManager;
            mAlarmName = alarmName;
            mAlarmID = alarmId;
            mAppWidgetID = appWidgetId;
            mAction = action;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(mAction != null && mAction.equalsIgnoreCase(ALARM) && mAlarmID != null) {
                SecurityApi securityApi = new SecurityApi(mContext);
                securityApi.setAlarmBypass(mAlarmID);
            } else {
                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                if(mAppWidgetID >= 0) {
                    mAppWidgetManager.updateAppWidget(mAppWidgetID, mViews);
                }
            }
        }
    }
}

