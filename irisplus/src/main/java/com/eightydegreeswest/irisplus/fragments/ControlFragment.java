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
import com.eightydegreeswest.irisplus.adapters.ControlListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.ControlDetailDoTask;
import com.eightydegreeswest.irisplus.tasks.ControlDoTask;
import com.eightydegreeswest.irisplus.tasks.ControlViewTask;

@SuppressLint("NewApi")
public class ControlFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    
    private ControlViewTask controlViewTask = null;
    private ControlDoTask controlDoTask = null;
    private ControlDetailDoTask controlDetailDoTask = null;
    private static ControlFragment fragment;   
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int currentPositionInList = 0;
    private ControlListAdapter controlListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ControlFragment newInstance(int sectionNumber) {
    	fragment = new ControlFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ControlFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_control, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.control_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public ControlViewTask getControlViewTask() {
		return controlViewTask;
	}

	public void setControlViewTask(ControlViewTask controlViewTask) {
		this.controlViewTask = controlViewTask;
	}
	
	public ControlDoTask getControlDoTask() {
		return controlDoTask;
	}

	public void setControlDoTask(ControlDoTask controlDoTask) {
		this.controlDoTask = controlDoTask;
	}

	public ControlDetailDoTask getControlDetailDoTask() {
		return controlDetailDoTask;
	}

	public void setControlDetailDoTask(ControlDetailDoTask controlDetailDoTask) {
		this.controlDetailDoTask = controlDetailDoTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setControlViewTask(new ControlViewTask(fragment));
        TaskHelper.execute(getControlViewTask());
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

    public ControlListAdapter getControlListAdapter() {
        return controlListAdapter;
    }

    public void setControlListAdapter(ControlListAdapter controlListAdapter) {
        this.controlListAdapter = controlListAdapter;
    }
}
