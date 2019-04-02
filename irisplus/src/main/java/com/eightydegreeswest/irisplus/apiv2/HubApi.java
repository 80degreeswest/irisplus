package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.HubItem;

import org.json.JSONObject;

import java.util.Date;

/**
 * Iris API for the Care screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class HubApi extends IrisApi {

    public HubApi(Context context) {
        super(context);
    }

    /**
     * Get hub details
     * @return JSON representation of a hub
     */
    public HubItem getHubDetails() {
        HubItem hubItem = null;
        try {
            String hubDetails =  this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:GetHub\",\"attributes\":{}}}");
            hubItem = new HubItem();
            JSONObject jsonObj = new JSONObject(hubDetails.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            jsonObj = new JSONObject(jsonObj.getString("hub"));

            try {
                hubItem.setState(jsonObj.getString("hub:state"));
                hubItem.setHubName(jsonObj.getString("hub:name"));
                hubItem.setId(jsonObj.getString("base:id"));
                hubItem.setModel(jsonObj.getString("hub:model"));
                hubItem.setVersion(jsonObj.getString("hubadv:osver"));
                hubItem.setMacAddress(jsonObj.getString("hubadv:mac"));
                hubItem.setPlatformVersion(jsonObj.getString("hubadv:agentver"));
                hubItem.setPowerSource(jsonObj.getString("hubpow:source"));
                hubItem.setBattery(jsonObj.getString("hubpow:Battery"));
                hubItem.setLocalIp(jsonObj.getString("hubnet:ip"));
                hubItem.setLastZwaveRebuild(new Date(jsonObj.getLong("hubzwave:healLastFinish")));
                hubItem.setZwaveRebuildRecommended(jsonObj.getString("hubzwave:healRecommended") == "true" ? "Yes" : "No");
                hubItem.setExternalIp(jsonObj.getString("hubnet:externalip"));
                hubItem.setLastRestartTime(new Date(jsonObj.getLong("hubadv:lastRestartTime")));
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get hub details success");

            String hubAdvancedDetails = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"" + jsonObj.getString("base:address") + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"hub:GetConfig\",\"attributes\":{}}}");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hubItem;
    }

    /**
     * Reboot hub
     */
    public void rebootHub() {
        try {
            //this.executeCommand(API_URL + "/users/" + "@USERNAME@" + "/hubs/only/power", "PUT", "power=REBOOT");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Rebooted hub");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add device
     */
    public void addDevice(String status) {
        try {
            //this.executeCommand(API_URL + "/users/" + "@USERNAME@" + "/hubs/only/acquisition", "PUT", "mode=" + status);
            logger.log(IrisPlusConstants.LOG_DEBUG, "Upgraded hub");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get hub configuration
     * @return xml configuration of the hub
     */
    public String getHubConfig() {
        String ret = null;
        try {
            //ret = this.executeCommand(API_URL + "/users/" + "@USERNAME@" + "/hubs/@HUBID@/config.xml", "GET", "");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get hub config success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
