package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.DeviceListAdapter;
import com.eightydegreeswest.irisplus.apiv2.ControlApi;
import com.eightydegreeswest.irisplus.apiv2.DeviceApi;
import com.eightydegreeswest.irisplus.apiv2.LockApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.DeviceFragment;
import com.eightydegreeswest.irisplus.fragments.NavigationDrawerFragment;
import com.eightydegreeswest.irisplus.model.ControlItem;
import com.eightydegreeswest.irisplus.model.DeviceItem;
import com.eightydegreeswest.irisplus.model.LockItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class DeviceViewTask extends AsyncTask<Void, Void, Boolean> {

	private DeviceFragment mFragment = null;
	private NavigationDrawerFragment mNavFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<DeviceItem> devices = new ArrayList<DeviceItem>();
    private DeviceListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;
	private boolean mProblemOnly = false;

	public DeviceViewTask(DeviceFragment fragment) {
		mProblemOnly = false;
		initializeDeviceViewTask(fragment);
	}

	public DeviceViewTask(DeviceFragment fragment, boolean problemOnly) {
		mProblemOnly = problemOnly;
		initializeDeviceViewTask(fragment);
	}

	@SuppressWarnings("unchecked")
	private void initializeDeviceViewTask(DeviceFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
		notificationHelper = new NotificationHelper(mContext);
		notifyID = notificationHelper.createRefreshNotification();

		try {
			//Load cached list
			FileInputStream fileInputStream = mContext.openFileInput("irisplus-devices-list.dat");
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			devices = (ArrayList<DeviceItem>) objectInputStream.readObject();
			objectInputStream.close();

			adapter = mFragment.getDeviceListAdapter();

			if(adapter == null) {
				adapter = new DeviceListAdapter(mContext, devices, mFragment);
				ListView deviceList = (ListView) mFragment.getActivity().findViewById(R.id.device_fragment_view);
				deviceList.setAdapter(adapter);
				mFragment.setDeviceListAdapter(adapter);
			}

		} catch (Exception cacheException) {
			cacheException.printStackTrace();
		}
	}

	public DeviceViewTask(NavigationDrawerFragment fragment) {
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
			mFragment.setDeviceViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		} else {
			this.performNavDrawerCommand();
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setDeviceViewTask(null);
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

			if(devices.size() > 0 && mProblemOnly) {
				//Filter out to show only problem devices
				List<DeviceItem> problemDevices = new ArrayList<DeviceItem>();
				for(DeviceItem deviceItem : devices) {
					if("OFFLINE".equalsIgnoreCase(deviceItem.getStatus())) {
						problemDevices.add(deviceItem);
					}

					try {
						if(!"n/a".equalsIgnoreCase(deviceItem.getSignal()) && Integer.parseInt(deviceItem.getSignal()) < 20) {
							problemDevices.add(deviceItem);
						}
					} catch(Exception e) { }

					try {
						if(!"ac".equalsIgnoreCase(deviceItem.getBatteryPercentage()) && Integer.parseInt(deviceItem.getBatteryPercentage()) < 30) {
							problemDevices.add(deviceItem);
						}
					} catch(Exception e) { }
				}
				if(problemDevices.size() > 0) {
					devices = new ArrayList<>();
					devices.addAll(problemDevices);
				}
			}

            adapter = mFragment.getDeviceListAdapter();

            if(adapter == null) {
                adapter = new DeviceListAdapter(mContext, devices, mFragment);
                ListView deviceList = (ListView) mFragment.getActivity().findViewById(R.id.device_fragment_view);
                deviceList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(devices);
            }
            mFragment.setDeviceListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-devices-list.dat", Context.MODE_PRIVATE);
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

	protected void performNavDrawerCommand() {
		try {
			if (devices.size() > 0) {
				Collections.sort(devices, new Comparator<DeviceItem>() {
					@Override
					public int compare(final DeviceItem object1, final DeviceItem object2) {
						return object1.getDeviceName().compareTo(object2.getDeviceName());
					}
				} );
			}

			try {
				//Cache list
				FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-nav-list.dat", Context.MODE_PRIVATE);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				objectOutputStream.writeObject(devices);
				objectOutputStream.close();
			} catch (Exception cacheException) {
				//Ignore
				cacheException.printStackTrace();
			}
			//mNavFragment.refreshFragment();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}