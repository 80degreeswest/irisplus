package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daimajia.swipe.SwipeLayout;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.fragments.IrrigationFragment;
import com.eightydegreeswest.irisplus.model.IrrigationItem;
import com.eightydegreeswest.irisplus.tasks.IrrigationDoTask;

import java.util.ArrayList;
import java.util.List;

public class IrrigationListAdapter extends ArrayAdapter<IrrigationItem> {
	private final Context context;
	private List<IrrigationItem> irrigations = new ArrayList<IrrigationItem>();
	private IrrigationFragment fragment;
    private int currentStateSelection = 9999;
    private int currentControlSelection = 9999;
    private int currentDelaySelection = 9999;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public IrrigationListAdapter(Context context, List<IrrigationItem> irrigations, IrrigationFragment fragment) {
		super(context, R.layout.list_irrigation);
		this.context = context;
		this.irrigations = irrigations;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(final int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_irrigation, parent, false);

        SwipeLayout swipeLayout =  (SwipeLayout) rowView.findViewById(R.id.irrigation_swipe_container);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.setDragDistance(20);

		TextView name = (TextView) rowView.findViewById(R.id.irrigation_name);
		name.setText(irrigations.get(position).getDeviceName());
        final ToggleButton status = (ToggleButton) rowView.findViewById(R.id.irrigation_status_button);
        String state = irrigations.get(position).getState();

        try {
            if("WATERING".equalsIgnoreCase(state)) {
                status.setChecked(true);
            } else {
                status.setChecked(false);
            }
        } catch (Exception e) {
            status.setChecked(false);
        }

        final int currentRow = position;

        final Button stateManual = (Button) rowView.findViewById(R.id.irrigation_state_manual);
        final Button stateSchedule = (Button) rowView.findViewById(R.id.irrigation_state_schedule);
        final Button delay0 = (Button) rowView.findViewById(R.id.irrigation_delay_0);
        final Button delay12 = (Button) rowView.findViewById(R.id.irrigation_delay_12);
        final Button delay24 = (Button) rowView.findViewById(R.id.irrigation_delay_24);
        final Button delay48 = (Button) rowView.findViewById(R.id.irrigation_delay_48);
        final Button delay72 = (Button) rowView.findViewById(R.id.irrigation_delay_72);

        if("MANUAL".equalsIgnoreCase(irrigations.get(position).getState())) {
            stateManual.setTextColor(Color.RED);
        } else if("SCHEDULE".equalsIgnoreCase(irrigations.get(position).getState())) {
            stateSchedule.setTextColor(Color.RED);
        }

        stateManual.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = irrigations.get(position).getId();
                    if(id != null) {
                        fragment.setIrrigationDoTask(new IrrigationDoTask(fragment, irrigations.get(position).getId(), "MANUAL", null, null));
                        TaskHelper.execute(fragment.getIrrigationDoTask());
                    }
                }
            }
        });

        stateSchedule.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = irrigations.get(position).getId();
                    if(id != null) {
                        fragment.setIrrigationDoTask(new IrrigationDoTask(fragment, irrigations.get(position).getId(), "SCHEDULE", null, null));
                        TaskHelper.execute(fragment.getIrrigationDoTask());
                    }
                }
            }
        });

        delay0.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = irrigations.get(position).getId();
                    if(id != null) {
                        fragment.setIrrigationDoTask(new IrrigationDoTask(fragment, irrigations.get(position).getId(), "0", null, null));
                        TaskHelper.execute(fragment.getIrrigationDoTask());
                    }
                }
            }
        });

        delay12.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = irrigations.get(position).getId();
                    if(id != null) {
                        fragment.setIrrigationDoTask(new IrrigationDoTask(fragment, irrigations.get(position).getId(), "12", null, null));
                        TaskHelper.execute(fragment.getIrrigationDoTask());
                    }
                }
            }
        });

        delay24.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = irrigations.get(position).getId();
                    if(id != null) {
                        fragment.setIrrigationDoTask(new IrrigationDoTask(fragment, irrigations.get(position).getId(), "24", null, null));
                        TaskHelper.execute(fragment.getIrrigationDoTask());
                    }
                }
            }
        });

        delay48.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = irrigations.get(position).getId();
                    if(id != null) {
                        fragment.setIrrigationDoTask(new IrrigationDoTask(fragment, irrigations.get(position).getId(), "48", null, null));
                        TaskHelper.execute(fragment.getIrrigationDoTask());
                    }
                }
            }
        });

        delay72.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = irrigations.get(position).getId();
                    if(id != null) {
                        fragment.setIrrigationDoTask(new IrrigationDoTask(fragment, irrigations.get(position).getId(), "72", null, null));
                        TaskHelper.execute(fragment.getIrrigationDoTask());
                    }
                }
            }
        });

        status.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationHelper.buttonFeedback();
                final int position = ((ListView) status.getParent().getParent().getParent()).getPositionForView(v);
                if (position != ListView.INVALID_POSITION) {
                    String id = irrigations.get(position).getId();
                    if("WATERING".equalsIgnoreCase(irrigations.get(position).getState())) {
                        fragment.setIrrigationDoTask(new IrrigationDoTask(fragment, irrigations.get(position).getId(), "STOP", null, null));
                        TaskHelper.execute(fragment.getIrrigationDoTask());
                    } else {
                        //Do zone and water time selection
                    }
                }
            }
        });

        return rowView;
    }

	@Override
	public int getCount() {
		return irrigations != null ? irrigations.size() : 0;
	}

    public void updateAdapterList(List<IrrigationItem> newList) {
        this.irrigations.clear();
        this.irrigations.addAll(newList);
        this.notifyDataSetChanged();
    }
}