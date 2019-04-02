package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.eightydegreeswest.irisplus.common.IrisPlusHelper;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DeviceItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Device screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class DeviceApi extends IrisApi {

    Context mContext = null;

    public DeviceApi(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * Get home status
     * @return JSON representation of devices
     */
    public List<DeviceItem> getHomeStatus() {
        List<DeviceItem> irisDevices = new ArrayList<DeviceItem>();
        try {
            String devices = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListDevices\",\"attributes\":{}}}");
            JSONObject jsonObj = new JSONObject(devices.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("devices");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                DeviceItem deviceItem = new DeviceItem();
                deviceItem.setDeviceName(jsonObj.getString("dev:name"));
                try {
                    deviceItem.setSignal(Integer.toString(jsonObj.getInt("devconn:signal")));
                } catch (Exception e) { }
                deviceItem.setType(jsonObj.getString("dev:model"));
                deviceItem.setDeviceTypeHint(jsonObj.getString("dev:devtypehint"));
                deviceItem.setStatus(jsonObj.getString("devconn:state"));
                deviceItem.setState(IrisPlusHelper.getDeviceState(jsonObj));
                try {
                    deviceItem.setBatteryLevel(jsonObj.getString("devpow:battery"));
                    deviceItem.setBatteryPercentage(jsonObj.getString("devpow:battery"));
                } catch (Exception e) { }
                try {
                    deviceItem.setTemperature(jsonObj.getString("temp:temperature"));
                } catch (Exception e) { }
                deviceItem.setId(jsonObj.getString("base:id"));

                //For smartplugs, get status from the hashmap we loaded earlier
                if("SmartPlug".equalsIgnoreCase(deviceItem.getType())) {
                    deviceItem.setPower(jsonObj.getString("pow:instantaneous"));
                }

                if(deviceItem.getTemperature() != null && !"null".equalsIgnoreCase(deviceItem.getTemperature())) {
                    //Convert from C to F
                    try {
                        double f = Double.parseDouble(deviceItem.getTemperature()) * 1.8 + 32;
                        deviceItem.setTemperature(Integer.toString((int) Math.round(f)));
                    } catch (Exception e) {
                        deviceItem.setTemperature("n/a");
                    }
                }

                if(deviceItem.getBatteryLevel() == null || "null".equalsIgnoreCase(deviceItem.getBatteryLevel())) {
                    deviceItem.setBatteryLevel("AC");
                }
                if(deviceItem.getBatteryPercentage() == null || "null".equalsIgnoreCase(deviceItem.getBatteryPercentage())) {
                    deviceItem.setBatteryPercentage("AC");
                }
                if(deviceItem.getTemperature() == null || "null".equalsIgnoreCase(deviceItem.getTemperature())) {
                    deviceItem.setTemperature("n/a");
                }
                if(deviceItem.getSignal() == null || "null".equalsIgnoreCase(deviceItem.getTemperature())) {
                    deviceItem.setSignal("n/a");
                }
                irisDevices.add(deviceItem);

                SharedPreferences mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                mSharedPrefs.edit().putString(jsonObj.getString("base:address"), deviceItem.getDeviceName()).commit();
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all devices success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return irisDevices;
    }
}
