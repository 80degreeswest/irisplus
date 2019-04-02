package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.PresenceListAdapter;
import com.eightydegreeswest.irisplus.apiv2.PresenceApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.PresenceFragment;
import com.eightydegreeswest.irisplus.model.PresenceItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PresenceViewTask extends AsyncTask<Void, Void, Boolean> {

	private PresenceFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<PresenceItem> keyfobs = new ArrayList<PresenceItem>();
	private List<PresenceItem> apiReturn = new ArrayList<PresenceItem>();
    private int phonesAtHome = 0;
	private String devicesAtHome = "";
    private PresenceListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	@SuppressWarnings("unchecked")
	public PresenceViewTask(PresenceFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();

		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-presence-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        keyfobs = (ArrayList<PresenceItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getPresenceListAdapter();

            if(adapter == null) {
                adapter = new PresenceListAdapter(mContext, keyfobs, mFragment);
                ListView presenceList = (ListView) mFragment.getActivity().findViewById(R.id.presence_fragment_view);
                presenceList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(keyfobs);
            }
            mFragment.setPresenceListAdapter(adapter);

        } catch (Exception cacheException) {
        	//Ignore
        	cacheException.printStackTrace();
        }
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        PresenceApi irisApi = new PresenceApi(mContext);
        apiReturn = irisApi.getAllKeyfobs();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setPresenceViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setPresenceViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			keyfobs = new ArrayList<PresenceItem>();

			if(apiReturn != null) {
				keyfobs.addAll(apiReturn);
			}

            if(keyfobs == null || keyfobs.size() == 0) {
                Toast.makeText(mContext, "You do not have any keyfobs on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
            }

            adapter = mFragment.getPresenceListAdapter();

            if(adapter == null) {
                adapter = new PresenceListAdapter(mContext, keyfobs, mFragment);
                ListView presenceList = (ListView) mFragment.getActivity().findViewById(R.id.presence_fragment_view);
                presenceList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(keyfobs);
            }
            mFragment.setPresenceListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-presence-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(keyfobs);
		        objectOutputStream.close();
	        } catch (Exception cacheException) {
	        	cacheException.printStackTrace();
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}