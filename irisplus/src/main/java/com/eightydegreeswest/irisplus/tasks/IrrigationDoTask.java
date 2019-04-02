package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.apiv2.IrrigationApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.IrrigationFragment;

public class IrrigationDoTask extends AsyncTask<Void, Void, Boolean> {

	private IrrigationFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private String mID = null;
	private String mState = null;
    private String mZone = null;
    private String mDuration = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	public IrrigationDoTask(IrrigationFragment fragment, String id, String state, String zone, String duration) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mID = id;
		mState = state;
        mZone = zone;
        mDuration = duration;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        IrrigationApi irisApi = new IrrigationApi(mContext);
        if("manual".equalsIgnoreCase(mState) || "schedule".equalsIgnoreCase(mState)) {
            //Update control
            irisApi.setIrrigationControl(mID, mState);
        } else if("start".equalsIgnoreCase(mState)) {
            //Start watering
            irisApi.setIrrigationStart(mID, mZone, mDuration);
        } else if("stop".equalsIgnoreCase(mState) || "stop delay".equalsIgnoreCase(mState)) {
            //Stop watering
            irisApi.setIrrigationStop(mID);
        } else {
            //Update delay
            irisApi.setIrrigationDelay(mID, mState);
        }

        return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		if(mFragment.isAdded()) {
            //mFragment.refreshFragment();
			mFragment.setIrrigationDoTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setIrrigationDoTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
}