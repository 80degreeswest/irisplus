package com.eightydegreeswest.irisplus.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Yuriy on 1/8/18.
 */

public class IrisPlusHelper {

    private static IrisPlusLogger logger = new IrisPlusLogger();

    public static void submitToIfttt(Context context, String message) {
        SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String iftttKey = mSharedPrefs.getString(IrisPlusConstants.PREF_IFTTT_KEY, "");
        final String iftttUrl = "https://maker.ifttt.com/trigger/EVENT_NAME/with/key/";
        if(mSharedPrefs.getBoolean(IrisPlusConstants.PREF_IFTTT_SEND_ALL, false)) {
            //Send all as JSON String
            submitViaRestService(iftttUrl.replace("EVENT_NAME", "irisplus") + iftttKey, message, "", "");
        } else {
            try {
                String deviceId = "";
                String deviceName = "";
                JSONObject jsonObj = new JSONObject(message);
                JSONObject headers = jsonObj.getJSONObject("headers");
                deviceId = headers.getString("source");
                JSONObject payload = jsonObj.getJSONObject("payload");
                payload = payload.getJSONObject("attributes");
                deviceName = mSharedPrefs.getString(deviceId, "IrisDevice");
                if(mSharedPrefs.getBoolean(deviceName, false)) {
                    if (message.contains(IrisPlusConstants.ATTR_POWER_INSTANT)) {
                        submitViaRestService(iftttUrl.replace("EVENT_NAME", deviceName.toLowerCase() + "_power") + iftttKey, deviceId, "POWER", Integer.toString(payload.getInt(IrisPlusConstants.ATTR_POWER_INSTANT)));
                        //submitViaRestService(iftttUrl.replace("EVENT_NAME", deviceName.toLowerCase() + "_power_" + Integer.toString(payload.getInt(IrisPlusConstants.ATTR_POWER_INSTANT))) + iftttKey, deviceId, "POWER", Integer.toString(payload.getInt(IrisPlusConstants.ATTR_POWER_INSTANT)));
                    }
                    if (message.contains(IrisPlusConstants.ATTR_POWER_CUMULATIVE)) {
                        submitViaRestService(iftttUrl.replace("EVENT_NAME", deviceName.toLowerCase() + "_power") + iftttKey, deviceId, "POWER", Integer.toString(payload.getInt(IrisPlusConstants.ATTR_POWER_CUMULATIVE)));
                        //submitViaRestService(iftttUrl.replace("EVENT_NAME", deviceName.toLowerCase() + "_power_" + Integer.toString(payload.getInt(IrisPlusConstants.ATTR_POWER_CUMULATIVE))) + iftttKey, deviceId, "POWER", Integer.toString(payload.getInt(IrisPlusConstants.ATTR_POWER_CUMULATIVE)));
                    } else {
                        String newState = IrisPlusHelper.getDeviceState(payload);
                        if (newState != null) {
                            try {
                                final String mDeviceName = deviceName;
                                final String mNewState = newState;
                                IrisPlus.getCurrentActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(IrisPlus.getCurrentActivity(), "Sending to IFTTT: " + mDeviceName.toLowerCase() + "_state_" + mNewState.toLowerCase(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //submitViaRestService(iftttUrl.replace("EVENT_NAME", deviceName.toLowerCase() + "_state") + iftttKey, deviceId, "STATE", newState);
                            submitViaRestService(iftttUrl.replace("EVENT_NAME", deviceName.toLowerCase() + "_state_" + newState.toLowerCase()) + iftttKey, deviceId, "STATE", newState);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void submitViaRestService(String url, String var1, String var2, String var3) {
        //logger.log(IrisPlusConstants.LOG_DEBUG, "IFTTT Request URL: " + url);
        HttpsURLConnection conn = null;
        try {
            URL obj = new URL(url);
            conn = (HttpsURLConnection) obj.openConnection();
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(60000);
            conn.connect();

            BufferedReader rd;
            StringBuilder sb;
            String line;

            // Send post request
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes("value1=" + var1 + "&value2=" + var2 + "&value3=" + var3);
            wr.flush();
            wr.close();

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            sb = new StringBuilder();

            while ((line = rd.readLine()) != null) {
                sb.append(line).append('\n');
            }

            logger.log(IrisPlusConstants.LOG_DEBUG, "WS request successful.");
        } catch(IOException ioe) {
            logger.log(IrisPlusConstants.LOG_ERROR, "Timeout during WS Request.");
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }
    }

    public static String getDeviceState(JSONObject payload) {
        String newState = null;
        try {
            newState = payload.getString("cont:contact");
        } catch (Exception e) { }
        try {
            newState = payload.getString("mot:motion");
        } catch (Exception e) { }
        try {
            newState = payload.getString("leakh2o:state");
        } catch (Exception e) { }
        try {
            newState = payload.getString("but:state");
        } catch (Exception e) { }
        try {
            newState = payload.getString("doorlock:lockstate");
        } catch (Exception e) { }
        try {
            newState = payload.getString("motdoor:doorstate");
        } catch (Exception e) { }
        try {
            newState = payload.getString("pres:presence");
        } catch (Exception e) { }
        try {
            newState = payload.getString("irrcont:controllerState");
        } catch (Exception e) { }
        try {
            newState = payload.getString("keypad:alarmState");
        } catch (Exception e) { }
        try {
            newState = payload.getString("alert:state");
        } catch (Exception e) { }
        try {
            newState = payload.getString("co:co");
        } catch (Exception e) { }
        try {
            newState = payload.getString("therm:hvacmode");
        } catch (Exception e) { }
        try {
            newState = payload.getString("glass:break");
        } catch (Exception e) { }
        try {
            newState = payload.getString("tilt:tiltstate");
        } catch (Exception e) { }
        try {
            newState = payload.getString("vent:ventstate");
        } catch (Exception e) { }
        try {
            newState = payload.getString("valv:valvestate");
        } catch (Exception e) { }
        try {
            newState = payload.getString("spaceheater:heatstate");
        } catch (Exception e) { }
        try {
            newState = payload.getString("somfyv1:currentstate");
        } catch (Exception e) { }
        try {
            newState = payload.getString("swit:state");
        } catch (Exception e) { }
        return newState;
    }
}
