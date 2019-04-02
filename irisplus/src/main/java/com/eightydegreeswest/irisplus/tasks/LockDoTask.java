package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.apiv2.LockApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.LockFragment;

public class LockDoTask extends AsyncTask<Void, Void, Boolean> {

	private LockFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private String mID = null;
	private String mState = null;
    private int notifyID = 0;
    private boolean mBuzzin = false;
	private String mLockType = null;
    NotificationHelper notificationHelper;

	public LockDoTask(LockFragment fragment, String id, String lockType, ToggleButton status, boolean buzzin) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mID = id;
        mBuzzin = buzzin;
		mLockType = lockType;
		if("Lock".equalsIgnoreCase(mLockType)) {
			mState = status.isChecked() ? "\"doorlock:lockstate\":\"LOCKED\"" : "\"doorlock:lockstate\":\"UNLOCKED\"";
		} else if("Garage Door".equalsIgnoreCase(mLockType)) {
			mState = status.isChecked() ? "\"motdoor:doorstate\":\"CLOSED\"" : "\"motdoor:doorstate\":\"OPEN\"";
		}
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        if(mBuzzin) {
            LockApi irisApi = new LockApi(mContext);
            irisApi.setLockBuzzIn(mID);
            try {
                Thread.sleep(70000);
            } catch (Exception e) {
                //Ignore and continue
            }
        } else {
            LockApi irisApi = new LockApi(mContext);
            irisApi.setLockState(mID, mState);
            try {
				if("Lock".equalsIgnoreCase(mLockType)) {
					Thread.sleep(5000);
				} else if("Garage Door".equalsIgnoreCase(mLockType)) {
					Thread.sleep(30000);
				}
            } catch (Exception e) {
                //Ignore and continue
            }
        }
        return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		if(mFragment.isAdded()) {
            if(mBuzzin) {
                Toast.makeText(mContext, "Lock has been re-locked", Toast.LENGTH_LONG).show();
            }
            mFragment.refreshFragment();
			mFragment.setLockDoTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setLockDoTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
}