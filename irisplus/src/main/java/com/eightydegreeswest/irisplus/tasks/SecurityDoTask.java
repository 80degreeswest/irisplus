package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.apiv2.SecurityApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.SecurityFragment;

public class SecurityDoTask extends AsyncTask<Void, Void, Boolean> {
	
	private SecurityFragment mFragment = null;
	private Context mContext = null;
	private String alarmStatus = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private String mAction = null;
	private String mParam = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;
	
	public SecurityDoTask(SecurityFragment fragment, String action, String param) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mAction = action;
		mParam = param;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		
        SecurityApi irisApi = new SecurityApi(mContext);
        
        if("alarm".equals(mAction)) {
            if("cancel".equalsIgnoreCase(mParam)) {
                irisApi.clearCurrentAlarms();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                alarmStatus = irisApi.getAlarmStatus();
                if (!mParam.equalsIgnoreCase(alarmStatus)) {
                    irisApi.setAlarm(mParam, true);
                }
            }
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		if(mFragment.isAdded()) {
            mFragment.refreshFragment();
			mFragment.setSecurityDoTask(null);
            mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setSecurityDoTask(null);
        mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
}