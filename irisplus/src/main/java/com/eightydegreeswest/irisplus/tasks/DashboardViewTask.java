package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.ListView;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.DashboardListAdapter;
import com.eightydegreeswest.irisplus.apiv2.DashboardApi;
import com.eightydegreeswest.irisplus.apiv2.SceneApi;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.DashboardFragment;
import com.eightydegreeswest.irisplus.model.DashboardItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DashboardViewTask extends AsyncTask<Void, Void, Boolean> {

    private DashboardFragment mFragment = null;
    private Context mContext = null;
    private IrisPlusLogger logger = new IrisPlusLogger();

    private SharedPreferences mSharedPrefs = null;
    private List<DashboardItem> dashboards = new ArrayList<DashboardItem>();
    private List<DashboardItem> apiReturn = new ArrayList<DashboardItem>();
    private DashboardListAdapter adapter = null;
    private int notifyID = 0;
    NotificationHelper notificationHelper;

    @SuppressWarnings("unchecked")
    public DashboardViewTask(DashboardFragment fragment) {
        mFragment = fragment;
        mContext = IrisPlus.getContext();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        notificationHelper = new NotificationHelper(mContext);
        notifyID = notificationHelper.createRefreshNotification();

        try {
            //dashboardsList = mSharedPrefs.getString("dashboardCache", "");
            //homeStatusList = mSharedPrefs.getString("homeStatusCache", "");
            //if(!"".equalsIgnoreCase(dashboardsList)) {
            //    this.performCommand();
            //}

            FileInputStream fileInputStream = mContext.openFileInput("irisplus-dashboards-list.dat");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            dashboards = (ArrayList<DashboardItem>) objectInputStream.readObject();
            objectInputStream.close();

            adapter = mFragment.getDashboardListAdapter();

            if(adapter == null) {
                adapter = new DashboardListAdapter(mContext, dashboards, mFragment);
                ListView deviceList = (ListView) mFragment.getActivity().findViewById(R.id.dashboard_fragment_view);
                deviceList.setAdapter(adapter);
                mFragment.setDashboardListAdapter(adapter);
            }
        } catch (Exception e) {
            //No cache - ignore
            //e.printStackTrace();
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        DashboardApi irisApi = new DashboardApi(mContext);
        apiReturn = irisApi.getDashboard();
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (mFragment.isAdded()) {
            this.performCommand();
            mFragment.setDashboardViewTask(null);
            mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        }
        notificationHelper.destroyNotification(notifyID);
    }

    @Override
    protected void onCancelled() {
        mFragment.setDashboardViewTask(null);
        mFragment.getmSwipeRefreshLayout().setRefreshing(false);
        notificationHelper.destroyNotification(notifyID);
    }

    protected void performCommand() {
        try {
            dashboards = new ArrayList<DashboardItem>();

            if(apiReturn != null) {
                dashboards.addAll(apiReturn);
            }

            adapter = mFragment.getDashboardListAdapter();

            if(adapter == null) {
                adapter = new DashboardListAdapter(mContext, dashboards, mFragment);
                ListView dashboardList = (ListView) mFragment.getActivity().findViewById(R.id.dashboard_fragment_view);
                dashboardList.setAdapter(adapter);
            } else {
                adapter.updateAdapterList(dashboards);
            }
            mFragment.setDashboardListAdapter(adapter);

            try {
                //mSharedPrefs.edit().putString("dashboardCache", dashboardsList).commit();
                //mSharedPrefs.edit().putString("homeStatusCache", homeStatusList).commit();

                FileOutputStream fileOutputStream = mContext.openFileOutput("irisplus-dashboards-list.dat", Context.MODE_PRIVATE);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(dashboards);
                objectOutputStream.close();
            } catch (Exception e) {
                //No cache - ignore
                //e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}