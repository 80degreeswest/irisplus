package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.DashboardListAdapter;
import com.eightydegreeswest.irisplus.adapters.HubListAdapter;
import com.eightydegreeswest.irisplus.apiv2.HubApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.HubFragment;
import com.eightydegreeswest.irisplus.model.DashboardItem;
import com.eightydegreeswest.irisplus.model.HubItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class HubViewTask extends AsyncTask<Void, Void, Boolean> {

	private HubFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();
	private SharedPreferences mSharedPrefs = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;
    private HubItem hubItem = new HubItem();
	private List<DashboardItem> hubDetails = new ArrayList<DashboardItem>();
	private HubListAdapter adapter = null;

	public HubViewTask(HubFragment fragment) {
		mFragment = fragment;
        mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();

		try {
			FileInputStream fileInputStream = mContext.openFileInput("irisplus-hub-list.dat");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			hubDetails = (ArrayList<DashboardItem>) objectInputStream.readObject();
			objectInputStream.close();

			adapter = mFragment.getHubListAdapter();

			if(adapter == null) {
				adapter = new HubListAdapter(mContext, hubDetails, mFragment);
				ListView deviceList = (ListView) mFragment.getActivity().findViewById(R.id.hub_fragment_view);
				deviceList.setAdapter(adapter);
				mFragment.setHubListAdapter(adapter);
			}
		} catch (Exception e) {
			//No cache - ignore
			//e.printStackTrace();
		}
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {

        HubApi irisApi = new HubApi(mContext);
        hubItem = irisApi.getHubDetails();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setHubViewTask(null);
            mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setHubViewTask(null);
        mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			hubDetails = new ArrayList<>();

			DashboardItem hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Name");
			hubDetailsItem.setStatus(hubItem.getHubName());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Model");
			hubDetailsItem.setStatus(hubItem.getModel());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("ID");
			hubDetailsItem.setStatus(hubItem.getId());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Hub Firmware Version");
			hubDetailsItem.setStatus(hubItem.getVersion());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Iris Platform Version");
			hubDetailsItem.setStatus(hubItem.getPlatformVersion());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Current State");
			hubDetailsItem.setStatus(hubItem.getState());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Power Source");
			hubDetailsItem.setStatus(hubItem.getPowerSource());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Battery Level");
			hubDetailsItem.setStatus(hubItem.getBattery() + "%");
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Mac Address");
			hubDetailsItem.setStatus(hubItem.getMacAddress());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Local IP");
			hubDetailsItem.setStatus(hubItem.getLocalIp());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("External IP");
			hubDetailsItem.setStatus(hubItem.getExternalIp());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Recommend Z-wave Rebuild");
			hubDetailsItem.setStatus(hubItem.getZwaveRebuildRecommended());
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Last Z-wave Rebuild");
			try {
				hubDetailsItem.setStatus(hubItem.getLastZwaveRebuild().toString());
			} catch (Exception e) {
				hubDetailsItem.setStatus("N/A");
				e.printStackTrace();
			}
			hubDetails.add(hubDetailsItem);

			hubDetailsItem = new DashboardItem();
			hubDetailsItem.setHeading("Last Restart Time");
			try {
				hubDetailsItem.setStatus(hubItem.getLastRestartTime().toString());
			} catch (Exception e) {
				hubDetailsItem.setStatus("N/A");
				e.printStackTrace();
			}
			hubDetails.add(hubDetailsItem);

			adapter = mFragment.getHubListAdapter();

			if(adapter == null) {
				adapter = new HubListAdapter(mContext, hubDetails, mFragment);
				ListView hubList = (ListView) mFragment.getActivity().findViewById(R.id.hub_fragment_view);
				hubList.setAdapter(adapter);
			} else {
				adapter.updateAdapterList(hubDetails);
			}
			mFragment.setHubListAdapter(adapter);

			try {
				FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-hub-list.dat", Context.MODE_PRIVATE);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(hubDetails);
				objectOutputStream.close();
			} catch (Exception e) {
				//No cache - ignore
				//e.printStackTrace();
			}

        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}