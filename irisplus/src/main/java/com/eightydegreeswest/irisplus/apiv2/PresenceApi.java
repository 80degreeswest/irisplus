package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.PresenceItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Presence screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class PresenceApi extends IrisApi {

    public PresenceApi(Context context) {
        super(context);
    }

    /**
     * Get all keyfobs on the account
     * @return JSON representation of keyfobs
     */
    public List<PresenceItem> getAllKeyfobs() {
        List<PresenceItem> keyfobs = new ArrayList<PresenceItem>();
        try {
            String devices = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListDevices\",\"attributes\":{}}}");
            JSONObject jsonObj = new JSONObject(devices.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("devices");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                if("KeyFob".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                    PresenceItem presenceItem = new PresenceItem();
                    presenceItem.setId(jsonObj.getString("base:id"));
                    presenceItem.setName(jsonObj.getString("dev:name"));
                    presenceItem.setState(jsonObj.getString("pres:presence"));
                    presenceItem.setLastChanged(jsonObj.getLong("pres:presencechanged"));

                    keyfobs.add(presenceItem);
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all keyfobs success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyfobs;
    }

    /**
     * Get number of keyfobs at home
     * @return JSON representation of keyfobs
     */
    public int getAllKeyfobsAtHome() {
        int keyfobsAtHome = 0;
        try {
            List<PresenceItem> keyfobs = this.getAllKeyfobs();
            for(PresenceItem keyfob : keyfobs) {
                if("PRESENT".equalsIgnoreCase(keyfob.getState())) {
                    keyfobsAtHome++;
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all keyfobs at home success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keyfobsAtHome;
    }
}
