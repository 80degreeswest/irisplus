package com.eightydegreeswest.irisplus.common;

import android.app.PendingIntent;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.eightydegreeswest.irisplus.R;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

public class Preferences extends PreferenceActivity {

	private SharedPreferences mSharedPrefs;
	private IrisPlusLogger logger = new IrisPlusLogger();

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

	}

	@Override
	protected void onResume() {
		super.onResume();
		this.scheduleAutomationAlarms();
		this.requestBackup();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.scheduleAutomationAlarms();
		this.requestBackup();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		this.scheduleAutomationAlarms();
		this.requestBackup();
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
		alarmManagerHelper.scheduleSingleAlarm(this, pendingIntent);

		//boolean useIftttForRules = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_IFTTT, false);

		//if(useIftttForRules && !isAccessibilityServiceEnabled(this, AutomationService.class)) {
		//	Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
		//	startActivityForResult(intent, 0);
		//}
	}

	public void requestBackup() {
		BackupManager bm = new BackupManager(this);
		bm.dataChanged();
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
}
