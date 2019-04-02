package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.SceneListAdapter;
import com.eightydegreeswest.irisplus.apiv2.SceneApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.SceneFragment;
import com.eightydegreeswest.irisplus.model.SceneItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SceneViewTask extends AsyncTask<Void, Void, Boolean> {

	private SceneFragment mFragment = null;
	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private List<SceneItem> scenes = new ArrayList<SceneItem>();
    private SceneListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

	@SuppressWarnings("unchecked")
	public SceneViewTask(SceneFragment fragment) {
		mFragment = fragment;
		mContext = IrisPlus.getContext();
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
		
		try {
        	//Load cached list
	        FileInputStream fileInputStream = mContext.openFileInput("irisplus-scene-list.dat");
	        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
	        scenes = (ArrayList<SceneItem>) objectInputStream.readObject();
	        objectInputStream.close();

            adapter = mFragment.getSceneListAdapter();

            if(adapter == null) {
                adapter = new SceneListAdapter(mContext, scenes, mFragment);
                ListView sceneList = (ListView) mFragment.getActivity().findViewById(R.id.scene_fragment_view);
                sceneList.setAdapter(adapter);
                mFragment.setSceneListAdapter(adapter);
            }
        } catch (Exception cacheException) {
        	//Ignore
        	cacheException.printStackTrace();
        }
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
        SceneApi irisApi = new SceneApi(mContext);
        scenes = irisApi.getScenes();
		return true;
	}

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
			mFragment.setSceneViewTask(null);
			mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setSceneViewTask(null);
		mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
			if(scenes == null || scenes.size() == 0) {
                Toast.makeText(mContext, "You do not have any scenes on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            } else {
				Collections.sort(scenes, new Comparator<SceneItem>() {
					@Override
					public int compare(final SceneItem object1, final SceneItem object2) {
						return object1.getSceneName().compareTo(object2.getSceneName());
					}
				} );
			}

            adapter = mFragment.getSceneListAdapter();

            if(adapter == null) {
                adapter = new SceneListAdapter(mContext, scenes, mFragment);
                ListView sceneList = (ListView) mFragment.getActivity().findViewById(R.id.scene_fragment_view);
                sceneList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(scenes);
            }
            mFragment.setSceneListAdapter(adapter);
	        
	        try {
	        	//Cache list
		        FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-scene-list.dat", Context.MODE_PRIVATE);
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		        objectOutputStream.writeObject(scenes);
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