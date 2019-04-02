package com.eightydegreeswest.irisplus.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

/**
 * Created by Yuriy on 8/24/15.
 */
public class TaskHelper {

    private static IrisPlusLogger logger = new IrisPlusLogger();
    private static PowerManager pm;
    private static PowerManager.WakeLock wakeLock;

    public static <P, T extends AsyncTask<P, ?, ?>> void execute(T task) {
        try {
            //Check for internet access
            Context context = IrisPlus.getContext();

            if(!isAirplaneModeOn(context)) {
                //Get wakelock
                pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "irisplus");
                wakeLock.acquire();

                SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
                boolean applicationAlerts = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_APP_EVENTS, true);

                boolean success = false;
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo ni = cm.getActiveNetworkInfo();

                if(ni != null && ni.isConnected()) {
                    execute(task, (P[]) null);
                    success = true;
                } else {
                    logger.log(IrisPlusConstants.LOG_INFO, "Could not execute task " + task.toString() + " - no internet access.");
                }

                //No network
                if (applicationAlerts && !success) {
                    NotificationHelper notificationHelper = new NotificationHelper(context);
                    notificationHelper.destroyNotification(412555);
                    notificationHelper.destroyNotificationWithSoundAndVibrate();
                    notificationHelper.createNotificationWithSoundAndVibrateAndIntent("Could not execute command - no internet access.", null);
                }

                if (wakeLock != null) {
                    wakeLock.release();
                }
            }
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_INFO, "Could not execute command.");
            if(wakeLock != null) {
                wakeLock.release();
            }
        }
    }

    @SuppressLint("NewApi")
    private static <P, T extends AsyncTask<P, ?, ?>> void execute(T task, P... params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                Context context = IrisPlus.getContext();
                if(context != null) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean useMultithreading = sharedPreferences.getBoolean(IrisPlusConstants.PREF_MULTI_THREADING, true);
                    if(useMultithreading) {
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
                    } else {
                        task.execute(params);
                    }
                } else {
                    task.execute(params);
                }
            } catch (Exception e) {
                task.execute(params);
            }
        } else {
            task.execute(params);
        }
    }

    @SuppressWarnings("deprecation")
    private static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }
}
