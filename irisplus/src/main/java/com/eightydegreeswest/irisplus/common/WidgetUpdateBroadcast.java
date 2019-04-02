package com.eightydegreeswest.irisplus.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.widgets.AlarmWidget;
import com.eightydegreeswest.irisplus.widgets.ControlWidget;
import com.eightydegreeswest.irisplus.widgets.LockWidget;
import com.eightydegreeswest.irisplus.widgets.SceneWidget;
import com.eightydegreeswest.irisplus.widgets.ThermostatWidget;

/*
    This class is used to update the widgets on startup
 */
public class WidgetUpdateBroadcast extends BroadcastReceiver {

    private IrisPlusLogger logger = new IrisPlusLogger();
    private Context context;

	@Override
	public void onReceive(Context context, Intent intent) {
        this.context = context;

        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        String updateIntervalStr = mSharedPrefs.getString(IrisPlusConstants.PREF_WIDGET_UPDATE_INTERVAL, "15");
        int updateInterval = Integer.parseInt(updateIntervalStr) * 60 * 1000;
        int initialDelay = 0;

        PackageManager pm = context.getPackageManager();

        ComponentName receiver = new ComponentName(context, ControlWidget.class);
        int status = pm.getComponentEnabledSetting(receiver);

        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Intent i = new Intent(context, ControlWidget.class);
            i.setAction(ControlWidget.UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IrisPlusConstants.ALARM_CONTROL_WIDGET_REPEAT, i, 0);
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
            alarmManagerHelper.scheduleRepeatingElapsedAlarm(context, pendingIntent, SystemClock.elapsedRealtime() + initialDelay, updateInterval);
        }

        receiver = new ComponentName(context, LockWidget.class);
        status = pm.getComponentEnabledSetting(receiver);

        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Intent i = new Intent(context, LockWidget.class);
            i.setAction(LockWidget.UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IrisPlusConstants.ALARM_LOCK_WIDGET_REPEAT, i, 0);
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
            alarmManagerHelper.scheduleRepeatingElapsedAlarm(context, pendingIntent, SystemClock.elapsedRealtime() + initialDelay, updateInterval);
        }

        receiver = new ComponentName(context, ThermostatWidget.class);
        status = pm.getComponentEnabledSetting(receiver);

        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Intent i = new Intent(context, ThermostatWidget.class);
            i.setAction(ThermostatWidget.UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IrisPlusConstants.ALARM_THERMOSTAT_WIDGET_REPEAT, i, 0);
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
            alarmManagerHelper.scheduleRepeatingElapsedAlarm(context, pendingIntent, SystemClock.elapsedRealtime() + initialDelay, updateInterval);
        }

        receiver = new ComponentName(context, SceneWidget.class);
        status = pm.getComponentEnabledSetting(receiver);

        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Intent i = new Intent(context, SceneWidget.class);
            i.setAction(SceneWidget.UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IrisPlusConstants.ALARM_SCENE_WIDGET_REPEAT, i, 0);
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
            alarmManagerHelper.scheduleRepeatingElapsedAlarm(context, pendingIntent, SystemClock.elapsedRealtime() + initialDelay, updateInterval);
        }

        receiver = new ComponentName(context, AlarmWidget.class);
        status = pm.getComponentEnabledSetting(receiver);

        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Intent i = new Intent(context, AlarmWidget.class);
            i.setAction(AlarmWidget.UPDATE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IrisPlusConstants.ALARM_ALARM_WIDGET_REPEAT, i, 0);
            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
            alarmManagerHelper.scheduleRepeatingElapsedAlarm(context, pendingIntent, SystemClock.elapsedRealtime() + initialDelay, updateInterval);
        }
	}
}
