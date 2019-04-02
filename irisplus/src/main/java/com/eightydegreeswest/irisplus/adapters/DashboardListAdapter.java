package com.eightydegreeswest.irisplus.adapters;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.eightydegreeswest.irisplus.BuildConfig;
import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.fragments.ControlFragment;
import com.eightydegreeswest.irisplus.fragments.DashboardFragment;
import com.eightydegreeswest.irisplus.fragments.DeviceFragment;
import com.eightydegreeswest.irisplus.fragments.EnergyUsageFragment;
import com.eightydegreeswest.irisplus.fragments.LockFragment;
import com.eightydegreeswest.irisplus.fragments.NavigationDrawerFragment;
import com.eightydegreeswest.irisplus.fragments.PresenceFragment;
import com.eightydegreeswest.irisplus.fragments.SecurityFragment;
import com.eightydegreeswest.irisplus.fragments.ThermostatFragment;
import com.eightydegreeswest.irisplus.model.DashboardItem;

import java.util.ArrayList;
import java.util.List;

public class DashboardListAdapter extends ArrayAdapter<DashboardItem> {
	private final Context context;
	private List<DashboardItem> dashboards = new ArrayList<DashboardItem>();
	private DashboardFragment fragment;
    private NotificationHelper notificationHelper;
    public boolean pinEntered = false;
    //private IrisPlusLogger logger = new IrisPlusLogger();

	public DashboardListAdapter(Context context, List<DashboardItem> dashboards, DashboardFragment fragment) {
		super(context, R.layout.list_dashboard);
		this.context = context;
		this.dashboards = dashboards;
		this.fragment = fragment;
        this.notificationHelper = new NotificationHelper(context);
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(int position, final View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.list_dashboard, parent, false);
		TextView heading = (TextView) rowView.findViewById(R.id.dashboard_heading);
        TextView status = (TextView) rowView.findViewById(R.id.dashboard_status);

		heading.setText(dashboards.get(position).getHeading());
        status.setText(Html.fromHtml(dashboards.get(position).getStatus()));

        final int currentRow = position;

        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentRow != ListView.INVALID_POSITION) {
                    notificationHelper.buttonFeedback();
                    String screen = dashboards.get(currentRow).getHeading();
                    if(screen != null && !"".equalsIgnoreCase(screen)) {
                        rowView.setSelected(true);
                        FragmentManager fragmentManager = fragment.getActivity().getFragmentManager();
                        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment) fragment.getActivity().getFragmentManager().findFragmentById(R.id.navigation_drawer);
                        navigationDrawerFragment.updateMenuSelection(screen);
                        boolean premium = ((IrisActivity) fragment.getActivity()).billingHelper.premium || BuildConfig.DEBUG;

                        ((IrisActivity) fragment.getActivity()).setBackButtonPressed(0);

                        if ("security".equalsIgnoreCase(screen)) {
                            fragmentManager.beginTransaction().replace(R.id.container, SecurityFragment.newInstance(99)).commit();
                        } else if ("control".equalsIgnoreCase(screen)) {
                            fragmentManager.beginTransaction().replace(R.id.container, ControlFragment.newInstance(99)).commit();
                        } else if ("thermostat".equalsIgnoreCase(screen)) {
                            fragmentManager.beginTransaction().replace(R.id.container, ThermostatFragment.newInstance(99)).commit();
                        } else if ("presence".equalsIgnoreCase(screen) && premium) {
                            fragmentManager.beginTransaction().replace(R.id.container, PresenceFragment.newInstance(99)).commit();
                        } else if ("locks".equalsIgnoreCase(screen)) {
                            fragmentManager.beginTransaction().replace(R.id.container, LockFragment.newInstance(99)).commit();
                        } else if (premium && ("contact sensors".equalsIgnoreCase(screen))) {
                            fragmentManager.beginTransaction().replace(R.id.container, DeviceFragment.newInstance(99)).commit();
                        } else if (premium && ("status".equalsIgnoreCase(screen) || "home status".equalsIgnoreCase(screen))) {
                            fragmentManager.beginTransaction().replace(R.id.container, DeviceFragment.newInstance(99, true)).commit();
                        } else if ("energy usage".equalsIgnoreCase(screen)) {
                            fragmentManager.beginTransaction().replace(R.id.container, EnergyUsageFragment.newInstance(99)).commit();
                        } else {
                            ((IrisActivity) fragment.getActivity()).billingHelper.mHelper.launchPurchaseFlow(fragment.getActivity(), ((IrisActivity) fragment.getActivity()).PREMIUM_SKU, 10001, ((IrisActivity) fragment.getActivity()).billingHelper.mPurchaseFinishedListener, "premium");
                        }
                    }
                }
            }
        });

        return rowView;
    }

	@Override
	public int getCount() {
		return dashboards != null ? dashboards.size() : 0;
	}

    public void updateAdapterList(List<DashboardItem> newList) {
        this.dashboards.clear();
        this.dashboards.addAll(newList);
        this.notifyDataSetChanged();
    }
}