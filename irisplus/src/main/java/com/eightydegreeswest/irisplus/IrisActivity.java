package com.eightydegreeswest.irisplus;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.common.AlarmManagerHelper;
import com.eightydegreeswest.irisplus.common.AutomationBroadcast;
import com.eightydegreeswest.irisplus.common.AutomationService;
import com.eightydegreeswest.irisplus.common.BillingHelper;
import com.eightydegreeswest.irisplus.common.IrisPlus;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.common.NotificationHelper;
import com.eightydegreeswest.irisplus.common.Preferences;
import com.eightydegreeswest.irisplus.common.TaskHelper;
import com.eightydegreeswest.irisplus.common.VoiceCommandInterpreter;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.fragments.CareFragment;
import com.eightydegreeswest.irisplus.fragments.ControlFragment;
import com.eightydegreeswest.irisplus.fragments.DashboardFragment;
import com.eightydegreeswest.irisplus.fragments.DeviceFragment;
import com.eightydegreeswest.irisplus.fragments.EnergyUsageFragment;
import com.eightydegreeswest.irisplus.fragments.HelpFragment;
import com.eightydegreeswest.irisplus.fragments.HistoryFragment;
import com.eightydegreeswest.irisplus.fragments.HubFragment;
import com.eightydegreeswest.irisplus.fragments.IftttFragment;
import com.eightydegreeswest.irisplus.fragments.IrrigationFragment;
import com.eightydegreeswest.irisplus.fragments.LockFragment;
import com.eightydegreeswest.irisplus.fragments.NavigationDrawerFragment;
import com.eightydegreeswest.irisplus.fragments.PetFragment;
import com.eightydegreeswest.irisplus.fragments.PresenceFragment;
import com.eightydegreeswest.irisplus.fragments.RuleFragment;
import com.eightydegreeswest.irisplus.fragments.SceneFragment;
import com.eightydegreeswest.irisplus.fragments.SecurityFragment;
import com.eightydegreeswest.irisplus.fragments.ThermostatFragment;
import com.eightydegreeswest.irisplus.model.HubItem;
import com.eightydegreeswest.irisplus.tasks.DeviceViewTask;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

/**
 * @author ybelenitsky
 */
