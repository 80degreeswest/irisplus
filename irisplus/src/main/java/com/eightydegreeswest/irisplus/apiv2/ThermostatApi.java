package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DeviceItem;
import com.eightydegreeswest.irisplus.model.ThermostatDetailsItem;
import com.eightydegreeswest.irisplus.model.ThermostatItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Thermostat screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class ThermostatApi extends IrisApi {

    public ThermostatApi(Context context) {
        super(context);
    }

    /**
     * Get details for a given thermostat
     * @param thermostatID
     * @return JSON representation of thermostat details
     */
    public ThermostatDetailsItem getThermostatDetails(String thermostatID) {
        ThermostatDetailsItem thermostatDetailsItem = new ThermostatDetailsItem();
        List<ThermostatItem> thermostats = new ArrayList<ThermostatItem>();
        try {
            String devices = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListDevices\",\"attributes\":{}}}");
            JSONObject jsonObj = new JSONObject(devices.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("devices");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = jsonArray.getJSONObject(i);
                if ("Thermostat".equalsIgnoreCase(jsonObj.getString("dev:devtypehint")) || "TCCThermostat".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                    ThermostatItem thermostat = new ThermostatItem();
                    thermostat.setId(jsonObj.getString("base:id"));
                    thermostat.setName(jsonObj.getString("dev:name"));
                    thermostats.add(thermostat);

                    if((thermostatID == null || "".equals(thermostatID) && thermostats.size() == 1) || (thermostatID != null && !"".equals(thermostatID) && thermostatID.equals(thermostat.getId()))) {
                        thermostatDetailsItem.setCurrentTemperature(Integer.toString((int) Math.round(jsonObj.getDouble("temp:temperature") * 1.8 + 32)));
                        thermostatDetailsItem.setTargetTemperature("");
                        thermostatDetailsItem.setHeatTargetTemperature(Integer.toString((int) Math.round(jsonObj.getDouble("therm:heatsetpoint") * 1.8 + 32)));
                        thermostatDetailsItem.setCoolTargetTemperature(Integer.toString((int) Math.round(jsonObj.getDouble("therm:coolsetpoint") * 1.8 + 32)));
                        thermostatDetailsItem.setMode(jsonObj.getString("therm:hvacmode"));
                        try {
                            thermostatDetailsItem.setHumidity(jsonObj.getString("humid:humidity") + "%");
                        } catch (Exception e) {
                            thermostatDetailsItem.setHumidity("N/A");
                        }
                        try {
                            thermostatDetailsItem.setFilterStatus(Integer.toString(jsonObj.getInt("therm:runtimesincefilterchange")));
                        } catch (Exception e) {
                            thermostatDetailsItem.setFilterStatus("0");
                        }
                    }
                }
            }
            thermostatDetailsItem.setThermostats(thermostats);
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get thermostat details success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return thermostatDetailsItem;
    }

    /**
     * Set a specific temperature
     * @param thermostatID
     * @param temperature
     */
    public void setTemperature(String thermostatID, String temperature, String mode) {
        try {
            double temp = (Double.parseDouble(temperature) - 32) * 5 / 9;
            if("COOL".equalsIgnoreCase(mode)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + thermostatID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{\"therm:coolsetpoint\":" + temp + "}}}");
            } else if("HEAT".equalsIgnoreCase(mode)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + thermostatID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{\"therm:heatsetpoint\":" + temp + "}}}");
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set temperature to " + temperature + " F");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set a specific thermostat mode
     * @param thermostatID
     * @param mode
     */
    public void setThermostatMode(String thermostatID, String mode) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + thermostatID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{\"therm:hvacmode\":\"" + mode.toUpperCase() + "\"}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set mode to " + mode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset furnace filter
     */
    public void resetThermostatFilter(String thermostatID) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"DRIV:dev:" + thermostatID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"therm:changeFilter\",\"attributes\":{}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Reset filter date");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
