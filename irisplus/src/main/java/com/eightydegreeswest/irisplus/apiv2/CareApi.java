package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Iris API for the Care screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class CareApi extends IrisApi {

    public CareApi(Context context) {
        super(context);
    }

    /**
     * Get care status
     *
     * @return JSON representation of care status
     */
    public HashMap<String, String> getCareStatus() {
        HashMap<String, String> careAlerts = new HashMap<String, String>();
        try {
            String careStr = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:subcare:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"subcare:ListBehaviors\",\"attributes\":{}}}");
            JSONObject jsonObj = null;
            JSONArray jsonArray = null;

            jsonObj = new JSONObject(careStr);
            careAlerts.put("state", jsonObj.getString("state"));
            careAlerts.put("alarmed", jsonObj.getString("alarmed"));
            careAlerts.put("alertsEnabled", jsonObj.getString("alertsEnabled"));

            jsonObj = new JSONObject(jsonObj.getString("lastTriggered"));
            careAlerts.put("time", jsonObj.getString("time"));

            jsonArray = jsonObj.getJSONArray("devices");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject device = new JSONObject(jsonArray.get(i).toString());
                if (careAlerts.containsKey("devices")) {
                    careAlerts.put("devices", careAlerts.get("devices") + "<br>" + device.getString("name"));
                } else {
                    careAlerts.put("devices", device.getString("name"));
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get care status success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return careAlerts;
    }

    /**
     * Set care alerts
     *
     * @param alert alert
     */
    public void setCareStatus(boolean alert) {
        try {
            //this.executeCommand(API_URL + "/users/" + "@USERNAME@" + "/widgets/care/alertsEnabled", "PUT", "enabled=" + alert);
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set care to " + alert);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
