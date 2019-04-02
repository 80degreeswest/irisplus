package com.eightydegreeswest.irisplus.fragments;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.eightydegreeswest.irisplus.BuildConfig;
import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.adapters.CustomDrawerAdapter;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DeviceItem;
import com.eightydegreeswest.irisplus.model.DrawerItem;
import com.eightydegreeswest.irisplus.tasks.ControlViewTask;
import com.eightydegreeswest.irisplus.tasks.DeviceViewTask;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    
    CustomDrawerAdapter adapter;
    List<DrawerItem> dataList;

    private DrawerItem selectedMenuItem;

    private SharedPreferences mSharedPrefs;
    private IrisPlusLogger logger = new IrisPlusLogger();

    private static NavigationDrawerFragment fragment;
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static NavigationDrawerFragment newInstance(int sectionNumber) {
        fragment = new NavigationDrawerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity();
        
		// Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mUserLearnedDrawer = mSharedPrefs.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        /*
        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if(fragmentManager.getBackStackEntryCount() > 0) {
                    String fragmentName = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
                    System.out.println("New fragment selected: " + fragmentName);
                    updateMenuSelection(fragmentName);
                }
            }
        });
        */
        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    public void refreshFragment() {
        List<DeviceItem> devices = new ArrayList<DeviceItem>();
        List<String> navOptions = new ArrayList<>();
        List<DrawerItem> menuList = new ArrayList<>();
        //Always show Dashboard
        menuList.add(new DrawerItem(getString(R.string.title_dashboard), R.drawable.ic_dashboard, menuList.size()));

        try {
            FileInputStream fileInputStream = IrisPlus.getContext().openFileInput("irisplus-nav-list.dat");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            devices = (ArrayList<DeviceItem>) objectInputStream.readObject();
            objectInputStream.close();
            for(DeviceItem item : devices) {
                navOptions.add(item.getDeviceTypeHint());
            }
            logger.log(IrisPlusConstants.LOG_INFO, "Nav types: " + navOptions.toString());
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not load navigation drawer from devices." + e);
        }

        boolean debug = false;
        //debug = BuildConfig.DEBUG;

        if((navOptions.contains("KeyPad") && (navOptions.contains("Motion") || navOptions.contains("Contact")))) {
            menuList.add(new DrawerItem(getString(R.string.title_security), R.drawable.ic_security, menuList.size()));
        }
        if(navOptions.contains("Switch") || navOptions.contains("Fan Control")) {
            menuList.add(new DrawerItem(getString(R.string.title_control), R.drawable.ic_control, menuList.size()));
        }
        if(navOptions.contains("Thermostat")) {
            menuList.add(new DrawerItem(getString(R.string.title_thermostat), R.drawable.ic_thermostat, menuList.size()));
        }
        if(navOptions.contains("Lock") || navOptions.contains("Garage Door")) {
            menuList.add(new DrawerItem(getString(R.string.title_locks), R.drawable.ic_lock, menuList.size()));
        }
        if(navOptions.size() > 0 && mSharedPrefs.getBoolean(IrisPlusConstants.PREF_PREMIUM_LOCK, true)) {
            menuList.add(new DrawerItem(getString(R.string.title_devices), R.drawable.ic_device, menuList.size()));
        }
        if(navOptions.contains("Camera")) {
            //menuList.add(new DrawerItem(getString(R.string.title_cameras), R.drawable.ic_camera, menuList.size()));
        }

        menuList.add(new DrawerItem(getString(R.string.title_history), R.drawable.ic_history, menuList.size()));

        if(navOptions.size() > 0) {
            menuList.add(new DrawerItem(getString(R.string.title_presence), R.drawable.ic_presence, menuList.size()));
        }
        if(navOptions.contains("Petdoor")) {   //TODO: verify
            //menuList.add(new DrawerItem(getString(R.string.title_petdoors), R.drawable.ic_pet, menuList.size()));
        }
        if(navOptions.contains("Irrigation")) {
            menuList.add(new DrawerItem(getString(R.string.title_irrigation), R.drawable.ic_irrigation, menuList.size()));
        }
        if(navOptions.contains("Switch")) {
            menuList.add(new DrawerItem(getString(R.string.title_usage), R.drawable.ic_energy, menuList.size()));
        }
        if(navOptions.size() > 0) {
            //menuList.add(new DrawerItem(getString(R.string.title_care), R.drawable.ic_care, menuList.size()));
        }

        menuList.add(new DrawerItem(getString(R.string.title_scene), R.drawable.ic_scene, menuList.size()));

        menuList.add(new DrawerItem(getString(R.string.title_rules), R.drawable.ic_rules, menuList.size()));

        menuList.add(new DrawerItem(getString(R.string.title_hub), R.drawable.ic_hub, menuList.size()));

        menuList.add(new DrawerItem("IFTTT", R.drawable.ic_ifttt, menuList.size()));

        adapter.updateAdapterList(menuList);
        //mDrawerListView.setAdapter(adapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
    }

    @SuppressLint("InlinedApi")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        dataList = new ArrayList<DrawerItem>();
        adapter = new CustomDrawerAdapter(inflater.getContext(), R.layout.custom_drawer_item, dataList);
        mDrawerListView.setAdapter(adapter);
        mDrawerListView.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        if(mSharedPrefs.contains(IrisPlusConstants.PREF_USERNAME)) {
            TaskHelper.execute(new DeviceViewTask(fragment));
        }
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                refreshFragment();
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    Context context = getActivity();
                    SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
                    mSharedPrefs.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            //mDrawerLayout.openDrawer(mFragmentContainerView);
            getActivity().getFragmentManager().beginTransaction().replace(R.id.container, DashboardFragment.newInstance(1)).commit();
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        this.setSelectedMenuItem();
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.iris, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    @SuppressLint("InlinedApi")
	private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    public DrawerItem getSelectedMenuItem() {
        return selectedMenuItem;
    }

    public void setSelectedMenuItem() {
        try {
            this.selectedMenuItem = dataList.get(0);

            for(DrawerItem drawerItem : dataList){
                if(drawerItem.getPosition() == mCurrentSelectedPosition) {
                    this.selectedMenuItem = drawerItem;
                    mDrawerListView.setItemChecked(drawerItem.getPosition(), true);
                    break;
                }
            }
        } catch (Exception e) {
            //Do nothing - return first item in the list that was initialized
        }
    }

    public void updateMenuSelection(String selectedMenuName) {
        try {
            if("Energy Cost".equalsIgnoreCase(selectedMenuName)) {
                selectedMenuName = "Energy Usage";
            }
            for(DrawerItem drawerItem : dataList){
                if(drawerItem.getItemName().equalsIgnoreCase(selectedMenuName)) {
                    this.selectedMenuItem = drawerItem;
                    break;
                }
            }

            getActionBar().setTitle(selectedMenuItem.getItemName());
            ((IrisActivity) this.getActivity()).setmTitle(selectedMenuItem.getItemName());
            mCurrentSelectedPosition = selectedMenuItem.getPosition();
            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
            if (mDrawerListView != null) {
                mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
            }
        } catch (Exception e) {
            //Do nothing - return first item in the list that was initialized
        }
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
		          long id) {
		    selectItem(position);
		
		}
    }
}
