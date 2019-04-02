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
import com.eightydegreeswest.irisplus.adapters.RuleListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.RuleDoTask;
import com.eightydegreeswest.irisplus.tasks.RuleViewTask;

@SuppressLint("NewApi")
public class RuleFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private RuleViewTask ruleViewTask = null;
    private RuleDoTask ruleDoTask = null;
    private static RuleFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RuleListAdapter ruleListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static RuleFragment newInstance(int sectionNumber) {
    	fragment = new RuleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RuleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_rule, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.rule_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public RuleViewTask getRuleViewTask() {
		return ruleViewTask;
	}

	public void setRuleViewTask(RuleViewTask ruleViewTask) {
		this.ruleViewTask = ruleViewTask;
	}
	
	public RuleDoTask getRuleDoTask() {
		return ruleDoTask;
	}

	public void setRuleDoTask(RuleDoTask ruleDoTask) {
		this.ruleDoTask = ruleDoTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setRuleViewTask(new RuleViewTask(fragment));
        TaskHelper.execute(getRuleViewTask());
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

    public RuleListAdapter getRuleListAdapter() {
        return ruleListAdapter;
    }

    public void setRuleListAdapter(RuleListAdapter ruleListAdapter) {
        this.ruleListAdapter = ruleListAdapter;
    }
}
