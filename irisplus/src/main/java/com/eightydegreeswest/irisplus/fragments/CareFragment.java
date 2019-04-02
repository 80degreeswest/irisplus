package com.eightydegreeswest.irisplus.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.tasks.CareDoTask;
import com.eightydegreeswest.irisplus.tasks.CareViewTask;

@SuppressLint("NewApi")
public class CareFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private CareViewTask careViewTask = null;
    private CareDoTask careDoTask = null;
    private static CareFragment fragment;
    private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
    private NotificationHelper notificationHelper;
    //private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static CareFragment newInstance(int sectionNumber) {
    	fragment = new CareFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public CareFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_care, container, false);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(fragment.getActivity());
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        this.notificationHelper = new NotificationHelper(fragment.getActivity().getApplicationContext());

        final ToggleButton status = (ToggleButton) rootView.findViewById(R.id.care_status);

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationHelper.buttonFeedback();
                fragment.setCareDoTask(new CareDoTask(fragment, status.isChecked()));
                TaskHelper.execute(fragment.getCareDoTask());
            }
        });

		// Configure the swipe refresh layout
        //mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.care_swipe_container);
        //mSwipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public CareViewTask getCareViewTask() {
		return careViewTask;
	}

	public void setCareViewTask(CareViewTask careViewTask) {
		this.careViewTask = careViewTask;
	}

    public CareDoTask getCareDoTask() {
        return careDoTask;
    }

    public void setCareDoTask(CareDoTask careDoTask) {
        this.careDoTask = careDoTask;
    }

    public void refreshFragment() {
		//mSwipeRefreshLayout.setRefreshing(true);
		setCareViewTask(new CareViewTask(fragment));
        TaskHelper.execute(getCareViewTask());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		this.refreshFragment();
	}

    /*
	@Override
    public void onRefresh() {
        this.refreshFragment();
    }
    */

    /*
	public SwipeRefreshLayout getmSwipeRefreshLayout() {
		return mSwipeRefreshLayout;
	}
	*/
}
