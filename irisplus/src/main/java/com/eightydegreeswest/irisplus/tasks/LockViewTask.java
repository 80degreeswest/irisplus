package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.LockListAdapter;
import com.eightydegreeswest.irisplus.apiv2.LockApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.LockFragment;
import com.eightydegreeswest.irisplus.model.LockItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LockViewTask extends AsyncTask<Void, Void, Boolean> {

	private LockFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<LockItem> locks = new ArrayList<LockItem>();
	private List<LockItem> apiReturn = new ArrayList<LockItem>();
    private LockListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	@SuppressWarnings("unchecked")
	public LockViewTask(LockFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
		
		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-lock-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        locks = (ArrayList<LockItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getLockListAdapter();

            if(adapter == null) {
                adapter = new LockListAdapter(mContext, locks, mFragment);
                ListView lockList = (ListView) mFragment.getActivity().findViewById(R.id.lock_fragment_view);
                lockList.setAdapter(adapter);
                mFragment.setLockListAdapter(adapter);
            }
        } catch (Exception cacheException) {
        	//Ignore
        	cacheException.printStackTrace();
        }
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        LockApi irisApi = new LockApi(mContext);
        apiReturn = irisApi.getAllLocks();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setLockViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setLockViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			locks = new ArrayList<LockItem>();

			if(apiReturn != null) {
				locks.addAll(apiReturn);
			}
			if(locks == null || locks.size() == 0) {
                Toast.makeText(mContext, "You do not have any locks on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            }

            adapter = mFragment.getLockListAdapter();

            if(adapter == null) {
                adapter = new LockListAdapter(mContext, locks, mFragment);
                ListView lockList = (ListView) mFragment.getActivity().findViewById(R.id.lock_fragment_view);
                lockList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(locks);
            }
            mFragment.setLockListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-lock-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(locks);
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