public class IrisActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	private SharedPreferences mSharedPrefs;
	private IrisPlusLogger logger = new IrisPlusLogger();
	Messenger mService = null;
	boolean mIsBound;
	final Messenger mMessenger = new Messenger(new Handler());
    private String selectedMenuItem = null;
    public boolean pinEntered = false;
    NotificationHelper notificationHelper;
    public static BillingHelper billingHelper;
    private static final int SPEECH_REQUEST_CODE = 0;
    private int backButtonPressed = 0;
    private AlertDialog debugDialog;

    private static final String TAG = IrisPlusConstants.TAG;
    private static final String base64EncodedPublicKey = IrisPlusConstants.base64EncodedPublicKey;
    public static final String PREMIUM_SKU = IrisPlusConstants.PREMIUM_SKU;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_iris);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        IrisPlus.setCurrentActivity(this);

        this.scheduleAutomationAlarms();

        onNewIntent(getIntent());

        AppRate.with(this)
                .setInstallDays(5) // default 10, 0 means install day.
                .setLaunchTimes(5) // default 10 times.
                .setRemindInterval(2) // default 1 day.
                .setDebug(false) // default false.
                .setShowTitle(false) // default true
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {

                    }
                })
                .monitor();

        // Show a dialog if meets conditions.
        AppRate.showRateDialogIfMeetsConditions(this);

        billingHelper = new BillingHelper(this, PREMIUM_SKU, base64EncodedPublicKey, TAG);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You are currently in a debug mode. This can decrease performance. If you no longer require assistance, you should turn it off. You can also use the button below to send logs to support for troubleshooting.").setTitle("Debug Mode");
        builder.setPositiveButton("Send Logs", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                logger.emailLog(IrisPlus.getCurrentActivity());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Turn Off", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mSharedPrefs.edit().putBoolean(IrisPlusConstants.PREF_DEBUG, false).commit();
                logger.setDebug(false);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        debugDialog = builder.create();

        showWhatsNewDialog();
    }
    
	@Override
    public void onNavigationDrawerItemSelected(int position) {
        selectedMenuItem = "Dashboard";
        try {
            selectedMenuItem = mNavigationDrawerFragment.getSelectedMenuItem().getItemName();
        } catch (Exception e) {
            //Default to selecting dashboard as it's the first item and will always be present.
            selectedMenuItem = "Dashboard";
        }
        logger.log(IrisPlusConstants.LOG_INFO, "Selected menu item: " + selectedMenuItem);
        this.selectAndAttachFragment(selectedMenuItem, position);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if("security".equalsIgnoreCase(selectedMenuItem)) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, SecurityFragment.newInstance(1)).commitAllowingStateLoss();
        } else if("thermostat".equalsIgnoreCase(selectedMenuItem)) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, ThermostatFragment.newInstance(1)).commitAllowingStateLoss();
        } /*else if("energy usage".equalsIgnoreCase(selectedMenuItem)) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, EnergyUsageFragment.newInstance(1)).commit();
        }*/
    }

    private void selectAndAttachFragment(String selectedMenuItem, int position) {
        // update the main content by replacing fragments

        Context context = this.getApplicationContext();
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String username = mSharedPrefs.getString(IrisPlusConstants.PREF_USERNAME, "");
        String password = mSharedPrefs.getString(IrisPlusConstants.PREF_PASSWORD, "");
        FragmentManager fragmentManager = getFragmentManager();

        logger.log(IrisPlusConstants.LOG_INFO, "Switching fragment through navigation: " + selectedMenuItem);
        boolean premium = false;
        backButtonPressed = 0;

        try {
            premium = (billingHelper.premium || BuildConfig.DEBUG);
        } catch (Exception e) { }

        if (username == null || "".equals(username) || password == null || "".equals(password)) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if ("security".equalsIgnoreCase(selectedMenuItem)) {
            this.checkPinSecurity();
            fragmentManager.beginTransaction().replace(R.id.container, SecurityFragment.newInstance(position + 1), "security").commitAllowingStateLoss();
        } else if ("dashboard".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, DashboardFragment.newInstance(position + 1), "dashboard").commitAllowingStateLoss();
        } else if ("control".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, ControlFragment.newInstance(position + 1), "control").commitAllowingStateLoss();
        } else if ("locks".equalsIgnoreCase(selectedMenuItem)) {
            this.checkPinSecurity();
            fragmentManager.beginTransaction().replace(R.id.container, LockFragment.newInstance(position + 1), "locks").commitAllowingStateLoss();
        } else if ("thermostat".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, ThermostatFragment.newInstance(position + 1), "thermostat").commitAllowingStateLoss();
        } else if ("history".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, HistoryFragment.newInstance(position + 1), "history").commitAllowingStateLoss();
        } else if ("devices".equalsIgnoreCase(selectedMenuItem) && premium) {
            fragmentManager.beginTransaction().replace(R.id.container, DeviceFragment.newInstance(position + 1), "devices").commitAllowingStateLoss();
        } else if ("energy usage".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, EnergyUsageFragment.newInstance(position + 1), "energy").commitAllowingStateLoss();
        } else if ("presence".equalsIgnoreCase(selectedMenuItem) && premium) {
            fragmentManager.beginTransaction().replace(R.id.container, PresenceFragment.newInstance(position + 1), "presence").commitAllowingStateLoss();
        } else if ("pets".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, PetFragment.newInstance(position + 1), "pets").commitAllowingStateLoss();
        } else if ("irrigation".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, IrrigationFragment.newInstance(position + 1), "irrigation").commitAllowingStateLoss();
        } else if ("care".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, CareFragment.newInstance(position + 1), "care").commitAllowingStateLoss();
        } else if ("hub".equalsIgnoreCase(selectedMenuItem) && premium) {
            fragmentManager.beginTransaction().replace(R.id.container, HubFragment.newInstance(position + 1), "hub").commitAllowingStateLoss();
        } else if ("scenes".equalsIgnoreCase(selectedMenuItem) && premium) {
            fragmentManager.beginTransaction().replace(R.id.container, SceneFragment.newInstance(position + 1), "scenes").commitAllowingStateLoss();
        } else if ("rules".equalsIgnoreCase(selectedMenuItem) && premium) {
            fragmentManager.beginTransaction().replace(R.id.container, RuleFragment.newInstance(position + 1), "rules").commitAllowingStateLoss();
        } else if ("ifttt".equalsIgnoreCase(selectedMenuItem) && premium) {
            fragmentManager.beginTransaction().replace(R.id.container, IftttFragment.newInstance(position + 1), "ifttt").commitAllowingStateLoss();
        } else if ("help".equalsIgnoreCase(selectedMenuItem)) {
            fragmentManager.beginTransaction().replace(R.id.container, HelpFragment.newInstance(position + 1), "help").commitAllowingStateLoss();
        } else if ("login".equalsIgnoreCase(selectedMenuItem)) {
            Intent login = new Intent(context, LoginActivity.class);
            startActivity(login);
        } else if ("settings".equalsIgnoreCase(selectedMenuItem)) {
            startActivity(new Intent(this, Preferences.class));
        } else if ("send logs".equalsIgnoreCase(selectedMenuItem)) {
            IrisPlusLogger logger = new IrisPlusLogger();
            logger.emailLog(this);
        } else {
            billingHelper.mHelper.launchPurchaseFlow(this, PREMIUM_SKU, 10001, billingHelper.mPurchaseFinishedListener, "premium");
        }
    }

    public void onSectionAttached(int number) {
        try {
            mTitle = mNavigationDrawerFragment.getSelectedMenuItem().getItemName();
        } catch (Exception e) {
            mTitle = "Dashboard";
        }
    }

    @SuppressLint("InlinedApi")
	public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.iris, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	startActivity(new Intent(this, Preferences.class));
            return true;
        } else if(id == R.id.action_help) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().add(R.id.container, HelpFragment.newInstance(99)).addToBackStack(null).commit();
        } else if(id == R.id.action_voice) {
            if(billingHelper.premium || BuildConfig.DEBUG) {
                this.displaySpeechRecognizer();
            } else {
                billingHelper.mHelper.launchPurchaseFlow(this, PREMIUM_SKU, 10001, billingHelper.mPurchaseFinishedListener, "premium");
            }
        } else if(id == R.id.action_login) {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        } else if (id == R.id.action_buy) {
            if(!billingHelper.premium) {
                billingHelper.mHelper.launchPurchaseFlow(this, PREMIUM_SKU, 10001, billingHelper.mPurchaseFinishedListener, "premium");
            } else {
                Toast.makeText(this.getApplicationContext(), "You have already purchased a Premium version. Thank you for your support!", Toast.LENGTH_LONG).show();
            }
            return true;
        } else if(id == R.id.action_switch_hub) {
            try {
                FileInputStream fileInputStream = IrisPlus.getContext().openFileInput("irisplus-hubs-list.dat");
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                List<HubItem> hubs = (ArrayList<HubItem>) objectInputStream.readObject();
                objectInputStream.close();
                if(hubs.size() > 1) {
                    String currentHub = mSharedPrefs.getString(IrisPlusConstants.PREF_HUB_ID, "");
                    int newHubIndex = 0;
                    for (HubItem hub : hubs) {
                        if (hub.getId().equals(currentHub)) {
                            if (newHubIndex + 1 < hubs.size()) {
                                //Get next hub in the list if it exists
                                mSharedPrefs.edit().putString(IrisPlusConstants.PREF_HUB_ID, hubs.get(newHubIndex + 1).getId()).commit();
                                logger.log(IrisPlusConstants.LOG_INFO, "Switching to hub " + hubs.get(newHubIndex + 1).getHubName());
                                Toast.makeText(this.getApplicationContext(), "Switching to hub " + hubs.get(newHubIndex + 1).getHubName(), Toast.LENGTH_LONG).show();
                            } else {
                                //Go to beginning of the list
                                mSharedPrefs.edit().putString(IrisPlusConstants.PREF_HUB_ID, hubs.get(0).getId()).commit();
                                logger.log(IrisPlusConstants.LOG_INFO, "Switching to hub " + hubs.get(0).getHubName());
                                Toast.makeText(this.getApplicationContext(), "Switching to hub " + hubs.get(0).getHubName(), Toast.LENGTH_LONG).show();
                            }
                            if(mSharedPrefs.contains(IrisPlusConstants.PREF_USERNAME)) {
                                TaskHelper.execute(new DeviceViewTask(mNavigationDrawerFragment));
                            }
                            break;
                        }
                        newHubIndex++;
                    }
                    getFragmentManager().beginTransaction().replace(R.id.container, DashboardFragment.newInstance(0), "dashboard").commitAllowingStateLoss();
                } else {
                    Toast.makeText(this.getApplicationContext(), "You only have one hub on your account.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_ERROR, "Could not switch hubs. " + e);
                Toast.makeText(this.getApplicationContext(), "Could not switch hubs. Log back into Iris and try again.", Toast.LENGTH_LONG).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }
    
    void doBindService() {
		mIsBound = true;
	}

	void doUnbindService() {
		if (!mIsBound) {
			return;
		}
		mIsBound = false;
	}

    @Override
    protected void onResume() {
        super.onResume();
        IrisPlus.setCurrentActivity(this);
        this.checkPinSecurity();

        try {
            notificationHelper = new NotificationHelper(IrisPlus.getContext());
            notificationHelper.destroyNotificationWithSoundAndVibrate();
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not destroy notification on resume. " + e);
        }

        try {
            if(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false)) {
                debugDialog.show();
            }
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not load debug dialog on resume. " + e);
        }

        try {
            onNewIntent(getIntent());
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not open new intent on resume. " + e);
        }

        try {
            mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean useIftttForRules = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_IFTTT, false);

            if(useIftttForRules && (billingHelper.premium || BuildConfig.DEBUG) && !Settings.Secure.getString(this.getContentResolver(),"enabled_notification_listeners").contains(getApplicationContext().getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            }
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not open notification listener settings on resume. " + e);
        }
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }
    
    @Override
	protected void onDestroy() {
        clearReferences();
		super.onDestroy();
		try {
			doUnbindService();
		} catch (Throwable e) {}
	}

    @Override
    public void onNewIntent(Intent intent){
        Bundle extras = intent.getExtras();
        if(extras != null){
            try {
                if (extras.containsKey("menuFragment") && billingHelper != null && (billingHelper.premium || BuildConfig.DEBUG)) {
                    String menuFragment = extras.getString("menuFragment");
                    if (menuFragment != null) {
                        logger.log(IrisPlusConstants.LOG_INFO, "Menu fragment from notification is " + menuFragment);
                        FragmentManager fragmentManager = getFragmentManager();
                        if ("history".equalsIgnoreCase(menuFragment)) {
                            fragmentManager.beginTransaction().replace(R.id.container, HistoryFragment.newInstance(1)).commitAllowingStateLoss();
                            mTitle = "History";
                        } else if ("security".equalsIgnoreCase(menuFragment)) {
                            fragmentManager.beginTransaction().replace(R.id.container, SecurityFragment.newInstance(1)).commitAllowingStateLoss();
                            mTitle = "Security";
                        } else if ("devices".equalsIgnoreCase(menuFragment)) {
                            fragmentManager.beginTransaction().replace(R.id.container, DeviceFragment.newInstance(1, true)).commitAllowingStateLoss();
                            mTitle = "Devices";
                        }
                        this.restoreActionBar();
                        mNavigationDrawerFragment.updateMenuSelection(mTitle.toString());
                        menuFragment = null;
                        intent.putExtra("menuFragment", menuFragment);
                    }
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_ERROR, "Could not open proper screen from notification. " + e);
            }
        }
    }

    private void checkPinSecurity() {
        try {
            Context context = this.getApplicationContext();
            SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            String pin = mSharedPrefs.getString(IrisPlusConstants.PREF_PIN, "");
            boolean securityOnly = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_PIN_SECURITYONLY, false);

            if(!"".equals(pin) && (!securityOnly || ("security".equalsIgnoreCase(selectedMenuItem) || "locks".equalsIgnoreCase(selectedMenuItem)))) {
                String username = mSharedPrefs.getString(IrisPlusConstants.PREF_USERNAME, "");
                String password = mSharedPrefs.getString(IrisPlusConstants.PREF_PASSWORD, "");

                Double defaultDateTime = (double) (new Date().getTime()) - 1000 * 60 * 60 * 24 * 365;   //1 year in the past
                Double currDateTime = Double.parseDouble(String.valueOf((double) new Date().getTime()));
                Double pinDateTime = Double.parseDouble(mSharedPrefs.getString(IrisPlusConstants.PREF_PIN_ENTERED, String.valueOf(defaultDateTime)));

                pinEntered = currDateTime - pinDateTime <= 10000;

                if (!pinEntered && pin != null && !"".equals(pin) && username != null && !"".equals(username) && password != null && !"".equals(password)) {
                    Intent intent = new Intent(this, PinActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }
            }
        } catch (Exception e) {}
    }

    public CharSequence getmTitle() {
        return mTitle;
    }

    public void setmTitle(CharSequence mTitle) {
        this.mTitle = mTitle;
    }


    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getFragmentManager();
        if(fragmentManager.getBackStackEntryCount() == 0 && backButtonPressed == 0) {
            fragmentManager.beginTransaction().replace(R.id.container, DashboardFragment.newInstance(1)).commit();
            mNavigationDrawerFragment.updateMenuSelection("Dashboard");
            backButtonPressed++;
            Toast.makeText(this.getApplicationContext(), "Press the back button again to exit.", Toast.LENGTH_SHORT).show();
            notificationHelper = new NotificationHelper(this.getApplicationContext());
            notificationHelper.destroyNotification(0);
        } else if(fragmentManager.getBackStackEntryCount() == 0 && backButtonPressed > 0) {
            try {
                notificationHelper = new NotificationHelper(IrisPlus.getContext());
                notificationHelper.destroyNotification(0);
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_ERROR, "Could not destroy notification. " + e);
            }
            backButtonPressed = 0;
            finish();
        }  else {
            backButtonPressed = 0;
            super.onBackPressed();
        }
        //displaySpeechRecognizer();
        logger.log(IrisPlusConstants.LOG_INFO, "Back button pressed. " + fragmentManager.getBackStackEntryCount());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText
            VoiceCommandInterpreter voiceCommandInterpreter = new VoiceCommandInterpreter();
            voiceCommandInterpreter.processVoiceCommand(this.getApplicationContext(), spokenText);
            //super.onActivityResult(requestCode, resultCode, data);
        } else if (!billingHelper.mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    private void scheduleAutomationAlarms() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));

        if(!logger.isDebug()) {
            logger.deleteLog();
        }

        AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
        Intent i = new Intent(getApplicationContext(), AutomationBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), IrisPlusConstants.ALARM_AUTOMATION_SINGLE, i, 0);
        alarmManagerHelper.scheduleSingleAlarm(this.getApplicationContext(), pendingIntent);

        try {
            if(Build.VERSION.SDK_INT >= 23) {
                Intent intent = new Intent();
                String packageName = this.getPackageName();
                PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    this.startActivity(intent);
                }
            }
        } catch (ActivityNotFoundException e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not request whitelist dialog.");
        }
    }

    private void clearReferences(){
        Activity currActivity = IrisPlus.getCurrentActivity();
        if (currActivity != null && this.equals(currActivity)) {
            IrisPlus.setCurrentActivity(null);
        }

        try {
            notificationHelper = new NotificationHelper(IrisPlus.getContext());
            notificationHelper.destroyNotification(0);
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not destroy notification. " + e);
        }
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        ComponentName expectedComponentName = new ComponentName(context, accessibilityService);

        String enabledServicesSetting = Settings.Secure.getString(context.getContentResolver(),  Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null)
            return false;

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter(':');
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            String componentNameString = colonSplitter.next();
            ComponentName enabledService = ComponentName.unflattenFromString(componentNameString);

            if (enabledService != null && enabledService.equals(expectedComponentName))
                return true;
        }

        return false;
    }

    public void showWhatsNewDialog() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            final int versionCode = pInfo.versionCode;
            boolean showDialog = mSharedPrefs.getBoolean("showWhatsNewDialog" + versionCode, true);
            if(showDialog) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("What's New?");

                WebView wv = new WebView(this);
                wv.loadUrl("http://www.80degreeswest.com/apps/irisplus/whatsnew_v4.php");
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                });

                alert.setView(wv);
                alert.setNegativeButton("Don't Show Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mSharedPrefs.edit().putBoolean("showWhatsNewDialog" + versionCode, false).commit();
                        dialog.dismiss();
                    }
                });
                alert.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                alert.show();
            }
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not show what's new dialog. " + e);
        }
    }

    public void setBackButtonPressed(int p) {
        backButtonPressed = p;
    }
}
