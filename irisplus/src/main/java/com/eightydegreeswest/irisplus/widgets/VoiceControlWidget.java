package com.eightydegreeswest.irisplus.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.VoiceRecognitionActivity;


/**
 * Implementation of App Widget functionality.
 */
public class VoiceControlWidget extends AppWidgetProvider {

    private static final String VOICE_BTN_CLICKED    = "VOICE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
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

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_voice_control);
        Intent voiceIntent = new Intent(context, VoiceRecognitionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, voiceIntent, 0);
        views.setOnClickPendingIntent(R.id.widget_voice_button, pendingIntent);
        views.setTextViewText(R.id.appwidget_voice_text, "Iris+ Voice Control");
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

