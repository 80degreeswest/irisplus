package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.PetListAdapter;
import com.eightydegreeswest.irisplus.apiv2.PetApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.PetFragment;
import com.eightydegreeswest.irisplus.model.PetItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PetViewTask extends AsyncTask<Void, Void, Boolean> {

	private PetFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<PetItem> pets = new ArrayList<PetItem>();
	private List<PetItem> apiReturn = new ArrayList<PetItem>();
    private PetListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	@SuppressWarnings("unchecked")
	public PetViewTask(PetFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
		
		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-pet-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        pets = (ArrayList<PetItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getPetListAdapter();

            if(adapter == null) {
                adapter = new PetListAdapter(mContext, pets, mFragment);
                ListView petList = (ListView) mFragment.getActivity().findViewById(R.id.pet_fragment_view);
                petList.setAdapter(adapter);
                mFragment.setPetListAdapter(adapter);
            }
        } catch (Exception cacheException) {
        	//Ignore
        	cacheException.printStackTrace();
        }
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        PetApi irisApi = new PetApi(mContext);
        apiReturn = irisApi.getAllPets();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setPetViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setPetViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			pets = new ArrayList<PetItem>();

			if(apiReturn != null) {
				pets.addAll(apiReturn);
			}

			if(pets == null || pets.size() == 0) {
                Toast.makeText(mContext, "You do not have any pets on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            }

            adapter = mFragment.getPetListAdapter();

            if(adapter == null) {
                adapter = new PetListAdapter(mContext, pets, mFragment);
                ListView petList = (ListView) mFragment.getActivity().findViewById(R.id.pet_fragment_view);
                petList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(pets);
            }
            mFragment.setPetListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-pet-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(pets);
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