package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.fragments.SceneFragment;
import com.eightydegreeswest.irisplus.model.SceneItem;
import com.eightydegreeswest.irisplus.tasks.SceneDoTask;

import java.util.ArrayList;
import java.util.List;

public class SceneListAdapter extends ArrayAdapter<SceneItem> {
	private final Context context;
	private List<SceneItem> scenes = new ArrayList<SceneItem>();
	private SceneFragment fragment;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public SceneListAdapter(Context context, List<SceneItem> scenes, SceneFragment fragment) {
		super(context, R.layout.list_scene);
		this.context = context;
		this.scenes = scenes;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_scene, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.scene_name);
		final ToggleButton run = (ToggleButton) rowView.findViewById(R.id.scene_run_button);
		textView.setText(scenes.get(position).getSceneName());

        final int currentRow = position;

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRow != ListView.INVALID_POSITION) {
                    notificationHelper.buttonFeedback();
					final int position = ((ListView) run.getParent().getParent()).getPositionForView(v);
                    String id = scenes.get(position).getId();
					boolean firing = scenes.get(position).isFiring();
					if(id != null && !firing) {
						fragment.setSceneDoTask(new SceneDoTask(fragment, id));
						TaskHelper.execute(fragment.getSceneDoTask());
                    }
                }
            }
        });
		
		run.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) run.getParent().getParent()).getPositionForView(v);
		        if (position != ListView.INVALID_POSITION) {
		        	String id = scenes.get(position).getId();
					boolean firing = scenes.get(position).isFiring();
					if(id != null && !firing) {
		        		fragment.setSceneDoTask(new SceneDoTask(fragment, id));
						TaskHelper.execute(fragment.getSceneDoTask());
		        	}
		        }
			}			
		});

        return rowView;
    }

	@Override
	public int getCount() {
		return scenes != null ? scenes.size() : 0;
	}

    public void updateAdapterList(List<SceneItem> newList) {
        this.scenes.clear();
        this.scenes.addAll(newList);
        this.notifyDataSetChanged();
    }
}