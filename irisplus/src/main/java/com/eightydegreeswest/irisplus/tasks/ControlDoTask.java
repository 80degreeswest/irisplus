package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.apiv2.ControlApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.ControlFragment;

public class ControlDoTask extends AsyncTask<Void, Void, Boolean> {
	
	private ControlFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private String mID = null;
	private String mState = null;
	private boolean mLockStateClosed = true; //true = locked, garage door is closed, false = unlocked, garage door is open
	private String mType = null;
    private String mControlType = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;
	
	public ControlDoTask(ControlFragment fragment, String id, String type, ToggleButton status) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mID = id;
		mState = status.isChecked() ? "on" : "off";
		mLockStateClosed = !status.isChecked();
		mType = type;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		logger.log(IrisPlusConstants.LOG_INFO, "Starting background controls task.");
		ControlApi irisApi = new ControlApi(mContext);
		irisApi.setDeviceState(mID, mState);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

		logger.log(IrisPlusConstants.LOG_INFO, "Ending background controls task.");
        
        return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		logger.log(IrisPlusConstants.LOG_INFO, "Post execute controls task. Fragment added? = " + mFragment.isAdded());
		if(mFragment.isAdded()) {
            mFragment.refreshFragment();
			mFragment.setControlDoTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
		logger.log(IrisPlusConstants.LOG_INFO, "End post execute controls task.");
	}

	@Override
	protected void onCancelled() {
		mFragment.setControlDoTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
}