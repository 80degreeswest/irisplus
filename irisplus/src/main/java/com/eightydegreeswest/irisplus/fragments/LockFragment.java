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
import com.eightydegreeswest.irisplus.adapters.LockListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.LockDoTask;
import com.eightydegreeswest.irisplus.tasks.LockViewTask;

@SuppressLint("NewApi")
public class LockFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private LockViewTask lockViewTask = null;
    private LockDoTask lockDoTask = null;
    private static LockFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LockListAdapter lockListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static LockFragment newInstance(int sectionNumber) {
    	fragment = new LockFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LockFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_lock, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.lock_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public LockViewTask getLockViewTask() {
		return lockViewTask;
	}

	public void setLockViewTask(LockViewTask lockViewTask) {
		this.lockViewTask = lockViewTask;
	}
	
	public LockDoTask getLockDoTask() {
		return lockDoTask;
	}

	public void setLockDoTask(LockDoTask lockDoTask) {
		this.lockDoTask = lockDoTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setLockViewTask(new LockViewTask(fragment));
        TaskHelper.execute(getLockViewTask());
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

    public LockListAdapter getLockListAdapter() {
        return lockListAdapter;
    }

    public void setLockListAdapter(LockListAdapter lockListAdapter) {
        this.lockListAdapter = lockListAdapter;
    }
}
