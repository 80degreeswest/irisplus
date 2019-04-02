package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.fragments.PresenceFragment;
import com.eightydegreeswest.irisplus.model.PresenceItem;

import java.util.ArrayList;
import java.util.List;

public class PresenceListAdapter extends ArrayAdapter<PresenceItem> {
	private final Context context;
	private List<PresenceItem> presences = new ArrayList<PresenceItem>();
	private PresenceFragment fragment;
	//private IrisPlusLogger logger = new IrisPlusLogger();

	public PresenceListAdapter(Context context, List<PresenceItem> presences, PresenceFragment fragment) {
		super(context, R.layout.list_presence);
		this.context = context;
		this.presences = presences;
		this.fragment = fragment;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_presence, parent, false);
		TextView name = (TextView) rowView.findViewById(R.id.presence_name);
		final ToggleButton status = (ToggleButton) rowView.findViewById(R.id.presence_status_button);

		name.setText(presences.get(position).getName());

        if("AWAY".equalsIgnoreCase(presences.get(position).getState())) {
			status.setChecked(false);
        } else {
			status.setChecked(true);
		}

        return rowView;
    }

	@Override
	public int getCount() {
		return presences != null ? presences.size() : 0;
	}

    public void updateAdapterList(List<PresenceItem> newList) {
        this.presences.clear();
        this.presences.addAll(newList);
        this.notifyDataSetChanged();
    }
}