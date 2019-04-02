package com.eightydegreeswest.irisplus.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.ThermostatItem;
import com.eightydegreeswest.irisplus.tasks.ThermostatDoTask;
import com.eightydegreeswest.irisplus.tasks.ThermostatViewTask;
import com.triggertrap.seekarc.SeekArc;

import java.util.List;

@SuppressLint("NewApi")
public class ThermostatFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private ThermostatViewTask thermostatViewTask = null;
    private ThermostatDoTask thermostatDoTask = null;
    //private TextView mStatusMessageView;
    private static ThermostatFragment fragment;
    private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
	//private SwipeRefreshLayout mSwipeRefreshLayout;
    private int currentModeSelection = 9999;
    private int currentThermostatSelection = 9999;
    private String targetHeat = "";
    private String targetCool = "";
    private String thermostatID = null;
    private List<ThermostatItem> thermostats = null;
    private Context mContext;
    private NotificationHelper notificationHelper;
    public boolean userAction = false;
    public int currentFilterValue = 0;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ThermostatFragment newInstance(int sectionNumber) {
    	fragment = new ThermostatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ThermostatFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_thermostat, container, false);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        final int minTemp = Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_MIN_TEMP, "35"));
        final int maxTemp = Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_MAX_TEMP, "95"));
        this.notificationHelper = new NotificationHelper(mContext);

        // Configure the swipe refresh layout
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.security_swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        final Spinner thermostatSelectSpinner = (Spinner) rootView.findViewById(R.id.thermostat_select);
        final Spinner modeSpinner = (Spinner) rootView.findViewById(R.id.thermostat_mode);
        final TextView heatTemperature = (TextView) rootView.findViewById(R.id.heatTemperature);
        final TextView coolTemperature = (TextView) rootView.findViewById(R.id.coolTemperature);
        final Button btnHeatMinus = (Button) rootView.findViewById(R.id.btn_heat_minus);
        final Button btnHeatPlus = (Button) rootView.findViewById(R.id.btn_heat_plus);
        final Button btnCoolMinus = (Button) rootView.findViewById(R.id.btn_cool_minus);
        final Button btnCoolPlus = (Button) rootView.findViewById(R.id.btn_cool_plus);

        targetHeat = heatTemperature.getText().toString();
        targetCool = coolTemperature.getText().toString();

        //final Button filterStatusBtn = (Button) rootView.findViewById(R.id.filter_status);

        thermostatSelectSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                userAction = true;
                return false;
            }
        });

        thermostatSelectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentThermostatSelection < 9999 && currentThermostatSelection != i && userAction) {
                    for(ThermostatItem item : thermostats) {
                        if(item.getName().equalsIgnoreCase(thermostatSelectSpinner.getItemAtPosition(i).toString())) {
                            thermostatID = item.getId();
                            break;
                        }
                    }
                    refreshFragment();
                }
                currentThermostatSelection = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        modeSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                userAction = true;
                return false;
            }
        });

        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(currentModeSelection < 9999 && currentModeSelection != i && userAction) {
                    fragment.setThermostatDoTask(new ThermostatDoTask(fragment, "mode", modeSpinner.getItemAtPosition(i).toString().toLowerCase(), null, null, thermostatID));
                    TaskHelper.execute(fragment.getThermostatDoTask());
                }
                currentModeSelection = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnHeatMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationHelper.buttonFeedback();
                Integer temperature = Integer.parseInt(heatTemperature.getText().toString()) - 1;
                heatTemperature.setText(Integer.toString(temperature));
                fragment.setThermostatDoTask(new ThermostatDoTask(fragment, "temperatureHeat", Integer.toString(temperature), targetHeat, targetCool, thermostatID));
                TaskHelper.execute(fragment.getThermostatDoTask());
            }
        });

        btnHeatPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationHelper.buttonFeedback();
                Integer temperature = Integer.parseInt(heatTemperature.getText().toString()) + 1;
                heatTemperature.setText(Integer.toString(temperature));
                fragment.setThermostatDoTask(new ThermostatDoTask(fragment, "temperatureHeat", Integer.toString(temperature), targetHeat, targetCool, thermostatID));
                TaskHelper.execute(fragment.getThermostatDoTask());
            }
        });

        btnCoolMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationHelper.buttonFeedback();
                Integer temperature = Integer.parseInt(coolTemperature.getText().toString()) - 1;
                coolTemperature.setText(Integer.toString(temperature));
                fragment.setThermostatDoTask(new ThermostatDoTask(fragment, "temperatureCool", Integer.toString(temperature), targetHeat, targetCool, thermostatID));
                TaskHelper.execute(fragment.getThermostatDoTask());
            }
        });

        btnCoolPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationHelper.buttonFeedback();
                Integer temperature = Integer.parseInt(coolTemperature.getText().toString()) + 1;
                coolTemperature.setText(Integer.toString(temperature));
                fragment.setThermostatDoTask(new ThermostatDoTask(fragment, "temperatureCool", Integer.toString(temperature), targetHeat, targetCool, thermostatID));
                TaskHelper.execute(fragment.getThermostatDoTask());
            }
        });

        /*
        filterStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationHelper.buttonFeedback();
                new AlertDialog.Builder(fragment.getActivity())
                    .setTitle("Replace Filter?")
                    .setMessage("Do you really want to reset furnace filter?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_LOCAL_FILTER_RUNTIME, true)) {
                                mSharedPrefs.edit().putInt("lastFilterReset", currentFilterValue).commit();
                            } else {
                                fragment.setThermostatDoTask(new ThermostatDoTask(fragment, "filter", null, null, null, thermostatID));
                                TaskHelper.execute(fragment.getThermostatDoTask());
                            }
                            filterStatusBtn.setBackgroundResource(R.drawable.ic_filter_ok);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();
            }
        });
        */

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        ((IrisActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }
	
	public ThermostatViewTask getThermostatViewTask() {
		return thermostatViewTask;
	}

	public void setThermostatViewTask(ThermostatViewTask thermostatViewTask) {
		this.thermostatViewTask = thermostatViewTask;
	}

    public ThermostatDoTask getThermostatDoTask() {
        return thermostatDoTask;
    }

    public void setThermostatDoTask(ThermostatDoTask thermostatDoTask) {
        this.thermostatDoTask = thermostatDoTask;
    }

    public String getThermostatID() {
        return thermostatID;
    }

    public void setThermostatID(String thermostatID) {
        this.thermostatID = thermostatID;
    }

    public List<ThermostatItem> getThermostats() {
        return thermostats;
    }

    public void setThermostats(List<ThermostatItem> thermostats) {
        this.thermostats = thermostats;
    }

    public void refreshFragment() {
		mSwipeRefreshLayout.setRefreshing(true);
		setThermostatViewTask(new ThermostatViewTask(fragment, thermostatID));
        TaskHelper.execute(getThermostatViewTask());
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

    public String getTargetHeat() {
        return targetHeat;
    }

    public void setTargetHeat(String targetHeat) {
        this.targetHeat = targetHeat;
    }

    public String getTargetCool() {
        return targetCool;
    }

    public void setTargetCool(String targetCool) {
        this.targetCool = targetCool;
    }

    public SwipeRefreshLayout getmSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }
}
