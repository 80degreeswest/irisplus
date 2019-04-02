package com.eightydegreeswest.irisplus.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.HubListAdapter;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.tasks.HubViewTask;

@SuppressLint("NewApi")
public class HubFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private HubViewTask hubViewTask = null;
    private static HubFragment fragment;
    private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	private Context mContext;
    private HubListAdapter hubListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static HubFragment newInstance(int sectionNumber) {
    	fragment = new HubFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HubFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_hub, container, false);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));

		// Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.hub_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public HubViewTask getHubViewTask() {
		return hubViewTask;
	}

	public void setHubViewTask(HubViewTask hubViewTask) {
		this.hubViewTask = hubViewTask;
	}

    public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setHubViewTask(new HubViewTask(fragment));
        TaskHelper.execute(getHubViewTask());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		this.refreshFragment();
	}

	@Override
    public void onRefresh() {
        this.refreshFragment();
    }

	public SwipeRefreshLayout getmSwipeRefreshLayout() {
		return mSwipeRefreshLayout;
	}

    public HubListAdapter getHubListAdapter() {
        return hubListAdapter;
    }

    public void setHubListAdapter(HubListAdapter hubListAdapter) {
        this.hubListAdapter = hubListAdapter;
    }
}
