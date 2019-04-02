package com.eightydegreeswest.irisplus.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.DashboardListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.DashboardViewTask;

@SuppressLint("NewApi")
public class DashboardFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private DashboardViewTask dashboardViewTask = null;
    private static DashboardFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private DashboardListAdapter dashboardListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DashboardFragment newInstance(int sectionNumber) {
    	fragment = new DashboardFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public DashboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.dashboard_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public DashboardViewTask getDashboardViewTask() {
		return dashboardViewTask;
	}

	public void setDashboardViewTask(DashboardViewTask dashboardViewTask) {
		this.dashboardViewTask = dashboardViewTask;
	}
	
	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setDashboardViewTask(new DashboardViewTask(fragment));
        TaskHelper.execute(getDashboardViewTask());
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

    public DashboardListAdapter getDashboardListAdapter() {
        return dashboardListAdapter;
    }

    public void setDashboardListAdapter(DashboardListAdapter dashboardListAdapter) {
        this.dashboardListAdapter = dashboardListAdapter;
    }
}
