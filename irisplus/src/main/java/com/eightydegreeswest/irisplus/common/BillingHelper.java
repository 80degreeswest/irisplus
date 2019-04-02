package com.eightydegreeswest.irisplus.common;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.eightydegreeswest.irisplus.BuildConfig;
import com.eightydegreeswest.irisplus.automation.homestatus.HomeStatusCheckBroadcast;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.inappbilling.util.IabHelper;
import com.eightydegreeswest.irisplus.inappbilling.util.IabResult;
import com.eightydegreeswest.irisplus.inappbilling.util.Inventory;
import com.eightydegreeswest.irisplus.inappbilling.util.Purchase;
import com.eightydegreeswest.irisplus.wear.WearService;
import com.eightydegreeswest.irisplus.widgets.AlarmWidget;
import com.eightydegreeswest.irisplus.widgets.ControlWidget;
import com.eightydegreeswest.irisplus.widgets.LockWidget;
import com.eightydegreeswest.irisplus.widgets.SceneWidget;
import com.eightydegreeswest.irisplus.widgets.ThermostatWidget;
import com.eightydegreeswest.irisplus.widgets.VoiceControlWidget;

/**
 * Created by ybelenitsky on 3/19/2015.
 */
public class BillingHelper {

    private Activity mActivity;
    private String tag;
    private String base64EncodedPublicKey;
    public IabHelper mHelper;
    public boolean premium = false;
    private boolean developer = false;
    static String sku;
    private SharedPreferences mSharedPrefs;
    //private int delay = 5 * 60 * 1000;
    private int delay = 10000;


    public BillingHelper(Activity activity, String sku, String base64EncodedPublicKey, String tag) {
        mActivity = activity;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
        this.tag = tag;
        this.base64EncodedPublicKey = base64EncodedPublicKey;
        BillingHelper.sku = sku;

        try {
            mHelper = new IabHelper(mActivity, base64EncodedPublicKey);
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        complain("In-app Billing setup failed: " + result);
                    }
                    if (mHelper == null) return;
                    complain("Setup successful. Querying inventory.");
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                }
            });
        } catch (Exception e) {
            complain("Could not setup Google billling. Amazon device? " + e);
            premium = false;
            updatePremiumContent();
        }
    }

    public IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (mHelper == null) return;
            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }
            Purchase purchase = inventory.getPurchase(sku);
            premium = (purchase != null && verifyDeveloperPayload(purchase));
            updatePremiumContent();

            //mHelper.consumeAsync(inventory.getPurchase(no_ads_sku), mConsumeFinishedListener);
        }
    };

    public IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                complain("Purchase failed.");
                return;
            } else if (purchase.getSku().equals(sku)) {
                complain("User already purchased Premium version.");
                consumeItem();
            }

        }
    };

    public void consumeItem() {
        complain("Consuming item.");
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    public IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener  = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                complain("Inventory received failure.");
            } else {
                complain("Doing the consuming.");
                //mHelper.consumeAsync(inventory.getPurchase(NO_ADS_SKU), mConsumeFinishedListener);
                premium = true;
                updatePremiumContent();
            }
        }
    };

    public IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                complain("Consumed successful.");
                premium = true;
                updatePremiumContent();
            }
        }
    };

    public boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    private void complain(String message) {
        Log.e(tag, message);
    }

    public void destroy() {
        complain("Destroying billing.");
        if (mHelper != null) {
            mHelper.dispose();
        }
        mHelper = null;
    }

    private void updatePremiumContent() {
        PackageManager pm = mActivity.getPackageManager();
        if (premium || BuildConfig.DEBUG) {
            complain("Setup premium content.");
            mSharedPrefs.edit().putBoolean(IrisPlusConstants.PREF_PREMIUM_LOCK, true).commit();
            ComponentName receiver = new ComponentName(mActivity, ControlWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, ThermostatWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, LockWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, SceneWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, AlarmWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, WearService.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, AutomationService.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, VoiceControlWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, AutomationBroadcast.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, HomeStatusCheckBroadcast.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

            AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
            Intent i = new Intent(mActivity.getApplicationContext(), WidgetUpdateBroadcast.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mActivity.getApplicationContext(), IrisPlusConstants.ALARM_WIDGET_UPDATE_SINGLE, i, 0);
            alarmManagerHelper.scheduleSingleAlarm(mActivity.getApplicationContext(), pendingIntent, delay);
        } else {
            complain("Setup basic content.");
            mSharedPrefs.edit().putBoolean(IrisPlusConstants.PREF_PREMIUM_LOCK, false).commit();
            ComponentName receiver = new ComponentName(mActivity, ControlWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, ThermostatWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, LockWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, SceneWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, AlarmWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, WearService.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, AutomationService.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, VoiceControlWidget.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, AutomationBroadcast.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

            receiver = new ComponentName(mActivity, HomeStatusCheckBroadcast.class);
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }
}
