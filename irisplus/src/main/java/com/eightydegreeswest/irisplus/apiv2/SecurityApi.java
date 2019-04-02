package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DeviceItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Iris API for the Security screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class SecurityApi extends IrisApi {

    public SecurityApi(Context context) {
        super(context);
    }

    /**
     * Get current alarm status
     * @return String name of alarm
     */
    public String getAlarmStatus() {
        String alarmStatus = null;
        try {
            String devices = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListDevices\",\"attributes\":{}}}");
            JSONObject jsonObj = new JSONObject(devices.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("devices");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                if ("KeyPad".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                    alarmStatus = jsonObj.getString("keypad:alarmMode");
                    break;
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get current alarm status");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alarmStatus;
    }

    public void setAlarm(String mode) {
        this.setAlarm(mode, false);
    }

    public void setAlarmBypass(String mode) {
        this.setAlarm(mode, true);
    }

    /**
     * Set alarm mode
     * @param mode (ON, OFF, PARTIAL)
     */
    public void setAlarm(String mode, boolean bypass) {
        try {
            mode = mode.toUpperCase().replace("night", "PARTIAL");
            if("OFF".equalsIgnoreCase(mode)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:subsecurity:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"subsecurity:Disarm\",\"attributes\":{}}}");
            } else if("ACKNOWLEDGE".equalsIgnoreCase(mode) || "RESET".equalsIgnoreCase(mode)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:subsecurity:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"subsecurity:Acknowledge\",\"attributes\":{}}}");
            } else if("PANIC".equalsIgnoreCase(mode)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:subsecurity:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"subsecurity:Panic\",\"attributes\":{\"silent\":false}}}");
            } else if("SILENTPANIC".equalsIgnoreCase(mode)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:subsecurity:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"subsecurity:Panic\",\"attributes\":{\"silent\":true}}}");
            } else if("PARTIAL".equalsIgnoreCase(mode)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:subsecurity:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"subsecurity:Arm\",\"attributes\":{\"mode\":\"" + mode.toUpperCase() + "\"}}}");
            } else if(bypass) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:subsecurity:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"subsecurity:ArmBypassed\",\"attributes\":{\"mode\":\"" + mode.toUpperCase() + "\"}}}");
            } else {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:subsecurity:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"subsecurity:Arm\",\"attributes\":{\"mode\":\"" + mode.toUpperCase() + "\"}}}");
            }

            logger.log(IrisPlusConstants.LOG_DEBUG, "Alarm mode changed to " + mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear current alarms
     */
    public void clearCurrentAlarms() {
        setAlarm("RESET");
    }
}
