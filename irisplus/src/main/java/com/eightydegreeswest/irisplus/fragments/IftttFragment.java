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
import com.eightydegreeswest.irisplus.adapters.IftttListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.IftttViewTask;

@SuppressLint("NewApi")
public class IftttFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private IftttViewTask iftttViewTask = null;
    private static IftttFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int currentPositionInList = 0;
    private IftttListAdapter iftttListAdapter = null;
    private static boolean mProblemOnly = false;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static IftttFragment newInstance(int sectionNumber) {
    	fragment = new IftttFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        mProblemOnly = false;
        return fragment;
    }

    public static IftttFragment newInstance(int sectionNumber, boolean problemOnly) {
        fragment = new IftttFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        mProblemOnly = problemOnly;
        return fragment;
    }

    public IftttFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_ifttt, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.ifttt_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public IftttViewTask getIftttViewTask() {
		return iftttViewTask;
	}

	public void setIftttViewTask(IftttViewTask iftttViewTask) {
		this.iftttViewTask = iftttViewTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setIftttViewTask(new IftttViewTask(fragment, mProblemOnly));
        TaskHelper.execute(getIftttViewTask());
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

    public IftttListAdapter getIftttListAdapter() {
        return iftttListAdapter;
    }

    public void setIftttListAdapter(IftttListAdapter iftttListAdapter) {
        this.iftttListAdapter = iftttListAdapter;
    }
}
