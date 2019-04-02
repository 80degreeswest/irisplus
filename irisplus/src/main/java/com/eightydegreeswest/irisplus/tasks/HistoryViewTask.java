package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.HistoryListAdapter;
import com.eightydegreeswest.irisplus.apiv2.HistoryApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.HistoryFragment;
import com.eightydegreeswest.irisplus.model.HistoryItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HistoryViewTask extends AsyncTask<Void, Void, Boolean> {
	
	private HistoryFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<HistoryItem> historyItems = new ArrayList<HistoryItem>();
	private List<HistoryItem> apiReturn = new ArrayList<HistoryItem>();
    private HistoryListAdapter adapter = null;
    private int notifyID = 0;
    private String mOffsetId = null;
    private int mLimit = 0;
    NotificationHelper notificationHelper;
	
	@SuppressWarnings("unchecked")
	public HistoryViewTask(HistoryFragment fragment, int limit, String offsetId) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
        mOffsetId = offsetId;
        mLimit = limit;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();

		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-history-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        historyItems = (ArrayList<HistoryItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getHistoryListAdapter();

            if(adapter == null) {
                adapter = new HistoryListAdapter(mContext, historyItems, mFragment);
                ListView historyList = (ListView) mFragment.getActivity().findViewById(R.id.history_fragment_view);
                historyList.setAdapter(adapter);
                mFragment.setHistoryListAdapter(adapter);
            }
        } catch (Exception cacheException) {
        	cacheException.printStackTrace();
        }
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        HistoryApi irisApi = new HistoryApi(mContext);
        apiReturn = irisApi.getHistory(historyItems, Integer.toString(mLimit), mOffsetId);
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setHistoryViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setHistoryViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			historyItems = new ArrayList<HistoryItem>();

			historyItems.addAll(logger.getIrisPlusHistoryItems());

			if(apiReturn != null) {
				historyItems.addAll(apiReturn);
			}

			Collections.sort(historyItems, new Comparator<HistoryItem>() {
				@Override
				public int compare(final HistoryItem object1, final HistoryItem object2) {
					return object2.getDate().compareTo(object1.getDate());
				}
			} );

            if(historyItems == null || historyItems.size() == 0) {
                Toast.makeText(mContext, "You do not have any history entries on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            }

            adapter = mFragment.getHistoryListAdapter();

            if(adapter == null) {
                adapter = new HistoryListAdapter(mFragment.getActivity(), historyItems, mFragment);
                ListView historyList = (ListView) mFragment.getActivity().findViewById(R.id.history_fragment_view);
                historyList.setAdapter(adapter);
                historyList.setSelection(mFragment.getPosition());
            } else {
                adapter.updateAdapterList(historyItems);
            }
            mFragment.setHistoryListAdapter(adapter);

	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-history-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(historyItems);
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