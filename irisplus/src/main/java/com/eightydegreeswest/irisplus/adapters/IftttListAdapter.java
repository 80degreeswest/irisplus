package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import com.eightydegreeswest.irisplus.fragments.IftttFragment;
import com.eightydegreeswest.irisplus.model.DeviceItem;

import java.util.ArrayList;
import java.util.List;

public class IftttListAdapter extends ArrayAdapter<DeviceItem> {
	private final Context context;
	private List<DeviceItem> devices = new ArrayList<DeviceItem>();
	private IftttFragment fragment;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();
	private SharedPreferences mSharedPrefs = null;

	public IftttListAdapter(Context context, List<DeviceItem> devices, IftttFragment fragment) {
		super(context, R.layout.list_ifttt);
		this.context = context;
		this.devices = devices;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_ifttt, parent, false);
		TextView textView = (TextView) rowView.findViewById(R.id.ifttt_name);
		final ToggleButton status = (ToggleButton) rowView.findViewById(R.id.ifttt_status_button);
		textView.setText(devices.get(position).getDeviceName());

		if(mSharedPrefs.getBoolean(devices.get(position).getDeviceName(), false)) {
			status.setChecked(true);
		} else {
			status.setChecked(false);
			//textView.setTextColor(Color.RED);
			//description.setTextColor(Color.RED);
		}

        final int currentRow = position;
		
		status.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                notificationHelper.buttonFeedback();
		        if (currentRow != ListView.INVALID_POSITION) {
		        	if(devices.get(currentRow).getDeviceName() != null) {
						mSharedPrefs.edit().putBoolean(devices.get(currentRow).getDeviceName(), status.isChecked()).commit();
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