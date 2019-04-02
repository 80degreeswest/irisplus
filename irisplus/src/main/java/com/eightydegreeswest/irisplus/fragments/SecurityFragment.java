package com.eightydegreeswest.irisplus.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Button;

import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.SecurityDoTask;
import com.eightydegreeswest.irisplus.tasks.SecurityViewTask;

@SuppressLint("NewApi")
public class SecurityFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    
    private SecurityViewTask securityViewTask = null;
    private SecurityDoTask securityDoTask = null;
    //private View mStatusView;
    private View mSecurityView;
    //private TextView mStatusMessageView;
    private static SecurityFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NotificationHelper notificationHelper;
    private Context mContext;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SecurityFragment newInstance(int sectionNumber) {
    	fragment = new SecurityFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);        
        return fragment;
    }

    public SecurityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_security, container, false);
        //mStatusView = rootView.findViewById(R.id.progress_status);
        //mSecurityView = rootView.findViewById(R.id.security_fragment_view);
        this.notificationHelper = new NotificationHelper(mContext);

        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.security_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
		
		final Button btnAlarmHome = (Button) rootView.findViewById(R.id.btn_alarm_home);
		final Button btnAlarmAway = (Button) rootView.findViewById(R.id.btn_alarm_away);
	    final Button btnAlarmNight = (Button) rootView.findViewById(R.id.btn_alarm_night);
        final Button btnAlarmPanic = (Button) rootView.findViewById(R.id.btn_alarm_panic);

        btnAlarmHome.setVisibility(View.INVISIBLE);
        btnAlarmAway.setVisibility(View.INVISIBLE);
        btnAlarmNight.setVisibility(View.INVISIBLE);
        btnAlarmPanic.setVisibility(View.INVISIBLE);

        btnAlarmHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//showProgress(true);
                notificationHelper.buttonFeedback();
                setSecurityDoTask(new SecurityDoTask(fragment, "alarm", "OFF"));
				TaskHelper.execute(getSecurityDoTask());
			}			
		});
		
	    btnAlarmAway.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//showProgress(true);
                notificationHelper.buttonFeedback();
                setSecurityDoTask(new SecurityDoTask(fragment, "alarm", "ON"));
				TaskHelper.execute(getSecurityDoTask());
			}			
		});
		
	    btnAlarmNight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//showProgress(true);
                notificationHelper.buttonFeedback();
                setSecurityDoTask(new SecurityDoTask(fragment, "alarm", "PARTIAL"));
				TaskHelper.execute(getSecurityDoTask());
			}			
		});

        btnAlarmPanic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //showProgress(true);
                notificationHelper.buttonFeedback();
                setSecurityDoTask(new SecurityDoTask(fragment, "alarm", "PANIC"));
                TaskHelper.execute(getSecurityDoTask());
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

	public SecurityViewTask getSecurityViewTask() {
		return securityViewTask;
	}

	public void setSecurityViewTask(SecurityViewTask securityViewTask) {
		this.securityViewTask = securityViewTask;
	}

	public SecurityDoTask getSecurityDoTask() {
		return securityDoTask;
	}

	public void setSecurityDoTask(SecurityDoTask securityDoTask) {
		this.securityDoTask = securityDoTask;
	}

	public void refreshFragment() {
		//showProgress(true);
        mSwipeRefreshLayout.setRefreshing(true);
		setSecurityViewTask(new SecurityViewTask(fragment));
		TaskHelper.execute(getSecurityViewTask());
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
}
