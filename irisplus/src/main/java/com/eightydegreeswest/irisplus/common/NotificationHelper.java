package com.eightydegreeswest.irisplus.common;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

/**
 * Created by ybelenitsky on 1/27/2015.
 */
public class NotificationHelper {

    private int notifyID = 0;
    private Context mContext;
    private Activity mActivity;
    private SharedPreferences mSharedPrefs;
    private boolean notifyBar = false;
    private boolean notifyToast = false;
    private boolean notifyDialog = false;
    private ProgressDialog pd;

    private final String message = "Updating...";

    public NotificationHelper(Context context) {
        mContext = context;

        if(mContext == null) {
            mContext = IrisPlus.getContext();
        }

        if(mActivity == null) {
            mActivity = IrisPlus.getCurrentActivity();
        }

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        notifyBar = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_BAR, true);
        notifyToast = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_TOAST, false);
        notifyDialog = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_DIALOG, false);
    }

    public NotificationHelper(Activity activity) {
        mActivity = activity;

        if(mActivity != null) {
            mContext = mActivity.getApplicationContext();
        }

        if(mContext == null) {
            mContext = IrisPlus.getContext();
        }

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        notifyBar = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_BAR, true);
        notifyToast = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_TOAST, false);
        notifyDialog = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_DIALOG, false);
    }

    public int createRefreshNotification() {
        if(notifyToast) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
        if(notifyBar) {
            notifyID = 412555;

            Intent intent = new Intent(mContext, IrisActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            Notification notification = new Notification.Builder(mContext)
                    .setContentTitle("Iris+")
                    .setStyle(new Notification.BigTextStyle().bigText(message))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setWhen(System.currentTimeMillis())
                    .setTicker(message)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(notifyID, notification);
        }
        if(notifyDialog) {
            pd = new ProgressDialog(mActivity);
            pd.setMessage(message);
            pd.setTitle("Iris+");
            pd.setCancelable(false);
            pd.show();
        }
        return notifyID;
    }

    @Deprecated
    private void createNotificationWithSoundAndVibrate(String message) {
        if(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_EVENTS, true)) {
            int notifyID = 412999;

            Intent intent = new Intent(mContext, IrisActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification notification = new Notification.Builder(mContext)
                    .setContentTitle("Iris+")
                    .setStyle(new Notification.BigTextStyle().bigText(message))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setSound(soundUri)
                    .setVibrate(new long[]{1000, 1000})
                    .setWhen(System.currentTimeMillis())
                    .setTicker(message)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(pIntent).addAction(R.drawable.ic_drawer, "View", pIntent)
                    .build();
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(notifyID, notification);
        }
    }

    public void createNotificationWithSoundAndVibrateAndIntent(String message, String menuName) {
        doCreateNotificationWithSoundAndVibrateAndIntent(message, menuName, false);
    }

    public void createNotificationSilentAndIntent(String message, String menuName) {
        doCreateNotificationWithSoundAndVibrateAndIntent(message, menuName, true);
    }

    private void doCreateNotificationWithSoundAndVibrateAndIntent(String message, String menuName, boolean silent) {
        if(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_NOTIFY_EVENTS, true)) {
            int notifyID = 412999;

            Intent intent = new Intent(mContext, IrisActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("menuFragment", menuName);
            PendingIntent pIntent = PendingIntent.getActivity(mContext, IrisPlusConstants.NOTIFICATION_INTENT, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if(!"".equalsIgnoreCase(mSharedPrefs.getString(IrisPlusConstants.PREF_RINGTONE, ""))) {
                soundUri = Uri.parse(mSharedPrefs.getString(IrisPlusConstants.PREF_RINGTONE, ""));
            }

            Notification notification;

            if(silent) {
                notification = new Notification.Builder(mContext)
                        .setContentTitle("Iris+")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setWhen(System.currentTimeMillis())
                        .setTicker(message)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentIntent(pIntent).addAction(R.drawable.ic_drawer, "View", pIntent)
                        .build();
            } else {
                notification = new Notification.Builder(mContext)
                        .setContentTitle("Iris+")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setSound(soundUri)
                        .setVibrate(new long[]{1000, 1000})
                        .setWhen(System.currentTimeMillis())
                        .setTicker(message)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentIntent(pIntent).addAction(R.drawable.ic_drawer, "View", pIntent)
                        .build();
            }
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(notifyID, notification);
        }
    }

    public void destroyNotification(int notifyID) {
        if(notifyBar) {
            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notifyID);
        }
        if(notifyToast) {
            Toast.makeText(mContext, "Update completed.", Toast.LENGTH_SHORT).show();
        }
        if(notifyDialog && pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    public void destroyNotificationWithSoundAndVibrate() {
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(412999);
    }

    public void buttonFeedback() {
        if(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_TOUCH_FEEDBACK, true)) {
            Vibrator vb = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(IrisPlusConstants.VIBRATE_FEEDBACK_LENGTH);
        }
    }

    public void buttonFeedback(int duration) {
        if(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_TOUCH_FEEDBACK, true)) {
            Vibrator vb = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(duration);
        }
    }
}
