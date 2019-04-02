package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.StatusSpinnerAdapter;
import com.eightydegreeswest.irisplus.apiv2.ThermostatApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.ThermostatFragment;
import com.eightydegreeswest.irisplus.model.ThermostatDetailsItem;
import com.eightydegreeswest.irisplus.model.ThermostatItem;
import com.triggertrap.seekarc.SeekArc;

import java.util.ArrayList;
import java.util.List;

public class ThermostatViewTask extends AsyncTask<Void, Void, Boolean> {

	private ThermostatFragment mFragment = null;
	private Context mContext = null;
    private ThermostatDetailsItem thermostatDetailsItem = null;
	private IrisPlusLogger logger = new IrisPlusLogger();

	private SharedPreferences mSharedPrefs = null;
    private String mThermostatID = null;
    private int notifyID = 0;
    private int mFilterHours;
    NotificationHelper notificationHelper;

	public ThermostatViewTask(ThermostatFragment fragment, String thermostatID) {
		mFragment = fragment;
        mContext = IrisPlus.getContext();
        mThermostatID = thermostatID;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        mFilterHours = Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_FILTER_HOURS, "300"));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();
        logger.log(IrisPlusConstants.LOG_INFO, "Thermostat selected: " + mThermostatID);
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {

        ThermostatApi irisApi = new ThermostatApi(mContext);
        thermostatDetailsItem = irisApi.getThermostatDetails(mThermostatID);

        return thermostatDetailsItem != null;
    }

	@Override
	protected void onPostExecute(final Boolean success) {		
		if(mFragment.isAdded()) {
            this.performCommand();
            mFragment.userAction = false;
			mFragment.setThermostatViewTask(null);
            mFragment.getmSwipeRefreshLayout().setRefreshing(false);
            notificationHelper.destroyNotification(notifyID);
		}
	}

	@Override
	protected void onCancelled() {
		mFragment.setThermostatViewTask(null);
        mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
	}
	
	protected void performCommand() {
		try {
            TextView currentTemperature = (TextView) mFragment.getActivity().findViewById(R.id.currentTemperature);
            Spinner modeSpinner = (Spinner) mFragment.getActivity().findViewById(R.id.thermostat_mode);
            TextView heatTemperature = (TextView) mFragment.getActivity().findViewById(R.id.heatTemperature);
            TextView coolTemperature = (TextView) mFragment.getActivity().findViewById(R.id.coolTemperature);
            //TextView humidityTxt = (TextView) mFragment.getActivity().findViewById(R.id.humidity);
            //Button filterStatusBtn = (Button) mFragment.getActivity().findViewById(R.id.filter_status);
            final RelativeLayout thermostatSelectView = (RelativeLayout) mFragment.getActivity().findViewById(R.id.thermostat_select_layout);
            final Spinner thermostatSelectSpinner = (Spinner) mFragment.getActivity().findViewById(R.id.thermostat_select);
            final LinearLayout heatRowLayout = (LinearLayout) mFragment.getActivity().findViewById(R.id.heat_row_layout);
            final LinearLayout coolRowLayout = (LinearLayout) mFragment.getActivity().findViewById(R.id.cool_row_layout);

            if(thermostatDetailsItem.getThermostats() == null || thermostatDetailsItem.getThermostats().size() == 0) {
                Toast.makeText(mContext, "You do not have any thermostats on your account.", Toast.LENGTH_LONG).show();
                //FragmentManager fragmentManager = mFragment.getFragmentManager();
                //fragmentManager.beginTransaction().remove(mFragment).commit();
                //fragmentManager.popBackStack();
            }

            if(thermostatDetailsItem.getThermostats() != null && thermostatDetailsItem.getThermostats().size() > 1) {
                List<String> temp = new ArrayList<String>();
                for(ThermostatItem tItem : thermostatDetailsItem.getThermostats()) {
                    temp.add(tItem.getName());
                }
                StatusSpinnerAdapter spinnerArrayAdapter = new StatusSpinnerAdapter(mContext, android.R.layout.simple_spinner_item, temp);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                thermostatSelectSpinner.setAdapter(spinnerArrayAdapter);

                mFragment.setThermostats(thermostatDetailsItem.getThermostats());
                if("".equalsIgnoreCase(mThermostatID)) {
                    //Select first thermostat in the list
                    mFragment.userAction = false;
                    mFragment.setThermostatID(thermostatDetailsItem.getThermostats().get(0).getId());
                    thermostatSelectSpinner.setSelection(0);
                } else {
                    //Select correct thermostat
                    int position = 0;
                    mFragment.userAction = false;
                    mFragment.setThermostatID(mThermostatID);
                    for(ThermostatItem item : thermostatDetailsItem.getThermostats()) {
                        if(item.getId().equalsIgnoreCase(mThermostatID)) {
                            thermostatSelectSpinner.setSelection(position);
                            break;
                        }
                        position++;
                    }
                }
                thermostatSelectView.setVisibility(View.VISIBLE);
            } else if(thermostatDetailsItem.getThermostats() != null && thermostatDetailsItem.getThermostats().size() == 1) {
                mThermostatID = thermostatDetailsItem.getThermostats().get(0).getId();
                mFragment.setThermostatID(mThermostatID);
                mFragment.setThermostats(null);
                thermostatSelectView.setVisibility(View.GONE);
            } else {
                mFragment.setThermostats(null);
                thermostatSelectView.setVisibility(View.GONE);
            }

            currentTemperature.setText(thermostatDetailsItem.getCurrentTemperature());
            //humidityTxt.setText(thermostatDetailsItem.getHumidity());
            mFragment.currentFilterValue = Integer.parseInt(thermostatDetailsItem.getFilterStatus());
            boolean useLocalFilterRuntime = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_LOCAL_FILTER_RUNTIME, true);

            if(useLocalFilterRuntime) {
                if ((Integer.parseInt(thermostatDetailsItem.getFilterStatus()) - mSharedPrefs.getInt("lastFilterReset", 0)) > Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_FILTER_HOURS, "0"))) {
                    //filterStatusBtn.setBackgroundResource(R.drawable.ic_filter_change);
                } else {
                    //filterStatusBtn.setBackgroundResource(R.drawable.ic_filter_ok);
                }
            } else {
                if (Integer.parseInt(thermostatDetailsItem.getFilterStatus()) > Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_FILTER_HOURS, "0"))) {
                    //filterStatusBtn.setBackgroundResource(R.drawable.ic_filter_change);
                } else {
                    //filterStatusBtn.setBackgroundResource(R.drawable.ic_filter_ok);
                }
            }

            heatTemperature.setText(thermostatDetailsItem.getHeatTargetTemperature());
            coolTemperature.setText(thermostatDetailsItem.getCoolTargetTemperature());

            if("AUTO".equalsIgnoreCase(thermostatDetailsItem.getMode())) {
                //Auto mode - show both controls
                heatRowLayout.setVisibility(View.VISIBLE);
                coolRowLayout.setVisibility(View.VISIBLE);
                mFragment.setTargetHeat(thermostatDetailsItem.getHeatTargetTemperature());
                mFragment.setTargetCool(thermostatDetailsItem.getCoolTargetTemperature());
                modeSpinner.setSelection(2);
            } else if("HEAT".equalsIgnoreCase(thermostatDetailsItem.getMode())) {
                //Heat mode - show heat controls
                mFragment.setTargetHeat(null);
                mFragment.setTargetCool(null);
                heatRowLayout.setVisibility(View.VISIBLE);
                coolRowLayout.setVisibility(View.GONE);
                modeSpinner.setSelection(1);
            } else if("COOL".equalsIgnoreCase(thermostatDetailsItem.getMode())) {
                //Cool mode - show cool controls
                mFragment.setTargetHeat(null);
                mFragment.setTargetCool(null);
                heatRowLayout.setVisibility(View.GONE);
                coolRowLayout.setVisibility(View.VISIBLE);
                modeSpinner.setSelection(0);
            } else if("OFF".equalsIgnoreCase(thermostatDetailsItem.getMode())) {
                //Off
                mFragment.setTargetHeat(null);
                mFragment.setTargetCool(null);
                heatRowLayout.setVisibility(View.GONE);
                coolRowLayout.setVisibility(View.GONE);
                modeSpinner.setSelection(3);
            }


            /*
            if("OK".equalsIgnoreCase(filterStatus)) {
                filterChangeBtn.setVisibility(View.GONE);
                filterStatusTxt.setVisibility(View.VISIBLE);
                filterStatusTxt.setText("OK");
            } else {
                filterChangeBtn.setVisibility(View.VISIBLE);
                filterStatusTxt.setVisibility(View.GONE);
            }
            */

            /*
            //Update widget
            try {
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                RemoteViews mViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_thermostat);
                ComponentName thisWidget = new ComponentName(mContext, ThermostatWidget.class);
                mViews.setTextViewText(R.id.widget_thermostat_current_temp, currentTemperature);
                mViews.setTextViewText(R.id.widget_thermostat_mode, mode.toUpperCase());
                mViews.setTextViewText(R.id.widget_thermostat_target_and_humidity, targetTemperature + (char) 0x00B0 + "/" + humidity + "%");
                appWidgetManager.updateAppWidget(thisWidget, mViews);
            } catch (Exception e) {
                //No widget?
                logger.log(IrisPlusConstants.LOG_INFO, "Error: Could not update widget. Error message: " + e.getMessage());
            }
            */
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
}