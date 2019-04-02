package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.interfaces.SwipeAdapterInterface;
import com.daimajia.swipe.interfaces.SwipeItemMangerInterface;
import com.daimajia.swipe.util.Attributes;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.fragments.ControlFragment;
import com.eightydegreeswest.irisplus.model.ControlItem;
import com.eightydegreeswest.irisplus.tasks.ControlDetailDoTask;
import com.eightydegreeswest.irisplus.tasks.ControlDoTask;

import java.util.ArrayList;
import java.util.List;

public class ControlListAdapter extends ArrayAdapter<ControlItem>  implements SwipeItemMangerInterface, SwipeAdapterInterface {
	private final Context context;
	private List<ControlItem> controls = new ArrayList<ControlItem>();
	private ControlFragment fragment;

    private SharedPreferences mSharedPrefs = null;
    private NotificationHelper notificationHelper;
	//private IrisPlusLogger logger = new IrisPlusLogger();

	public ControlListAdapter(Context context, List<ControlItem> controls, ControlFragment fragment) {
		super(context, R.layout.list_control, controls);
		this.context = context;
		this.controls = controls;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_control, parent, false);

        SwipeLayout swipeLayout =  (SwipeLayout) rowView.findViewById(R.id.control_swipe_container);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        swipeLayout.setDragDistance(20);

        RelativeLayout mainControls = (RelativeLayout) rowView.findViewById(R.id.control_main_container) ;
		TextView textView = (TextView) rowView.findViewById(R.id.control_name);
		final ToggleButton status = (ToggleButton) rowView.findViewById(R.id.control_status_button);
		textView.setText(controls.get(position).getControlName());
		
		String controlStatus = controls.get(position).getStatus();
		String state = controls.get(position).getState();
		int devicesOff = controls.get(position).getDevicesOff();
		
		try {		
			if("on".equalsIgnoreCase(controlStatus) ||
                    "opened".equalsIgnoreCase(state) || "open".equalsIgnoreCase(state) ||
                    "favorite".equalsIgnoreCase(state)) {
				status.setChecked(true);
                status.setBackgroundResource(R.drawable.ic_power_on);
			} else if(controls.get(position).isShade()) {
                status.setChecked(false);
                status.setBackgroundResource(R.drawable.ic_blinds); //TODO: set based on current state
            } else {
				status.setChecked(false);
                status.setBackgroundResource(R.drawable.ic_power_off);
			}
		} catch (Exception e) {
			status.setChecked(false);
		}

        final int currentRow = position;

