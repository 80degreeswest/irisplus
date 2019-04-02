package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.apiv2.CareApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.CareFragment;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class CareViewTask extends AsyncTask<Void, Void, Boolean> {

	private CareFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
    private HashMap<String, String> careAlerts = new HashMap<String, String>();
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	public CareViewTask(CareFragment fragment) {
		mFragment = fragment;
        mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {

        CareApi irisApi = new CareApi(mContext);
        careAlerts = irisApi.getCareStatus();
        return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setCareViewTask(null);
            //mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setCareViewTask(null);
        //mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
            TextView stateTxt = (TextView) mFragment.getActivity().findViewById(R.id.care_state_content);
            TextView alarmedTxt = (TextView) mFragment.getActivity().findViewById(R.id.care_alarmed_content);
            TextView timeTxt = (TextView) mFragment.getActivity().findViewById(R.id.care_time_content);
            TextView devicesTxt = (TextView) mFragment.getActivity().findViewById(R.id.care_devices_content);
            ToggleButton status = (ToggleButton) mFragment.getActivity().findViewById(R.id.care_status);
            RelativeLayout careLayout = (RelativeLayout) mFragment.getActivity().findViewById(R.id.care_swipe_container);

            if(careAlerts == null || careAlerts.isEmpty()) {
                careLayout.setVisibility(View.GONE);
                Toast.makeText(mContext, "You do not have any care devices on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            } else {
                careLayout.setVisibility(View.VISIBLE);
                status.setChecked(Boolean.getBoolean(careAlerts.get("alertsEnabled")));
                Date date = new Date(Long.parseLong(careAlerts.get("time")) * 1000);
                Format formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
                String dateStr = formatter.format(date);

                Spanned content = Html.fromHtml("<big><b>" + careAlerts.get("state") + "</b></big>");
                stateTxt.setText(content);

                content = Html.fromHtml("<big><b>" + careAlerts.get("alarmed") + "</b></big>");
                alarmedTxt.setText(content);

                content = Html.fromHtml("<big><b>" + dateStr + "</b></big>");
                timeTxt.setText(content);

                content = Html.fromHtml("<big><b>" + careAlerts.get("devices") + "</b></big>");
                devicesTxt.setText(content);
            }
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}