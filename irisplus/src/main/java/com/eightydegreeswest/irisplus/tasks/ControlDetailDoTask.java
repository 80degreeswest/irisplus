package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.apiv2.ControlApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.ControlFragment;

public class ControlDetailDoTask extends AsyncTask<Void, Void, Boolean> {
	
	private ControlFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private String mID = null;
	private int mIntensity = 0;
    private String mSpeed = null;
    private String mBlindState = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;
	
	public ControlDetailDoTask(ControlFragment fragment, String id, int intensity, String speed, String blindState) {
		mFragment = fragment;
        mContext = IrisPlus.getContext();
		mID = id;
		mIntensity = intensity;
        mSpeed = speed;
        mBlindState = blindState;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        if(mSpeed != null && !"".equalsIgnoreCase(mSpeed)) {
            ControlApi irisApi = new ControlApi(mContext);
            if("OFF".equalsIgnoreCase(mSpeed)) {
                irisApi.setDeviceState(mID, mSpeed);
            } else {
                irisApi.setFanSpeed(mID, mSpeed);
            }
        } else if(mBlindState != null && !"".equalsIgnoreCase(mBlindState)) {
            ControlApi irisApi = new ControlApi(mContext);
            irisApi.setBlindState(mID, mBlindState);
        } else {
            if(mIntensity < 0 && mIntensity > -999) {
                mIntensity = 0;
            } else if(mIntensity > 100 && mIntensity < 999) {
                mIntensity = 10;
            }
            ControlApi irisApi = new ControlApi(mContext);
            irisApi.setDeviceIntensity(mID, mIntensity);
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		if(mFragment.isAdded()) {
            mFragment.refreshFragment();
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
			mFragment.setControlDetailDoTask(null);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
		mFragment.setControlDetailDoTask(null);
        notificationHelper.destroyNotification(notifyID);
	}
}