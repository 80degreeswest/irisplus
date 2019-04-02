package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.RuleListAdapter;
import com.eightydegreeswest.irisplus.apiv2.RuleApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.RuleFragment;
import com.eightydegreeswest.irisplus.model.RuleItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RuleViewTask extends AsyncTask<Void, Void, Boolean> {

	private RuleFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<RuleItem> rules = new ArrayList<RuleItem>();
    private RuleListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	@SuppressWarnings("unchecked")
	public RuleViewTask(RuleFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
		
		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-rule-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        rules = (ArrayList<RuleItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getRuleListAdapter();

            if(adapter == null) {
                adapter = new RuleListAdapter(mContext, rules, mFragment);
                ListView ruleList = (ListView) mFragment.getActivity().findViewById(R.id.rule_fragment_view);
                ruleList.setAdapter(adapter);
                mFragment.setRuleListAdapter(adapter);
            }
        } catch (Exception cacheException) {
        	//Ignore
        	cacheException.printStackTrace();
        }
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        RuleApi irisApi = new RuleApi(mContext);
        rules = irisApi.getRules();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setRuleViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setRuleViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			if(rules == null || rules.size() == 0) {
                Toast.makeText(mContext, "You do not have any rules on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            } else {
				Collections.sort(rules, new Comparator<RuleItem>() {
					@Override
					public int compare(final RuleItem object1, final RuleItem object2) {
						return object1.getRuleName().compareTo(object2.getRuleName());
					}
				} );

				Collections.sort(rules, new Comparator<RuleItem>() {
					@Override
					public int compare(final RuleItem object1, final RuleItem object2) {
						return object2.getEnabled().compareTo(object1.getEnabled());
					}
				} );
			}

            adapter = mFragment.getRuleListAdapter();

            if(adapter == null) {
                adapter = new RuleListAdapter(mContext, rules, mFragment);
                ListView ruleList = (ListView) mFragment.getActivity().findViewById(R.id.rule_fragment_view);
                ruleList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(rules);
            }
            mFragment.setRuleListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-rule-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(rules);
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