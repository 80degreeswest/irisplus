package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.apiv2.SecurityApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.SecurityFragment;

public class SecurityViewTask extends AsyncTask<Void, Void, Boolean> {
	
	private SecurityFragment mFragment = null;
	private Context mContext = null;
	private String alarmStatus = null;
    private boolean showCancel = false;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;
	
	public SecurityViewTask(SecurityFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        SecurityApi irisApi = new SecurityApi(mContext);
        alarmStatus = irisApi.getAlarmStatus();
        showCancel = false;

		return alarmStatus != null;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setSecurityViewTask(null);
            mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setSecurityViewTask(null);
        mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			Button btnAlarmHome = (Button) mFragment.getActivity().findViewById(R.id.btn_alarm_home);
			Button btnAlarmAway = (Button) mFragment.getActivity().findViewById(R.id.btn_alarm_away);
			Button btnAlarmNight = (Button) mFragment.getActivity().findViewById(R.id.btn_alarm_night);
			Button btnAlarmPanic = (Button) mFragment.getActivity().findViewById(R.id.btn_alarm_panic);
			TextView alarmStatusView = (TextView) mFragment.getActivity().findViewById(R.id.alarm_status);

			btnAlarmPanic.setVisibility(View.VISIBLE);
	        
	        if("OFF".equalsIgnoreCase(alarmStatus)) {
	        	btnAlarmHome.setVisibility(View.GONE);
				btnAlarmAway.setVisibility(View.VISIBLE);
				btnAlarmNight.setVisibility(View.VISIBLE);
				alarmStatusView.setBackgroundResource(R.drawable.greengradient);
	        } else if("ON".equalsIgnoreCase(alarmStatus)) {
				btnAlarmHome.setVisibility(View.VISIBLE);
				btnAlarmAway.setVisibility(View.GONE);
				btnAlarmNight.setVisibility(View.VISIBLE);
				alarmStatusView.setBackgroundResource(R.drawable.redgradient);
	        } else if("PARTIAL".equalsIgnoreCase(alarmStatus)) {
				btnAlarmHome.setVisibility(View.VISIBLE);
				btnAlarmAway.setVisibility(View.VISIBLE);
				btnAlarmNight.setVisibility(View.GONE);
				alarmStatusView.setBackgroundResource(R.drawable.yellowgradient);
	        }
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}