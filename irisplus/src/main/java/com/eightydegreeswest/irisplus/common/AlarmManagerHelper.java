package com.eightydegreeswest.irisplus.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

import java.util.Calendar;

/**
 * Created by ybelenitsky on 5/28/2015.
 */
public class AlarmManagerHelper {

    private int sdk;
    private IrisPlusLogger logger = new IrisPlusLogger();
    private SharedPreferences mSharedPrefs = null;

    public AlarmManagerHelper() {
        sdk = Build.VERSION.SDK_INT;
    }

    public void scheduleSingleAlarm(Context context, PendingIntent pendingIntent) {
        this.logBuildNumber(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(sdk >= 23) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(SystemClock.elapsedRealtime(), pendingIntent), pendingIntent);
        } else if (sdk >= 19) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
        }
        logger.log(IrisPlusConstants.LOG_INFO, "Executing single immediate alarm.");
    }

    public void scheduleSingleAlarm(Context context, PendingIntent pendingIntent, long delay) {
        this.logBuildNumber(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(sdk >= 23) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            if(delay < 60000) {
                calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + (int) (delay / 1000));
            } else {
                calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + (int) (delay / 60000));
            }
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(), pendingIntent), pendingIntent);
        } else if(sdk >= 19) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
        }
        logger.log(IrisPlusConstants.LOG_INFO, "Scheduling alarm for " + delay / 60000 + " minutes from now.");
    }

    public void scheduleSingleAlarmFuture(Context context, PendingIntent pendingIntent, long start) {
        this.logBuildNumber(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(sdk >= 23) {
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(start, pendingIntent), pendingIntent);
        } else if(sdk >= 19) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, start, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, start, pendingIntent);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        logger.log(IrisPlusConstants.LOG_INFO, "Scheduling alarm for " + cal.getTime().toString() + ".");
    }

    public void scheduleRepeatingAlarm(Context context, PendingIntent pendingIntent, long start, long interval) {
        this.logBuildNumber(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, start, interval, pendingIntent);
    }

    public void scheduleRepeatingElapsedAlarm(Context context, PendingIntent pendingIntent, long start, long interval) {
        this.logBuildNumber(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, start, interval, pendingIntent);
    }

    private void logBuildNumber(Context context) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        //logger.log(IrisPlusConstants.LOG_INFO, "Build number: " + sdk);
    }
}
