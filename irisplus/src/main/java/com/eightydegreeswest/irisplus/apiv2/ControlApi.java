package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.ControlItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Control screen
 * Created by ybelenitsky on 2/14/2015.
 */

@SuppressWarnings("unused")
public class ControlApi extends IrisApi {

    public ControlApi(Context context) {
        super(context);
    }

    /**
     * Set a specific device to on/off
     * @param deviceID
     * @param state
     */
    public void setDeviceState(String deviceID, String state) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{\"swit:state\":\"" + state.toUpperCase() + "\"}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set device " + deviceID + " to " + state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a specific blind to up, down, favorite
     * @param deviceID
     * @param state
     */
    public void setBlindState(String deviceID, String state) {
        try {
            if("open".equalsIgnoreCase(state)) {
                state = "Open";
            } else if("closed".equalsIgnoreCase(state)) {
                state = "Closed";
            } else if("favorite".equalsIgnoreCase(state)) {
                state = "Favorite";
            }
            //String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{\"somfyv1:currentstate\":\"" + state + "\"}}}");
            String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"somfyv1:GoTo" + state + "\",\"attributes\":{}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set blind " + deviceID + " to " + state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a specific device intensity
     * @param deviceID
     * @param intensity
     */
    public void setDeviceIntensity(String deviceID, int intensity) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{\"dim:brightness\":" + intensity + "}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set device " + deviceID + " intensity to " + String.valueOf(intensity));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a specific fan speed
     * @param deviceID
     * @param speed
     */
    public void setFanSpeed(String deviceID, String speed) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{\"fan:speed\":" + speed + "}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set fan " + deviceID + " speed to " + String.valueOf(speed));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all switches and smartplugs on the account
     * @return JSON representation of devices
     */
    public List<ControlItem> getAllControls() {
        List<ControlItem> controls = new ArrayList<ControlItem>();
        try {
            String devices = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListDevices\",\"attributes\":{}}}");
            JSONObject jsonObj = new JSONObject(devices.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("devices");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                ControlItem controlItem = new ControlItem();
                controlItem.setId(jsonObj.getString("base:id"));
                controlItem.setControlName(jsonObj.getString("dev:name"));
                controlItem.setType(jsonObj.getString("dev:devtypehint"));

                if("Switch".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                    controlItem.setState(jsonObj.getString("swit:state"));
                    controlItem.setStatus(jsonObj.getString("swit:state"));
                } else if("Fan Control".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                    controlItem.setState(jsonObj.getString("swit:state"));
                    controlItem.setStatus(jsonObj.getString("swit:state"));
                    controlItem.setHasSpeed(true);
                    controlItem.setSpeed(jsonObj.getString("fan:speed"));
                } else if("Dimmer".equalsIgnoreCase(jsonObj.getString("dev:devtypehint")) || "Light".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                    controlItem.setState(jsonObj.getString("swit:state"));
                    controlItem.setStatus(jsonObj.getString("swit:state"));
                    controlItem.setDimmer(true);
                    controlItem.setIntensity(jsonObj.getInt("dim:brightness"));
                } else if("SomfyV1Blind".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                    controlItem.setState(jsonObj.getString("somfyv1:currentstate"));
                    controlItem.setStatus(jsonObj.getString("somfyv1:currentstate"));
                    controlItem.setShade(true);
                }

                if("Switch".equalsIgnoreCase(controlItem.getType()) || "Light".equalsIgnoreCase(controlItem.getType()) || "Fan Control".equalsIgnoreCase(controlItem.getType()) || "Dimmer".equalsIgnoreCase(controlItem.getType()) || "SomfyV1Blind".equalsIgnoreCase(controlItem.getType())) {
                    controls.add(controlItem);
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all smartplugs success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return controls;
    }
}
