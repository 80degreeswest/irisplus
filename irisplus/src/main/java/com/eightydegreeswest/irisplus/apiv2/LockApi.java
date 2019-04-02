package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.LockItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Lock screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class LockApi extends IrisApi {

    public LockApi(Context context) {
        super(context);
    }

    /**
     * Get all locks on the account
     * @return JSON representation of devices
     */
    public List<LockItem> getAllLocks() {
        List<LockItem> locks = new ArrayList<LockItem>();
        try {
            String locksList = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListDevices\",\"attributes\":{}}}");
            JSONObject jsonObj = new JSONObject(locksList.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("devices");

            for(int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                LockItem lockItem = new LockItem();
                lockItem.setId(jsonObj.getString("base:id"));
                lockItem.setLockName(jsonObj.getString("dev:name"));
                lockItem.setType(jsonObj.getString("dev:devtypehint"));
                if("Garage Door".equalsIgnoreCase(lockItem.getType())) {
                    lockItem.setState(jsonObj.getString("motdoor:doorstate"));  //CLOSED, OPENED
                    locks.add(lockItem);
                } else if("Lock".equalsIgnoreCase(lockItem.getType())) {
                    lockItem.setState(jsonObj.getString("doorlock:lockstate")); //LOCKED, UNLOCKED
                    lockItem.setBuzzIn(jsonObj.getBoolean("doorlock:supportsBuzzIn"));
                    locks.add(lockItem);
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all locks success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return locks;
    }

    /**
     * Set specific lock state
     * @param lockID (LOCKED, UNLOCKED, OPENED, CLOSED)
     */
    public void setLockState(String lockID, String state) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + lockID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{" + state + "}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set lock " + lockID + " to " + state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set lock to buzz in
     * @param lockID
     */
    public void setLockBuzzIn(String lockID) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"DRIV:dev:" + lockID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"doorlock:BuzzIn\",\"attributes\":{}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set lock " + lockID + " to buzz in");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
