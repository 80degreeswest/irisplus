package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.apiv2.CareApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.CareFragment;

public class CareDoTask extends AsyncTask<Void, Void, Boolean> {

	private CareFragment mFragment = null;
	private Context mContext = null;

	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private boolean mStatus = false;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	public CareDoTask(CareFragment fragment, boolean status) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mStatus = status;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		
        CareApi irisApi = new CareApi(mContext);
        irisApi.setCareStatus(mStatus);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		if(mFragment.isAdded()) {
            mFragment.refreshFragment();
			mFragment.setCareDoTask(null);
            //mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setCareDoTask(null);
        //mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
}