package com.eightydegreeswest.irisplus.automation.homestatus;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eightydegreeswest.irisplus.common.AlarmManagerHelper;
import com.eightydegreeswest.irisplus.common.AutomationBroadcast;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

public class HomeStatusCheckBroadcast extends BroadcastReceiver {

    private static HomeStatusCheckTask homeStatusCheckTask = null;
    private IrisPlusLogger logger = new IrisPlusLogger();

	@Override
	public void onReceive(Context context, Intent intent) {
        setHomeStatusCheckTask(new HomeStatusCheckTask(context));
        TaskHelper.execute(getHomeStatusCheckTask());
        Intent i = new Intent(context, AutomationBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, IrisPlusConstants.ALARM_AUTOMATION_SINGLE, i, 0);
        AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
        alarmManagerHelper.scheduleSingleAlarm(context, pendingIntent);
	}

    public HomeStatusCheckTask getHomeStatusCheckTask() {
        return homeStatusCheckTask;
    }

    public static void setHomeStatusCheckTask(HomeStatusCheckTask homeStatusCheckTask) {
        com.eightydegreeswest.irisplus.automation.homestatus.HomeStatusCheckBroadcast.homeStatusCheckTask = homeStatusCheckTask;
    }
}
