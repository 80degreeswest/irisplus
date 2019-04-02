package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.apiv2.ThermostatApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.ThermostatFragment;

public class ThermostatDoTask extends AsyncTask<Void, Void, Boolean> {

	private ThermostatFragment mFragment = null;
	private Context mContext = null;

	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private String mAction = null;
	private String mParam = null;
    private int notifyID = 0;
    private String mTargetHeat = null;
    private String mTargetCool = null;
    private String mThermostatID = null;
    NotificationHelper notificationHelper;

	public ThermostatDoTask(ThermostatFragment fragment, String action, String param, String targetHeat, String targetCool, String thermostatID) {
		mFragment = fragment;
        mContext = IrisPlus.getContext();
		mAction = action;
		mParam = param;
        mTargetHeat = targetHeat;
        mTargetCool = targetCool;
        mThermostatID = thermostatID;

        if(mThermostatID == null) {
            try {
                mThermostatID = mFragment.getThermostats().get(0).getId();
            } catch (Exception e) {
                mThermostatID = "";
            }
        }

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		
        ThermostatApi irisApi = new ThermostatApi(mContext);
        
        if("temperatureHeat".equalsIgnoreCase(mAction)) {
            irisApi.setTemperature(mThermostatID, mParam, "heat");
        } else if("temperatureCool".equalsIgnoreCase(mAction)) {
            irisApi.setTemperature(mThermostatID, mParam, "cool");
        } else if("mode".equalsIgnoreCase(mAction)) {
            irisApi.setThermostatMode(mThermostatID, mParam);
        } else if("filter".equalsIgnoreCase(mAction)) {
            irisApi.resetThermostatFilter(mThermostatID);
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
            mFragment.userAction = false;
            mFragment.refreshFragment();
			mFragment.setThermostatDoTask(null);
            //mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setThermostatDoTask(null);
        //mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
}