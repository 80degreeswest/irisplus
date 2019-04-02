package com.eightydegreeswest.irisplus.automation.homestatus;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.apiv2.DashboardApi;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DashboardItem;

import java.util.Date;
import java.util.List;

public class HomeStatusCheckTask extends AsyncTask<Void, Void, Boolean> {

	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
    NotificationHelper notificationHelper;
    List<DashboardItem> dashboardItems;
    long lastAlert = 0;

	public HomeStatusCheckTask(Context context) {
		mContext = context;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        lastAlert = Long.parseLong(mSharedPrefs.getString("homeStatusLastAlert", "0"));
        DashboardApi irisApi = new DashboardApi(mContext);
        dashboardItems = irisApi.getDashboard();
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        logger.log(IrisPlusConstants.LOG_INFO, "Home Status task started");
        return dashboardItems != null;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
        try {
            if(success) {
                int snoozeHours = Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_HOME_STATUS_CHECK_INTERVAL_SNOOZE, "4"));
                for(DashboardItem item : dashboardItems) {
                    if (item.getHeading().equalsIgnoreCase("Home Status") && !item.getStatus().equalsIgnoreCase("All OK!") && new Date().getTime() > (lastAlert + snoozeHours * 60 * 60 * 1000)) {
                        notificationHelper = new NotificationHelper(mContext);
                        notificationHelper.createNotificationWithSoundAndVibrateAndIntent(item.getStatus().replaceAll("<br>", " "), "devices");
                        mSharedPrefs.edit().putString("homeStatusLastAlert", Long.toString(new Date().getTime())).commit();
                        mSharedPrefs.edit().putBoolean("homeStatusAlerted", true).commit();
                    }
                }
            }
            HomeStatusCheckBroadcast.setHomeStatusCheckTask(null);
        } catch (Exception e) {
            //Ignore - the broadcast was cancelled
        }
	}

    @Override
    protected void onCancelled() {
        HomeStatusCheckBroadcast.setHomeStatusCheckTask(null);
    }
}