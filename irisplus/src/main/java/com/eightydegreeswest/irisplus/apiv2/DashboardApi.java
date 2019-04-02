package com.eightydegreeswest.irisplus.apiv2;

import android.content.Context;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.DashboardItem;
import com.eightydegreeswest.irisplus.model.HubItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Iris API for the Dashboard screen
 * Created by ybelenitsky on 2/14/2015.
 */
public class DashboardApi extends IrisApi {

    private static final String LAST_FILTER_RESET = "lastFilterReset";

    public DashboardApi(Context context) {
        super(context);
    }

    /**
     * Get dashboard
     * @return JSON representation of dashboard
     */
    public List<DashboardItem> getDashboard() {
        List<DashboardItem> dashboards = new ArrayList<DashboardItem>();
        try {
            String devices = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:ListDevices\",\"attributes\":{}}}");
            JSONObject jsonObj = new JSONObject(devices.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("devices");
            int devicesOn = 0;
            int devicesOff = 0;
            int doorsLocked = 0;
            int doorsUnlocked = 0;
            int sensorsOpen = 0;
            int sensorsClosed = 0;
            int devicesOffline = 0;
            int devicesLowBattery = 0;
            String thermostatMode = null;
            int thermostatLow = 0;
            int thermostatHigh = 0;
            int thermostatCurrent = 0;
            String thermostatHumidity = null;
            String alarmStatus = null;
            int peopleHome = 0;
            int peopleAway = 0;
            int devicesAtHome = 0;
            int devicesNoSignal = 0;
            int filterRuntime = 0;
            int lastFilterReset = mSharedPrefs.getInt(LAST_FILTER_RESET, 0);
            boolean useLocalFilterRuntime = mSharedPrefs.getBoolean(IrisPlusConstants.PREF_LOCAL_FILTER_RUNTIME, true);
            boolean hubUpgraded = false;
            boolean platformUpgraded = false;
            int energy = 0;
            boolean energyDevices = false;
            String hubState = "";
            DashboardItem dashboardItem;

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObj = new JSONObject(jsonArray.get(i).toString());
                try {
                    if("ON".equalsIgnoreCase(jsonObj.getString("swit:state"))) {
                        devicesOn++;
                        if("SmartPlug".equalsIgnoreCase(jsonObj.getString("dev:model"))) {
                            energyDevices = true;
                            try {
                                energy += jsonObj.getInt("pow:instantaneous");
                            } catch (Exception e) {
                                energy += Integer.parseInt(jsonObj.getString("pow:instantaneous"));
                            }
                        }
                    } else if("OFF".equalsIgnoreCase(jsonObj.getString("swit:state"))) {
                        devicesOff++;
                    }
                } catch(Exception e) {}

                try {
                    if("LOCKED".equalsIgnoreCase(jsonObj.getString("doorlock:lockstate")) || "LOCKING".equalsIgnoreCase(jsonObj.getString("doorlock:lockstate"))) {
                        doorsLocked++;
                    } else if("UNLOCKED".equalsIgnoreCase(jsonObj.getString("doorlock:lockstate"))) {
                        doorsUnlocked++;
                    }
                } catch(Exception e) {}

                try {
                    if("CLOSED".equalsIgnoreCase(jsonObj.getString("motdoor:doorstate")) || "CLOSING".equalsIgnoreCase(jsonObj.getString("motdoor:doorstate"))) {
                        doorsLocked++;
                    } else if("OPEN".equalsIgnoreCase(jsonObj.getString("motdoor:doorstate"))) {
                        doorsUnlocked++;
                    }
                } catch(Exception e) {}

                try {
                    if("CLOSED".equalsIgnoreCase(jsonObj.getString("cont:contact"))) {
                        sensorsClosed++;
                    } else if("OPENED".equalsIgnoreCase(jsonObj.getString("cont:contact"))) {
                        sensorsOpen++;
                    }
                } catch(Exception e) {}

                try {
                    if("PRESENT".equalsIgnoreCase(jsonObj.getString("pres:presence"))) {
                        peopleHome++;
                    } else if("ABSENT".equalsIgnoreCase(jsonObj.getString("pres:presence"))) {
                        peopleAway++;
                    }
                } catch(Exception e) {}

                try {
                    if(!"ONLINE".equalsIgnoreCase(jsonObj.getString("devconn:state"))) {
                        if(!"Keyfob".equalsIgnoreCase(jsonObj.getString("dev:devtypehint"))) {
                            //Don't alert about keyfobs offline since this is expected behaviour
                            devicesOffline++;
                        }
                    }
                } catch(Exception e) {}

                try {
                    if(jsonObj.getInt("devconn:signal") == 0 && mSharedPrefs.getBoolean(IrisPlusConstants.PREF_HOME_STATUS_CHECK_SIGNAL, true)) {
                        devicesNoSignal++;
                    }
                } catch(Exception e) {}

                try {
                    alarmStatus = jsonObj.getString("keypad:alarmMode");
                } catch (Exception e) { }

                try {
                    thermostatMode = jsonObj.getString("therm:hvacmode");
                    try {
                        thermostatHumidity = jsonObj.getString("humid:humidity") + "%";
                    } catch (Exception e) {
                        thermostatHumidity = "N/A";
                    }
                    thermostatCurrent = (int) Math.round(jsonObj.getDouble("temp:temperature") * 1.8 + 32);
                    thermostatLow = (int) Math.round(jsonObj.getDouble("therm:heatsetpoint") * 1.8 + 32);
                    thermostatHigh = (int) Math.round(jsonObj.getDouble("therm:coolsetpoint") * 1.8 + 32);
                    filterRuntime = jsonObj.getInt("therm:runtimesincefilterchange");
                    if(useLocalFilterRuntime) {
                        filterRuntime -= lastFilterReset;
                    }
                } catch (Exception e) { }

                try {
                    if(Integer.parseInt(jsonObj.getString("devpow:battery")) < 30) {
                        devicesLowBattery++;
                    }
                } catch (Exception e) { }
            }

            HubItem hubItem = this.getHubDetails();
            if(hubItem != null) {
                String lastHubVersion = mSharedPrefs.getString("lastHubVersion", "");
                String lastPlatformVersion = mSharedPrefs.getString("lastPlatformVersion", "");
                if(!"".equalsIgnoreCase(lastHubVersion) && !lastHubVersion.equals(hubItem.getVersion())) {
                    hubUpgraded = true;
                }
                if(!"".equalsIgnoreCase(lastPlatformVersion) && !lastPlatformVersion.equals(hubItem.getPlatformVersion())) {
                    platformUpgraded = true;
                }
                hubState = hubItem.getState();
            }

            try {
                dashboardItem = new DashboardItem();
                dashboardItem.setHeading("Home Status");
                String messages = "";
                if ("DOWN".equalsIgnoreCase(hubState) || devicesOffline > 0 || devicesNoSignal > 0 || devicesLowBattery > 0 || hubUpgraded || platformUpgraded || filterRuntime > Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_FILTER_HOURS, "0"))) {
                    if("DOWN".equalsIgnoreCase(hubState)) {
                        messages += hubItem.getHubName() + " is OFFLINE.<br>";
                    }
                    if(devicesOffline > 0) {
                        messages += devicesOffline + " devices are OFFLINE.<br>";
                    }
                    if(devicesNoSignal > 0) {
                        messages += devicesNoSignal + " devices have NO SIGNAL.<br>";
                    }
                    if(devicesLowBattery > 0) {
                        messages += devicesLowBattery + " devices have LOW BATTERY.<br>";
                    }
                    if(hubUpgraded) {
                        messages += "Your hub has been upgraded to the latest firmware.<br>";
                        if(mSharedPrefs.getBoolean("homeStatusAlerted", false)) {
                            mSharedPrefs.edit().putString("lastHubVersion", hubItem.getVersion());
                        }
                    }
                    if(platformUpgraded) {
                        messages += "Iris platform has been upgraded.<br>";
                        mSharedPrefs.edit().putString("lastPlatformVersion", hubItem.getPlatformVersion());
                    }

                    if(filterRuntime > Integer.parseInt(mSharedPrefs.getString(IrisPlusConstants.PREF_FILTER_HOURS, "0"))) {
                        messages += "Furnace filter needs to be replaced.<br>";
                    }
                    messages = messages.substring(0, messages.length() - 4);
                    mSharedPrefs.edit().putBoolean("homeStatusAlerted", false).commit();
                } else {
                    messages += "All OK!";
                }
                dashboardItem.setStatus(messages);
                dashboards.add(dashboardItem);
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_INFO, "error: Could not load Home Status into the Dashboard. " + e.getMessage());
            }

            try {
                if (devicesOn > 0 || devicesOff > 0) {
                    dashboardItem = new DashboardItem();
                    dashboardItem.setHeading("Control");
                    String content = "<big><big><b>" + devicesOn + "</b></big></big> on&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                            "<big><big><b>" + devicesOff + "</b></big></big> off";
                    dashboardItem.setStatus(content);
                    dashboards.add(dashboardItem);
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_INFO, "error: Could not load Control into the Dashboard. " + e.getMessage());
            }

            try {
                if (energyDevices) {
                    dashboardItem = new DashboardItem();
                    dashboardItem.setHeading("Energy Usage");
                    String content = "Smart Plugs are using <big><big><b>";
                    if(energy < 1000) {
                        content += energy + "</b></big></big> W";
                    } else {
                        content += energy / 1000 + "</b></big></big> kW";
                    }
                    dashboardItem.setStatus(content);
                    dashboards.add(dashboardItem);
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_INFO, "error: Could not load Energy Usage into the Dashboard. " + e.getMessage());
            }

            try {
                if (doorsLocked > 0 || doorsUnlocked > 0) {
                    dashboardItem = new DashboardItem();
                    dashboardItem.setHeading("Locks");
                    String content = "<big><big><b>" + doorsLocked + "</b></big></big> locked&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<big><big><b>" + doorsUnlocked + "</b></big></big> unlocked";
                    dashboardItem.setStatus(content);
                    dashboards.add(dashboardItem);
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_INFO, "error: Could not load Locks into the Dashboard. " + e.getMessage());
            }

            try {
                if (sensorsOpen > 0 || sensorsClosed > 0) {
                    dashboardItem = new DashboardItem();
                    dashboardItem.setHeading("Contact Sensors");
                    String content = "<big><big><b>" + sensorsOpen + "</b></big></big> open&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<big><big><b>" + sensorsClosed + "</b></big></big> closed";
                    dashboardItem.setStatus(content);
                    dashboards.add(dashboardItem);
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_INFO, "error: Could not load Contact Sensors into the Dashboard. " + e.getMessage());
            }

            try {
                if (peopleHome > 0 || peopleAway > 0) {
                    dashboardItem = new DashboardItem();
                    dashboardItem.setHeading("Presence");
                    String content = "";
                    if(peopleHome > 0 || peopleAway > 0) {
                        content += "<big><big><b>" + peopleHome + "</b></big></big> people are home<br>";
                    }
                    content = content.substring(0, content.length() - 4);
                    dashboardItem.setStatus(content);
                    dashboards.add(dashboardItem);
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_INFO, "error: Could not load Presence into the Dashboard. " + e.getMessage());
            }

            try {
                if (alarmStatus != null) {
                    dashboardItem = new DashboardItem();
                    dashboardItem.setHeading("Security");
                    String content = "Alarm is set to <big><b>" + alarmStatus + "</b></big>";
                    dashboardItem.setStatus(content);
                    dashboards.add(dashboardItem);
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_INFO, "error: Could not load Security into the Dashboard. " + e.getMessage());
            }

            try {
                if (thermostatMode != null) {
                    dashboardItem = new DashboardItem();
                    dashboardItem.setHeading("Thermostat");
                    String content = "<big><big><b>" + thermostatCurrent + "°</b></big></big> now&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
                    content += "<big><big><b>" + thermostatLow + "°/" + thermostatHigh + "°</b></big></big> target<br>";
                    content += "Humidity is <big><big><b>" + thermostatHumidity + "</b></big></big><br>";
                    content += "Current mode is <big><b>" + thermostatMode + "</b></big><br>";
                    content += "Filter runtime is <big><b>" + filterRuntime + "</b></big> hours<br>";
                    dashboardItem.setStatus(content);
                    dashboards.add(dashboardItem);
                }
            } catch (Exception e) {
                logger.log(IrisPlusConstants.LOG_INFO, "error: Could not load Thermostat into the Dashboard. " + e.getMessage());
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get dashboard success");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dashboards;
    }

    /**
     * Get hub details
     * @return JSON representation of a hub
     */
    public HubItem getHubDetails() {
        HubItem hubItem = null;
        try {
            String hubDetails = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:place:@HUBID@\",\"isRequest\":true},\"payload\":{\"messageType\":\"place:GetHub\",\"attributes\":{}}}");
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
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get hub details success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hubItem;
    }

    /**
     * Get place premium status
     * @return boolean premium status
     */
    public boolean isPlacePremium(String accountId, String placeName) {
        boolean premium = true;
        try {
            String places = this.sendToWebsocket(API_URL, "{\"headers\":{\"destination\":\"SERV:account:" + accountId + "\",\"isRequest\":true},\"payload\":{\"messageType\":\"account:ListPlaces\",\"attributes\":{}}}");
            JSONObject jsonObj = new JSONObject(places.toString());
            jsonObj = new JSONObject(jsonObj.getString("payload"));
            jsonObj = new JSONObject(jsonObj.getString("attributes"));
            JSONArray jsonArray = jsonObj.getJSONArray("places");
            try {
                for(int i = 0; i < jsonArray.length(); i++) {
                    jsonObj = jsonArray.getJSONObject(i);
                    if(jsonObj.getString("place:name").equalsIgnoreCase(placeName)) {
                        //Place matched - get service type
                        String serviceType = jsonObj.getString("place:serviceLevel");
                        if("basic".equalsIgnoreCase(serviceType)) {
                            premium = false;
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.log(IrisPlusConstants.LOG_DEBUG, "Get place service type success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return premium;
    }
}
