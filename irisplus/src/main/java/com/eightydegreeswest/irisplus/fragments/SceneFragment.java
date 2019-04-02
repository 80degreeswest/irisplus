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
import com.eightydegreeswest.irisplus.adapters.SceneListAdapter;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.tasks.SceneDoTask;
import com.eightydegreeswest.irisplus.tasks.SceneViewTask;

@SuppressLint("NewApi")
public class SceneFragment extends Fragment implements OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private SceneViewTask sceneViewTask = null;
    private SceneDoTask sceneDoTask = null;
    private static SceneFragment fragment;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SceneListAdapter sceneListAdapter = null;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SceneFragment newInstance(int sectionNumber) {
    	fragment = new SceneFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SceneFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_scene, container, false);
        
        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.scene_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public SceneViewTask getSceneViewTask() {
		return sceneViewTask;
	}

	public void setSceneViewTask(SceneViewTask sceneViewTask) {
		this.sceneViewTask = sceneViewTask;
	}
	
	public SceneDoTask getSceneDoTask() {
		return sceneDoTask;
	}

	public void setSceneDoTask(SceneDoTask sceneDoTask) {
		this.sceneDoTask = sceneDoTask;
	}

	public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setSceneViewTask(new SceneViewTask(fragment));
        TaskHelper.execute(getSceneViewTask());
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

    public SceneListAdapter getSceneListAdapter() {
        return sceneListAdapter;
    }

    public void setSceneListAdapter(SceneListAdapter sceneListAdapter) {
        this.sceneListAdapter = sceneListAdapter;
    }
}
