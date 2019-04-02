package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.ControlListAdapter;
import com.eightydegreeswest.irisplus.apiv2.ControlApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.ControlFragment;
import com.eightydegreeswest.irisplus.model.ControlItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ControlViewTask extends AsyncTask<Void, Void, Boolean> {
	
	private ControlFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<ControlItem> controls = new ArrayList<ControlItem>();
    private ControlListAdapter adapter = null;
    private List<ControlItem> groups = null;
    private List<ControlItem> devices = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;
	
	@SuppressWarnings("unchecked")
	public ControlViewTask(ControlFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
		
		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-control-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        controls = (ArrayList<ControlItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getControlListAdapter();

            if(adapter == null) {
                adapter = new ControlListAdapter(mContext, controls, mFragment);
                ListView controlList = (ListView) mFragment.getActivity().findViewById(R.id.control_fragment_view);
                controlList.setAdapter(adapter);
                mFragment.setControlListAdapter(adapter);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	@Override
	protected Boolean doInBackground(Void... params) {
        ControlApi irisApi = new ControlApi(mContext);
        devices = irisApi.getAllControls();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setControlViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setControlViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			controls = new ArrayList<ControlItem>();

			if(groups != null) {
				controls.addAll(groups);
			}
			if(devices != null) {
				controls.addAll(devices);
			}
			if(controls == null || controls.size() == 0) {
                Toast.makeText(mContext, "You do not have any controls on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            } else {
				Collections.sort(controls, new Comparator<ControlItem>() {
					@Override
					public int compare(final ControlItem object1, final ControlItem object2) {
						return object1.getControlName().compareTo(object2.getControlName());
					}
				} );
			}

            adapter = mFragment.getControlListAdapter();

            if(adapter == null) {
                adapter = new ControlListAdapter(mContext, controls, mFragment);
                ListView controlList = (ListView) mFragment.getActivity().findViewById(R.id.control_fragment_view);
                controlList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(controls);
            }
            mFragment.setControlListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-control-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(controls);
		        objectOutputStream.close();
	        } catch (Exception cacheException) {
	        	//Ignore
	        	cacheException.printStackTrace();
	        }
            /*
            //Update widget
            try {
                Intent i = new Intent(mContext, ControlWidget.class);
                i.setAction(ControlWidget.UPDATE);
                mContext.sendBroadcast(i);
            } catch (Exception e) {
                //No widget?
                logger.log(IrisPlusConstants.LOG_INFO, "Error: Could not update Control widget, Error message: " + e.getMessage());
            }
	        */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}