        status.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                notificationHelper.buttonFeedback();
		        if (currentRow != ListView.INVALID_POSITION) {
		        	String id = controls.get(currentRow).getId();
		        	if(id != null && !controls.get(currentRow).isShade()) {
                        fragment.setControlDoTask(new ControlDoTask(fragment, id, controls.get(currentRow).getType(), status));
                        TaskHelper.execute(fragment.getControlDoTask());
		        	}
		        }
			}			
		});

        activateDimmerSlide(currentRow, rowView, status);
        activateShadeSlide(currentRow, rowView, status);
        activateFanSlide(currentRow, rowView, status);

		return rowView;
	}

    private void activateDimmerSlide(final int currentRow, View rowView, ToggleButton status) {
        if (currentRow != ListView.INVALID_POSITION && controls.get(currentRow).isDimmer()) {
            LinearLayout dimmerButtons = (LinearLayout) rowView.findViewById(R.id.control_dimmer_buttons);
            final Button dimmer10 = (Button) rowView.findViewById(R.id.control_dimmer_10);
            final Button dimmer20 = (Button) rowView.findViewById(R.id.control_dimmer_20);
            final Button dimmer30 = (Button) rowView.findViewById(R.id.control_dimmer_30);
            final Button dimmer40 = (Button) rowView.findViewById(R.id.control_dimmer_40);
            final Button dimmer50 = (Button) rowView.findViewById(R.id.control_dimmer_50);
            final Button dimmer60 = (Button) rowView.findViewById(R.id.control_dimmer_60);
            final Button dimmer70 = (Button) rowView.findViewById(R.id.control_dimmer_70);
            final Button dimmer80 = (Button) rowView.findViewById(R.id.control_dimmer_80);
            final Button dimmer90 = (Button) rowView.findViewById(R.id.control_dimmer_90);
            final Button dimmer100 = (Button) rowView.findViewById(R.id.control_dimmer_100);
            dimmerButtons.setVisibility(View.VISIBLE);

            if(controls.get(currentRow).getIntensity() == 10) {
                dimmer10.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 20) {
                dimmer20.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 30) {
                dimmer30.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 40) {
                dimmer40.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 50) {
                dimmer50.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 60) {
                dimmer60.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 70) {
                dimmer70.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 80) {
                dimmer80.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 90) {
                dimmer90.setTextColor(Color.RED);
            } else if(controls.get(currentRow).getIntensity() == 100) {
                dimmer100.setTextColor(Color.RED);
            }

            dimmer10.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer10.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 10, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer20.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer20.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 20, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer30.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer30.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 30, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer40.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer40.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 40, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer50.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer50.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 50, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer60.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer60.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 60, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer70.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer70.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 70, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer80.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer80.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 80, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer90.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer90.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 90, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            dimmer100.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    dimmer100.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 100, null, null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });
        }
    }

    private void activateFanSlide(final int currentRow, View rowView, ToggleButton status) {
        if (currentRow != ListView.INVALID_POSITION && "Fan Control".equalsIgnoreCase(controls.get(currentRow).getType())) {
            LinearLayout fanButtons = (LinearLayout) rowView.findViewById(R.id.control_fan_buttons);
            final Button fanLow = (Button) rowView.findViewById(R.id.control_fan_low);
            final Button fanMed = (Button) rowView.findViewById(R.id.control_fan_med);
            final Button fanHigh = (Button) rowView.findViewById(R.id.control_fan_high);
            fanButtons.setVisibility(View.VISIBLE);

            if("1".equalsIgnoreCase(controls.get(currentRow).getSpeed())) {
                fanLow.setTextColor(Color.RED);
            } else if("2".equalsIgnoreCase(controls.get(currentRow).getSpeed())) {
                fanMed.setTextColor(Color.RED);
            } else if("3".equalsIgnoreCase(controls.get(currentRow).getSpeed())) {
                fanHigh.setTextColor(Color.RED);
            }

            fanLow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    fanLow.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 0, "1", null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            fanMed.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    fanMed.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 0, "2", null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            fanHigh.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    fanHigh.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 0, "3", null));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });
        }
    }

    private void activateShadeSlide(final int currentRow, View rowView, ToggleButton status) {
        if (currentRow != ListView.INVALID_POSITION && controls.get(currentRow).isShade()) {
            LinearLayout shadeButtons = (LinearLayout) rowView.findViewById(R.id.control_shade_buttons);
            final Button shadeDown = (Button) rowView.findViewById(R.id.control_shade_down);
            final Button shadeFav = (Button) rowView.findViewById(R.id.control_shade_fav);
            final Button shadeUp = (Button) rowView.findViewById(R.id.control_shade_up);
            shadeButtons.setVisibility(View.VISIBLE);

            if("open".equalsIgnoreCase(controls.get(currentRow).getStatus())) {
                shadeUp.setTextColor(Color.RED);
            } else if("closed".equalsIgnoreCase(controls.get(currentRow).getStatus())) {
                shadeDown.setTextColor(Color.RED);
            } else if("favorite".equalsIgnoreCase(controls.get(currentRow).getStatus())) {
                shadeFav.setTextColor(Color.RED);
            }

            shadeDown.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    shadeDown.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 0, null, "CLOSED"));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            shadeFav.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    shadeFav.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 0, null, "FAVORITE"));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });

            shadeUp.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    shadeUp.setTextColor(Color.RED);
                    notificationHelper.buttonFeedback();
                    fragment.setControlDetailDoTask(new ControlDetailDoTask(fragment, controls.get(currentRow).getId(), 0, null, "OPEN"));
                    TaskHelper.execute(fragment.getControlDetailDoTask());
                }
            });
        }
    }

	@Override
	public int getCount() {
		return controls != null ? controls.size() : 0;
	}

    public void updateAdapterList(List<ControlItem> newList) {
        this.controls.clear();
        this.controls.addAll(newList);
        this.notifyDataSetChanged();
    }


    @Override
    public int getSwipeLayoutResourceId(int position) {
        return 0;
    }

    @Override
    public void openItem(int position) {

    }

    @Override
    public void closeItem(int position) {

    }

    @Override
    public void closeAllExcept(SwipeLayout layout) {

    }

    @Override
    public void closeAllItems() {

    }

    @Override
    public List<Integer> getOpenItems() {
        return null;
    }

    @Override
    public List<SwipeLayout> getOpenLayouts() {
        return null;
    }

    @Override
    public void removeShownLayouts(SwipeLayout layout) {

    }

    @Override
    public boolean isOpen(int position) {
        return false;
    }

    @Override
    public Attributes.Mode getMode() {
        return null;
    }

    @Override
    public void setMode(Attributes.Mode mode) {

    }
}