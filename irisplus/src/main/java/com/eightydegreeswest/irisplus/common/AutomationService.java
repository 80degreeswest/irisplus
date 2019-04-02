package com.eightydegreeswest.irisplus.common;

/**
 * Created by Yuriy on 2/12/17.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import com.eightydegreeswest.irisplus.IrisActivity;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.tasks.AutomationTask;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.net.ssl.HttpsURLConnection;

public class AutomationService extends NotificationListenerService {

    public static AutomationService service;
    private static boolean isRunning = false;
    private static volatile PowerManager.WakeLock wakeLock;
    private SharedPreferences mSharedPrefs;
    private IrisPlusLogger logger = new IrisPlusLogger();
    final ReentrantReadWriteLock updateLock = new ReentrantReadWriteLock();
    public static WebSocket ws = null;
    private String iftttUrl = "https://maker.ifttt.com/trigger/EVENT_NAME/with/key/";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String iftttTitle = "IFTTT";
        try {
            if(sbn != null && sbn.getNotification() != null && sbn.getNotification().extras.get("android.text") != null) {
                String notificationTitle = sbn.getNotification().extras.get("android.title").toString();
                String commands = sbn.getNotification().extras.get("android.text").toString();
                //System.out.println("Title: " + notificationTitle + ", Commands: " + commands);
                if(iftttTitle.equalsIgnoreCase(notificationTitle) && commands != null && !"".equals(commands) && commands.contains("=")) {
                    this.executeIftttRules(commands);
                    this.cancelNotification(sbn.getKey());
                }
            } else {
                logger.log(IrisPlusConstants.LOG_ERROR, "Could not parse command from notification. " + sbn.toString());
            }
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Could not perform automation command. " + e);
        }

        AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
        Intent i = new Intent(getApplicationContext(), AutomationBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), IrisPlusConstants.ALARM_AUTOMATION_SINGLE, i, 0);
        alarmManagerHelper.scheduleSingleAlarm(this, pendingIntent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        AutomationService.service = this;
        isRunning = true;
        logger.log(IrisPlusConstants.LOG_INFO, "Automation service started");
        this.updateAutomationListener();
        this.connectToWebsocket();
    }

    public void updateAutomationListener() {
        if (wakeLock == null) {
            PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);

			/* we don't need the screen on */
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "irisplus");
            wakeLock.setReferenceCounted(true);
        }

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        updateLock.writeLock().lock();
        wakeLock.release();
        updateLock.writeLock().unlock();
        logger.setDebug(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_DEBUG, false));
        logger.log(IrisPlusConstants.LOG_INFO, "Automation service updated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        try {
            logger.log(IrisPlusConstants.LOG_INFO, "Stopped Automation service");
            startService(new Intent(this, AutomationService.class));
        }
        catch (Exception e) {}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int res = super.onStartCommand(intent, flags, startId);
        if(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_SERVICE_FOREGROUND, false)) {
            Intent notificationIntent = new Intent(this, IrisActivity.class);
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Iris+")
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build();
            startForeground(300, notification);
        }
        return START_STICKY;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private void executeIftttRules(String rulesTxt) {
        String executedRules = "";
        try {
            logger.log(IrisPlusConstants.LOG_INFO, "Using IFTTT to execute rules.");
            if(rulesTxt != null && !"".equals(rulesTxt) && rulesTxt.contains("=")) {
                rulesTxt = rulesTxt.replaceAll(", ", ",").replaceAll("\r", "").replaceAll("\n", "");
                String[] customRules = rulesTxt.split(",");
                int executed = 0;
                logger.log(IrisPlusConstants.LOG_INFO, "Found " + customRules.length + " custom IFTTT rules to execute.");

                for (int currExe = 0; currExe < customRules.length; currExe++) {
                    try {
                        String rule = customRules[currExe];
                        logger.log(IrisPlusConstants.LOG_INFO, "Parsing custom IFTTT rule: " + rule);
                        String[] parsedRule = rule.split("=");
                        if (parsedRule.length == 2) {
                            //Valid rule - continue
                            String deviceName = parsedRule[0];
                            String newStatus = parsedRule[1];
                            TaskHelper.execute(new AutomationTask(this, deviceName, newStatus, false));
                            executed++;
                            executedRules += "Set " + deviceName + " to " + newStatus + ".  ";
                        }
                    } catch (Exception e) {
                        logger.log(IrisPlusConstants.LOG_INFO, "Error while executing IFTTT rule: " + e.getMessage());
                    }
                }

                if (executed > 0) {
                    //Toast.makeText(this, "The following rules have ran: " + executedRules, Toast.LENGTH_LONG).show();
                    AlarmManagerHelper alarmManagerHelper = new AlarmManagerHelper();
                    Intent i = new Intent(this, WidgetUpdateBroadcast.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, IrisPlusConstants.ALARM_WIDGET_UPDATE_SINGLE, i, 0);
                    alarmManagerHelper.scheduleSingleAlarm(this, pendingIntent);

                    NotificationHelper notificationHelper = new NotificationHelper(this);
                    notificationHelper.createNotificationWithSoundAndVibrateAndIntent("Iris+ executed  " + executed + " custom rules.", "history");
                    logger.log(IrisPlusConstants.LOG_INFO, "Executed " + executed + " custom rules. " + customRules);
                }
            }
        } catch (Exception e) {
            logger.log(IrisPlusConstants.LOG_INFO, "error while executing IFTTT rule: " + e.getMessage());
        }
    }

    /**
     * With "Send ALL events to IFTTT as JSON String" Checked:
     *
     * 1. Add "Maker Webhooks" service to your IFTTT account.
     * 2. Copy the long key from the IFTTT Maker Webhooks service settings to Iris Plus "IFTTT Maker Key" setting.
     * 3. Create new applet:
     *      a. For IF part, use Maker Webhooks.
     *      b. For event name, use IRISPLUS (in all caps).
     *      c. For ELSE part, use any service you want. Note, that Value1 will contain the JSON object that was received from the Iris servers and will need to be parsed by whatever service you specify in the ELSE part.
     *
     * With "Send ALL events to IFTTT as JSON String" Unchecked:
     *
     * 1. Add "Maker Webhooks" service to your IFTTT account.
     * 2. Copy the long key from the IFTTT Maker Webhooks service settings to Iris Plus "IFTTT Maker Key" setting.
     * 3. Create new applet:
     *      a. For IF part, use Maker Webhooks.
     *      b. For event name, use Device Name:EVENT (where Device Name is the exact device name and EVENT is one of the following in all caps - POWER, STATE).
     *      c. For ELSE part, use any service you want. Note, that Value1 will be exact Device ID, Value 2 will be the Event (POWER, STATE), and Value 3 will be the value changed (new power value, new state, etc).
     *
     */
    private void connectToWebsocket() {
        final String url = "wss://bc.irisbylowes.com/websocket";
        final String iftttKey = mSharedPrefs.getString(IrisPlusConstants.PREF_IFTTT_KEY, "");
        try {
            logger.log(IrisPlusConstants.LOG_DEBUG, "Attempt to open web socket for Automation Service.");
            if(ws == null || !ws.isOpen()) {
                logger.log(IrisPlusConstants.LOG_DEBUG, "Opening web socket for Automation Service.");
                ws = new WebSocketFactory()
                        .createSocket(url)
                        .addHeader("Cookie", "irisAuthToken=" + mSharedPrefs.getString(IrisPlusConstants.PREF_TOKEN, ""))
                        .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                        .addListener(new WebSocketAdapter() {
                            // A text message arrived from the server.
                            public void onTextMessage(WebSocket websocket, String message) throws Exception {
                                //logger.log(IrisPlusConstants.LOG_DEBUG, "WS Message: " + message);
                                if(!"".equalsIgnoreCase(iftttKey) && message.contains("base:ValueChange")) {
                                    logger.log(IrisPlusConstants.LOG_DEBUG, "WS ValueChange Message: " + message);
                                    IrisPlusHelper.submitToIfttt(IrisPlus.getContext(), message);
                                }
                            }
                        })
                        .connectAsynchronously();
            }
        } catch(Exception e) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Websocket exception. " + e);
        }
    }
}
