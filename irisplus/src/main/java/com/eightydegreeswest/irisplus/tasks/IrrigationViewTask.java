package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.IrrigationListAdapter;
import com.eightydegreeswest.irisplus.apiv2.IrrigationApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.IrrigationFragment;
import com.eightydegreeswest.irisplus.model.IrrigationItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class IrrigationViewTask extends AsyncTask<Void, Void, Boolean> {

	private IrrigationFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<IrrigationItem> irrigations = new ArrayList<IrrigationItem>();
	private List<IrrigationItem> apiReturn = new ArrayList<IrrigationItem>();
    private IrrigationListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	@SuppressWarnings("unchecked")
	public IrrigationViewTask(IrrigationFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();

		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-irrigation-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        irrigations = (ArrayList<IrrigationItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getIrrigationListAdapter();

            if(adapter == null) {
                adapter = new IrrigationListAdapter(mContext, irrigations, mFragment);
                ListView irrigationList = (ListView) mFragment.getActivity().findViewById(R.id.irrigation_fragment_view);
                irrigationList.setAdapter(adapter);
                mFragment.setIrrigationListAdapter(adapter);
            }
        } catch (Exception cacheException) {
        	cacheException.printStackTrace();
        }
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        IrrigationApi irisApi = new IrrigationApi(mContext);
        apiReturn = irisApi.getIrrigationList();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setIrrigationViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setIrrigationViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			irrigations = new ArrayList<IrrigationItem>();

			if(apiReturn != null) {
				irrigations.addAll(apiReturn);
			}

			if(irrigations == null || irrigations.size() == 0) {
                Toast.makeText(mContext, "You do not have any irrigation devices on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            }

            adapter = mFragment.getIrrigationListAdapter();

            if(adapter == null) {
                adapter = new IrrigationListAdapter(mContext, irrigations, mFragment);
                ListView irrigationList = (ListView) mFragment.getActivity().findViewById(R.id.irrigation_fragment_view);
                irrigationList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(irrigations);
            }
            mFragment.setIrrigationListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-irrigation-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(irrigations);
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