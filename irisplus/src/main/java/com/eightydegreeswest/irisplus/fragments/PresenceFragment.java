package com.eightydegreeswest.irisplus.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.eightydegreeswest.irisplus.adapters.PresenceListAdapter;
import com.eightydegreeswest.irisplus.common.AlarmManagerHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.tasks.PresenceViewTask;

@SuppressLint("NewApi")
public class PresenceFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PresenceViewTask presenceViewTask = null;
    private static PresenceFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Context mContext;
    private PresenceListAdapter presenceListAdapter;

    SharedPreferences mSharedPrefs;
    boolean usePresenceDetection;
    boolean useIfttt;
    boolean useIftttForRules;
    private Activity mActivity;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PresenceFragment newInstance(int sectionNumber) {
    	fragment = new PresenceFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PresenceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_presence, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.presence_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mContext = activity.getApplicationContext();
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public PresenceViewTask getPresenceViewTask() {
		return presenceViewTask;
	}

	public void setPresenceViewTask(PresenceViewTask presenceViewTask) {
		this.presenceViewTask = presenceViewTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);

        useIftttForRules = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_IFTTT, false);

        setPresenceViewTask(new PresenceViewTask(fragment));
        TaskHelper.execute(getPresenceViewTask());
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

    public PresenceListAdapter getPresenceListAdapter() {
        return presenceListAdapter;
    }

    public void setPresenceListAdapter(PresenceListAdapter presenceListAdapter) {
        this.presenceListAdapter = presenceListAdapter;
    }
}
