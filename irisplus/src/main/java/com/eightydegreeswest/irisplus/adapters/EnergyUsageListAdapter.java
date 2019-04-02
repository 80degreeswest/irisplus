package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.fragments.EnergyUsageFragment;
import com.eightydegreeswest.irisplus.model.DeviceItem;

import java.util.ArrayList;
import java.util.List;

public class EnergyUsageListAdapter extends ArrayAdapter<DeviceItem> {
	private final Context context;
	private List<DeviceItem> devices = new ArrayList<DeviceItem>();
	private EnergyUsageFragment fragment;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public EnergyUsageListAdapter(Context context, List<DeviceItem> devices, EnergyUsageFragment fragment) {
		super(context, R.layout.list_energy);
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
		final View rowView = inflater.inflate(R.layout.list_energy, parent, false);

        TextView name = (TextView) rowView.findViewById(R.id.energy_device_name);
        TextView usage = (TextView) rowView.findViewById(R.id.energy_device_usage);

        name.setText(devices.get(position).getDeviceName());
        usage.setText(devices.get(position).getPower() + "W");

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