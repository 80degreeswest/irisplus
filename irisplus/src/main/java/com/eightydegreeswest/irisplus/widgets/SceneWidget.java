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
import com.eightydegreeswest.irisplus.apiv2.SceneApi;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link SceneWidgetConfigureActivity SceneWidgetConfigureActivity}
 */
public class SceneWidget extends AppWidgetProvider {

    public WidgetSceneTask widgetSceneTask = null;
    private IrisPlusLogger logger = new IrisPlusLogger();
    private SharedPreferences mSharedPrefs = null;

    public static final String UPDATE = "update";
    public static final String SCENE = "scene";

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
            SceneWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
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
            onUpdate(context, manager, manager.getAppWidgetIds(new ComponentName(context, SceneWidget.class)));
        } else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(SCENE)) {
            super.onReceive(context, intent);
            //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Receiving Scene widget command from user.");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views;
            ComponentName watchWidget;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_scene);
            watchWidget = new ComponentName(context, SceneWidget.class);

            widgetSceneTask = new WidgetSceneTask(context, views, appWidgetManager, watchWidget, intent.getStringExtra("SCENE_NAME"), intent.getStringExtra("SCENE_ID"), intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1), intent.getAction());
            TaskHelper.execute(widgetSceneTask);

            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.buttonFeedback();

            appWidgetManager.updateAppWidget(watchWidget, views);

        }
    }

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        //logger.log(IrisPlusConstants.LOG_INFO, "Widget: Updating Scene widget.");

        CharSequence widgetText = SceneWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_scene);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setInt(R.id.widget_scene_button, "setBackgroundResource", R.drawable.ic_scene);

        CharSequence widgetId = SceneWidgetConfigureActivity.loadIdPref(context, appWidgetId, widgetText.toString());

        if(widgetId == null) widgetId = "";

        views.setOnClickPendingIntent(R.id.widget_scene_button, getPendingSelfIntent(context, SCENE, widgetText.toString(), widgetId.toString(), appWidgetId));

        widgetSceneTask = new WidgetSceneTask(context, views, appWidgetId, appWidgetManager, widgetText.toString(), widgetId.toString(), null);
        TaskHelper.execute(widgetSceneTask);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action, String sceneName, String sceneId, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra("SCENE_NAME", sceneName);
        intent.putExtra("SCENE_ID", sceneId);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
    }

    public class WidgetSceneTask extends AsyncTask<Void, Void, Boolean> {

        private Context mContext;
        private RemoteViews mViews;
        private int mAppWidgetID;
        private AppWidgetManager mAppWidgetManager;
        private String mSceneName;
        private String mSceneID;
        private String mAction;

        //View task
        public WidgetSceneTask(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager, String sceneName, String sceneId, String action) {
            mContext = context;
            mViews = views;
            mAppWidgetID = appWidgetID;
            mAppWidgetManager = appWidgetManager;
            mSceneName = sceneName;
            mSceneID = sceneId;
            mAction = action;
        }

        //Do task
        public WidgetSceneTask(Context context, RemoteViews views, AppWidgetManager appWidgetManager, ComponentName watchWidget, String sceneName, String sceneId, int appWidgetId, String action) {
            mContext = context;
            mViews = views;
            mAppWidgetManager = appWidgetManager;
            mSceneName = sceneName;
            mSceneID = sceneId;
            mAppWidgetID = appWidgetId;
            mAction = action;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(mAction != null && mAction.equalsIgnoreCase(SCENE) && mSceneID != null) {
                SceneApi sceneApi = new SceneApi(mContext);
                sceneApi.runScene(mSceneID);
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

