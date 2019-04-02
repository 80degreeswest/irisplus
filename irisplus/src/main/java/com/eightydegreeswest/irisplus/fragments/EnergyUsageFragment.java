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
import com.eightydegreeswest.irisplus.adapters.EnergyUsageListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.EnergyUsageViewTask;

@SuppressLint("NewApi")
public class EnergyUsageFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private EnergyUsageViewTask energyUsageViewTask = null;
    private static EnergyUsageFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int currentPositionInList = 0;
    private EnergyUsageListAdapter energyUsageListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static EnergyUsageFragment newInstance(int sectionNumber) {
    	fragment = new EnergyUsageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public EnergyUsageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_energy, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.energy_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public EnergyUsageViewTask getEnergyUsageViewTask() {
		return energyUsageViewTask;
	}

	public void setEnergyUsageViewTask(EnergyUsageViewTask energyUsageViewTask) {
		this.energyUsageViewTask = energyUsageViewTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setEnergyUsageViewTask(new EnergyUsageViewTask(fragment));
        TaskHelper.execute(getEnergyUsageViewTask());
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

    public int getCurrentPositionInList() {
        return currentPositionInList;
    }

    public void setCurrentPositionInList(int currentPositionInList) {
        this.currentPositionInList = currentPositionInList;
    }

    public EnergyUsageListAdapter getEnergyUsageListAdapter() {
        return energyUsageListAdapter;
    }

    public void setEnergyUsageListAdapter(EnergyUsageListAdapter energyUsageListAdapter) {
        this.energyUsageListAdapter = energyUsageListAdapter;
    }
}
