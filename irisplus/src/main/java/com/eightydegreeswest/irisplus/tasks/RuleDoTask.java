package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.apiv2.RuleApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.RuleFragment;

public class RuleDoTask extends AsyncTask<Void, Void, Boolean> {

	private RuleFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private String mID = null;
	private String mState = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	public RuleDoTask(RuleFragment fragment, String id, ToggleButton status) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mID = id;
		mState = status.isChecked() ? "ENABLED" : "DISABLED";
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        RuleApi irisApi = new RuleApi(mContext);
		irisApi.toggleRuleStatus(mID, mState);
        return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		mFragment.setRuleDoTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
		notificationHelper.destroyNotification(notifyID);
	}

	@Override
	protected void onCancelled() {
		mFragment.setRuleDoTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
}