package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.IftttListAdapter;
import com.eightydegreeswest.irisplus.apiv2.DeviceApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.IftttFragment;
import com.eightydegreeswest.irisplus.fragments.NavigationDrawerFragment;
import com.eightydegreeswest.irisplus.model.DeviceItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class IftttViewTask extends AsyncTask<Void, Void, Boolean> {

	private IftttFragment mFragment = null;
	private NavigationDrawerFragment mNavFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<DeviceItem> devices = new ArrayList<DeviceItem>();
    private IftttListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;
	private boolean mProblemOnly = false;

	public IftttViewTask(IftttFragment fragment) {
		mProblemOnly = false;
		initializeIftttViewTask(fragment);
	}

	public IftttViewTask(IftttFragment fragment, boolean problemOnly) {
		mProblemOnly = problemOnly;
		initializeIftttViewTask(fragment);
	}

	@SuppressWarnings("unchecked")
	private void initializeIftttViewTask(IftttFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
		notificationHelper = new NotificationHelper(mContext);
		notifyID = notificationHelper.createRefreshNotification();

		try {
			//Load cached list
			FileInputStream fileInputStream = mContext.openFileInput("irisplus-ifttts-list.dat");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			devices = (ArrayList<DeviceItem>) objectInputStream.readObject();
			objectInputStream.close();

			adapter = mFragment.getIftttListAdapter();

			if(adapter == null) {
				adapter = new IftttListAdapter(mContext, devices, mFragment);
				ListView iftttList = (ListView) mFragment.getActivity().findViewById(R.id.ifttt_fragment_view);
				iftttList.setAdapter(adapter);
				mFragment.setIftttListAdapter(adapter);
			}

		} catch (Exception cacheException) {
			cacheException.printStackTrace();
		}
	}

	public IftttViewTask(NavigationDrawerFragment fragment) {
		mNavFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        DeviceApi deviceApi = new DeviceApi(mContext);
        devices = deviceApi.getHomeStatus();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment != null && mFragment.isAdded()) {
            this.performCommand();
			mFragment.setIftttViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setIftttViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
            if (devices.size() > 0) {
                Collections.sort(devices, new Comparator<DeviceItem>() {
                    @Override
                    public int compare(final DeviceItem object1, final DeviceItem object2) {
                        return object1.getDeviceName().compareTo(object2.getDeviceName());
                    }
                } );
            } else {
                Toast.makeText(mContext, "You do not have any devices on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            }

            adapter = mFragment.getIftttListAdapter();

            if(adapter == null) {
                adapter = new IftttListAdapter(mContext, devices, mFragment);
                ListView iftttList = (ListView) mFragment.getActivity().findViewById(R.id.ifttt_fragment_view);
                iftttList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(devices);
            }
            mFragment.setIftttListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-ifttts-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(devices);
		        objectOutputStream.close();
	        } catch (Exception cacheException) {
	        	//Ignore
	        	cacheException.printStackTrace();
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}