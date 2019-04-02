package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.EnergyUsageListAdapter;
import com.eightydegreeswest.irisplus.apiv2.DeviceApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.EnergyUsageFragment;
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

public class EnergyUsageViewTask extends AsyncTask<Void, Void, Boolean> {

	private EnergyUsageFragment mFragment = null;
	private NavigationDrawerFragment mNavFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<DeviceItem> devices = new ArrayList<DeviceItem>();
    private EnergyUsageListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	@SuppressWarnings("unchecked")
	public EnergyUsageViewTask(EnergyUsageFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();

		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-energy-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        devices = (ArrayList<DeviceItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getEnergyUsageListAdapter();

            if(adapter == null) {
                adapter = new EnergyUsageListAdapter(mContext, devices, mFragment);
                ListView deviceList = (ListView) mFragment.getActivity().findViewById(R.id.energy_fragment_view);
                deviceList.setAdapter(adapter);
                mFragment.setEnergyUsageListAdapter(adapter);
            }

        } catch (Exception cacheException) {
        	cacheException.printStackTrace();
        }
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
			mFragment.setEnergyUsageViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setEnergyUsageViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			List<DeviceItem> energyDevices = new ArrayList<>();
			if(devices.size() > 0) {
				for(DeviceItem deviceItem : devices) {
					if(deviceItem.getPower() != null) {
						energyDevices.add(deviceItem);
					}
				}
			}
            if (energyDevices.size() > 0) {
                Collections.sort(energyDevices, new Comparator<DeviceItem>() {
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

            adapter = mFragment.getEnergyUsageListAdapter();

            if(adapter == null) {
                adapter = new EnergyUsageListAdapter(mContext, energyDevices, mFragment);
                ListView deviceList = (ListView) mFragment.getActivity().findViewById(R.id.energy_fragment_view);
                deviceList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(energyDevices);
            }
            mFragment.setEnergyUsageListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-energy-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(energyDevices);
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