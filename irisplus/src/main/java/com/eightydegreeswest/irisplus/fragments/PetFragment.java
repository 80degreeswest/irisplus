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
import com.eightydegreeswest.irisplus.adapters.PetListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.PetDoTask;
import com.eightydegreeswest.irisplus.tasks.PetViewTask;

@SuppressLint("NewApi")
public class PetFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private PetViewTask petViewTask = null;
    private PetDoTask petDoTask = null;
    private static PetFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PetListAdapter petListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PetFragment newInstance(int sectionNumber) {
    	fragment = new PetFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PetFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_pet, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.pet_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public PetViewTask getPetViewTask() {
		return petViewTask;
	}

	public void setPetViewTask(PetViewTask petViewTask) {
		this.petViewTask = petViewTask;
	}
	
	public PetDoTask getPetDoTask() {
		return petDoTask;
	}

	public void setPetDoTask(PetDoTask petDoTask) {
		this.petDoTask = petDoTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setPetViewTask(new PetViewTask(fragment));
        TaskHelper.execute(getPetViewTask());
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

    public PetListAdapter getPetListAdapter() {
        return petListAdapter;
    }

    public void setPetListAdapter(PetListAdapter petListAdapter) {
        this.petListAdapter = petListAdapter;
    }
}
