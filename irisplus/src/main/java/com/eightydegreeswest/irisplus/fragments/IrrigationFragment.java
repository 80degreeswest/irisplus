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
import com.eightydegreeswest.irisplus.adapters.IrrigationListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.IrrigationDoTask;
import com.eightydegreeswest.irisplus.tasks.IrrigationViewTask;

@SuppressLint("NewApi")
public class IrrigationFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private IrrigationViewTask irrigationViewTask = null;
    private IrrigationDoTask irrigationDoTask = null;
    private static IrrigationFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private IrrigationListAdapter irrigationListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static IrrigationFragment newInstance(int sectionNumber) {
    	fragment = new IrrigationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public IrrigationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_irrigation, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.irrigation_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public IrrigationViewTask getIrrigationViewTask() {
		return irrigationViewTask;
	}

	public void setIrrigationViewTask(IrrigationViewTask irrigationViewTask) {
		this.irrigationViewTask = irrigationViewTask;
	}
	
	public IrrigationDoTask getIrrigationDoTask() {
		return irrigationDoTask;
	}

	public void setIrrigationDoTask(IrrigationDoTask irrigationDoTask) {
		this.irrigationDoTask = irrigationDoTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setIrrigationViewTask(new IrrigationViewTask(fragment));
        TaskHelper.execute(getIrrigationViewTask());
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

    public IrrigationListAdapter getIrrigationListAdapter() {
        return irrigationListAdapter;
    }

    public void setIrrigationListAdapter(IrrigationListAdapter irrigationListAdapter) {
        this.irrigationListAdapter = irrigationListAdapter;
    }
}
