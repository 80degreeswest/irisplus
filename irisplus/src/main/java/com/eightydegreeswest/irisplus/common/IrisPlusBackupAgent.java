package com.eightydegreeswest.irisplus.common;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Created by Yuriy on 1/20/16.
 */
public class IrisPlusBackupAgent extends BackupAgentHelper {

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, this.getApplicationContext().getPackageName() + "_preferences");
        addHelper("com.eightydegreeswest.irisplus", helper);
    }
}
