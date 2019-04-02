package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.fragments.DeviceFragment;
import com.eightydegreeswest.irisplus.model.DeviceItem;

import java.util.ArrayList;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<DeviceItem> {
	private final Context context;
	private List<DeviceItem> devices = new ArrayList<DeviceItem>();
	private DeviceFragment fragment;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public DeviceListAdapter(Context context, List<DeviceItem> devices, DeviceFragment fragment) {
		super(context, R.layout.list_device);
		this.context = context;
		this.devices = devices;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_device, parent, false);

        TextView name = (TextView) rowView.findViewById(R.id.device_name);
        TextView state = (TextView) rowView.findViewById(R.id.device_state);
        TextView batteryPercentage = (TextView) rowView.findViewById(R.id.device_battery);
        TextView signal = (TextView) rowView.findViewById(R.id.device_signal);
        TextView temperature = (TextView) rowView.findViewById(R.id.device_temperature);
        TextView type = (TextView) rowView.findViewById(R.id.device_type);
        TextView status = (TextView) rowView.findViewById(R.id.device_status);
        ImageView batteryImg = (ImageView) rowView.findViewById(R.id.device_battery_img);
        ImageView signalImg = (ImageView) rowView.findViewById(R.id.device_signal_img) ;

        name.setText(devices.get(position).getDeviceName());
        if(devices.get(position).getPower() != null) {
            state.setText(devices.get(position).getState() + ", " + devices.get(position).getPower() + "W");
        } else {
            state.setText(devices.get(position).getState());
        }
        batteryPercentage.setText(devices.get(position).getBatteryPercentage());
        signal.setText(devices.get(position).getSignal());
        temperature.setText(devices.get(position).getTemperature());
        type.setText(devices.get(position).getDeviceTypeHint() + " (" + devices.get(position).getType() + ")");
        status.setText(devices.get(position).getStatus());

        if("OFFLINE".equalsIgnoreCase(devices.get(position).getStatus())) {
            status.setTextColor(Color.RED);
            rowView.setBackgroundResource(R.drawable.pinkgradient);
        }

        try {
            if(!"n/a".equalsIgnoreCase(devices.get(position).getSignal()) && Integer.parseInt(devices.get(position).getSignal()) < 20) {
                signal.setTextColor(Color.RED);
                signalImg.setBackgroundResource(R.drawable.ic_signal_low);

            }
        } catch(Exception e) { }

        try {
            if(!"ac".equalsIgnoreCase(devices.get(position).getBatteryPercentage()) && Integer.parseInt(devices.get(position).getBatteryPercentage()) < 30) {
                batteryPercentage.setTextColor(Color.RED);
                batteryImg.setBackgroundResource(R.drawable.ic_battery_empty);
            }
        } catch(Exception e) { }

        final int currentRow = position;

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRow != ListView.INVALID_POSITION) {
                    //notificationHelper.buttonFeedback();
                    String id = devices.get(currentRow).getId();
                    if(id != null) {
                        //rowView.setSelected(true);
                        //FragmentManager fragmentManager = fragment.getActivity().getFragmentManager();
                        //fragmentManager.beginTransaction().add(R.id.container, RuleFragment.newInstance(99, id)).addToBackStack(null).commit();
                    }
                }
            }
        });

        return rowView;
    }

	@Override
	public int getCount() {
		return devices != null ? devices.size() : 0;
	}

    public void updateAdapterList(List<DeviceItem> newList) {
        this.devices.clear();
        this.devices.addAll(newList);
        this.notifyDataSetChanged();
    }
}