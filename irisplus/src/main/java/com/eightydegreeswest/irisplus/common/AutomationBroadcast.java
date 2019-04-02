package com.eightydegreeswest.irisplus.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.automation.homestatus.HomeStatusCheckBroadcast;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

import java.util.Calendar;

/*
    This class is used for rules set inside the app - does not execute ifttt or dropbox rules
 */
public class AutomationBroadcast extends BroadcastReceiver {

    private IrisPlusLogger logger = new IrisPlusLogger();
    private Context context;
    private SharedPreferences mSharedPrefs;
    boolean useIftttForRules = false;

	@Override
	public void onReceive(Context context, Intent intent) {
        this.context = context;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        useIftttForRules = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_IFTTT, false);
        this.initializeAutomationListener();
        this.checkHomeStatus();
	}

    private void initializeAutomationListener() {
        if(useIftttForRules) {
            if (AutomationService.service != null && AutomationService.isRunning()) {
                AutomationService.service.updateAutomationListener();
            } else {
                context.startService(new Intent(context, AutomationService.class));
            }
        } else if (AutomationService.service != null && AutomationService.isRunning()) {
            context.stopService(new Intent(context, AutomationService.class));
        }
    }

    private void checkHomeStatus() {
        try {

            SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String homeStatusCheck = mSharedPrefs.getString(IrisPlusConstants.PREF_HOME_STATUS_CHECK_INTERVAL, "60");

            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, HomeStatusCheckBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IrisPlusConstants.ALARM_HOME_STATUS_REPEAT, i, 0);

            if("0".equalsIgnoreCase(homeStatusCheck)) {
                logger.log(IrisPlusConstants.LOG_INFO, "Cancelling home status schedule.");
                alarmManager.cancel(pendingIntent);
            } else {
                String prevHomeStatusCheck = mSharedPrefs.getString("prevHomeStatusCheck", "1");
                boolean alarmExists = (PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_NO_CREATE) != null);
                if(!prevHomeStatusCheck.equals(homeStatusCheck) || !alarmExists) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, Integer.parseInt(homeStatusCheck));
                    logger.log(IrisPlusConstants.LOG_INFO, "Home Status check scheduled for " + calendar.getTime().toString());

                    AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
                    alarmManagerHelper.scheduleSingleAlarmFuture(context, pendingIntent, calendar.getTimeInMillis());
                    mSharedPrefs.edit().putString("prevHomeStatusCheck", homeStatusCheck);
                }
            }
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_INFO, "error: trying to schedule home status check failed. " + e.getMessage());
            e.printStackTrace();
        }
    }
}
