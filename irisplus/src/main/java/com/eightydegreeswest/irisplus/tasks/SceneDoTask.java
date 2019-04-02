package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.apiv2.SceneApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.SceneFragment;

public class SceneDoTask extends AsyncTask<Void, Void, Boolean> {

	private SceneFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private String mID = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	public SceneDoTask(SceneFragment fragment, String id) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mID = id;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        SceneApi irisApi = new SceneApi(mContext);
		irisApi.runScene(mID);
        return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
		Toast.makeText(mContext, "Scene has run successfully.", Toast.LENGTH_LONG).show();
		mFragment.setSceneDoTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
		notificationHelper.destroyNotification(notifyID);
	}

	@Override
	protected void onCancelled() {
		mFragment.setSceneDoTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
}