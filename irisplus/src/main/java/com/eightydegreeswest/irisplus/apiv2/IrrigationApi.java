package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;

import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DeviceItem;
import com.eightydegreeswest.irisplus.model.IrrigationItem;
import com.eightydegreeswest.irisplus.model.IrrigationZoneItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Iris API for the Irrigation screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class IrrigationApi extends IrisApi {

    public IrrigationApi(Context context) {
        super(context);
    }

    /**
     * Get irrigation devices on the account
     *
     * @return JSON representation of irrigation devices
     */
    public List<IrrigationItem> getIrrigationList() {
        List<IrrigationItem> irrigations = new ArrayList<IrrigationItem>();
        try {
            String devices = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListDevices\",\"attributes\":{}}}");

            JSONObject jsonObj = new JSONObject(devices.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("devices");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                if("Irrigation".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                    logger.log(IrisPlusConstants.LOG_INFO, jsonObj.toString());
                    IrrigationItem irrigationItem = new IrrigationItem();
                    irrigationItem.setDeviceName(jsonObj.getString("dev:name"));
                    irrigationItem.setState(jsonObj.getString("irrcont:controllerState"));
                    irrigationItem.setId(jsonObj.getString("base:id"));
                    irrigationItem.setControl(null);    //TODO: manual or schedule
                    irrigationItem.setMode(null);
                    irrigationItem.setNext(null);   //TODO: next water time
                    irrigationItem.setOnOffState(null);
                    irrigationItem.setType(null);

                    //Get zones
                    List<IrrigationZoneItem> zones = new ArrayList<>();
                    for(int zoneNumber = 1; zoneNumber <= jsonObj.getInt("irrcont:numZones"); zoneNumber++) {
                        try {
                            IrrigationZoneItem zoneItem = new IrrigationZoneItem();
                            zoneItem.setActive(true);
                            zoneItem.setDefaultDuration(Integer.toString(jsonObj.getInt("irr:defaultDuration:z" + zoneNumber)));
                            zoneItem.setId(Integer.toString(zoneNumber));
                            zoneItem.setName(jsonObj.getString("irr:zonename:z" + zoneNumber));
                            zoneItem.setNumber(zoneNumber);
                            zoneItem.setStatus(jsonObj.getString("irr:zoneState:z" + zoneNumber));
                            Date next = new Date(jsonObj.getLong("irr:wateringStart:z" + zoneNumber));
                            if (irrigationItem.getNext() == null || new Date(Long.parseLong(irrigationItem.getNext())).after(next)) {
                                irrigationItem.setNext(Long.toString(next.getTime()));
                            }
                            zones.add(zoneItem);
                        } catch(Exception e) {
                            logger.log(IrisPlusConstants.LOG_ERROR, "Could not parse zone information for zone number " + zoneNumber);
                        }
                    }
                    irrigationItem.setZones(zones);
                    irrigations.add(irrigationItem);
                }
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get all irrigation devices success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return irrigations;
    }

    /**
     * Set irrigation control
     *
     * @param deviceID
     * @param control
     */
    public void setIrrigationControl(String deviceID, String control) {
        try {
            if("manual".equalsIgnoreCase(control)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"irrsched:DisableSchedule\",\"attributes\":{\"duration\":0}}}");
            } else if("schedule".equalsIgnoreCase(control)) {
                String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"irrsched:EnableSchedule\",\"attributes\":{}}}");
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set irrigation device " + deviceID + " control to " + control);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set irrigation delay
     *
     * @param deviceID
     * @param delay
     */
    public void setIrrigationDelay(String deviceID, String delay) {
        try {
            delay = delay.replace(" hours", "");
            String retVal = this.sendToWebsocket(API_URL, "{\"type\":\"base:SetAttributes\",\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"base:SetAttributes\",\"attributes\":{\"irrcont:rainDelay\":" + (Integer.parseInt(delay) * 60) + "}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set irrigation device " + deviceID + " delay to " + delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set irrigation to stop
     *
     * @param deviceID
     */
    public void setIrrigationStop(String deviceID) {
        try {
            String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:sublawnngarden:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"sublawnngarden:StopWatering\",\"attributes\":{\"controller\":\"DRIV:dev:" + deviceID + "\"}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set irrigation device " + deviceID + " to stop");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set irrigation to start
     *
     * @param deviceID
     * @param zone
     * @param duration
     */
    public void setIrrigationStart(String deviceID, String zone, String duration) {
        try {
            duration = duration.replace(" minutes", "");
            String retVal = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"DRIV:dev:" + deviceID + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"irrcont:WaterNow\",\"attributes\":{\"zonenum\":" + zone + ", \"duration\":" + duration + "}}}");
            logger.log(IrisPlusConstants.LOG_DEBUG, "Set irrigation device " + deviceID + " to start watering zone " + zone + " for " + duration + " minutes");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
