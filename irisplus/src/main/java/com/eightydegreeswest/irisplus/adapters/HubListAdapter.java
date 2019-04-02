package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.fragments.HubFragment;
import com.eightydegreeswest.irisplus.model.DashboardItem;

import java.util.ArrayList;
import java.util.List;

public class HubListAdapter extends ArrayAdapter<DashboardItem> {
	private final Context context;
	private List<DashboardItem> hubDetails = new ArrayList<DashboardItem>();
	private HubFragment fragment;
    private NotificationHelper notificationHelper;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public HubListAdapter(Context context, List<DashboardItem> hubDetails, HubFragment fragment) {
		super(context, R.layout.list_hub);
		this.context = context;
		this.hubDetails = hubDetails;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_hub, parent, false);
		TextView heading = (TextView) rowView.findViewById(R.id.hub_heading);
        TextView status = (TextView) rowView.findViewById(R.id.hub_status);

		heading.setText(hubDetails.get(position).getHeading());
		try {
			status.setText(Html.fromHtml(hubDetails.get(position).getStatus()));
		} catch (Exception e) {
			status.setText(Html.fromHtml("N/A"));
		}

        return rowView;
    }

	@Override
	public int getCount() {
		return hubDetails != null ? hubDetails.size() : 0;
	}

    public void updateAdapterList(List<DashboardItem> newList) {
        this.hubDetails.clear();
        this.hubDetails.addAll(newList);
        this.notifyDataSetChanged();
    }
}