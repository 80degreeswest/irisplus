package com.eightydegreeswest.irisplus.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.eightydegreeswest.irisplus.apiv2.ControlApi;
import com.eightydegreeswest.irisplus.apiv2.DeviceApi;
import com.eightydegreeswest.irisplus.apiv2.IrrigationApi;
import com.eightydegreeswest.irisplus.apiv2.LockApi;
import com.eightydegreeswest.irisplus.apiv2.SceneApi;
import com.eightydegreeswest.irisplus.apiv2.SecurityApi;
import com.eightydegreeswest.irisplus.apiv2.ThermostatApi;
import com.eightydegreeswest.irisplus.common.IrisPlusLogger;
import com.eightydegreeswest.irisplus.constants.IrisPlusConstants;
import com.eightydegreeswest.irisplus.model.ControlItem;
import com.eightydegreeswest.irisplus.model.DeviceItem;
import com.eightydegreeswest.irisplus.model.IrrigationItem;
import com.eightydegreeswest.irisplus.model.LockItem;
import com.eightydegreeswest.irisplus.model.ThermostatDetailsItem;
import com.eightydegreeswest.irisplus.model.ThermostatItem;

import java.util.List;

public class AutomationTask extends AsyncTask<Void, Void, Boolean> {

	private Context mContext = null;
	private IrisPlusLogger logger = new IrisPlusLogger();
	private String mDeviceName;
    private String mDeviceID;
    private String mDeviceType;
    private String mNewStatus;

	public AutomationTask(Context context, String deviceName, String newStatus, boolean voice) {
		mContext = context;
		mDeviceName = deviceName;
        mNewStatus = newStatus;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {

        DeviceApi deviceApi = new DeviceApi(mContext);
        List<DeviceItem> devices = deviceApi.getHomeStatus();

        if("alarm".equalsIgnoreCase(mDeviceName)) {
            SecurityApi securityApi = new SecurityApi(mContext);
            securityApi.setAlarm(mNewStatus, false);
            return true;
        } else if("alarmbypassed".equalsIgnoreCase(mDeviceName)) {
            SecurityApi securityApi = new SecurityApi(mContext);
            securityApi.setAlarm(mNewStatus, true);
            return true;
        } else if("raindelay".equalsIgnoreCase(mDeviceName) || "rain delay".equalsIgnoreCase(mDeviceName)) {
            for(DeviceItem deviceItem : devices) {
                if("Irrigation".equalsIgnoreCase(deviceItem.getDeviceTypeHint())) {
                    IrrigationApi irrigationApi = new IrrigationApi(mContext);
                    irrigationApi.setIrrigationDelay(deviceItem.getId(), mNewStatus);
                }
            }
            return true;
        } else if("scene".equalsIgnoreCase(mDeviceName) || "run".equalsIgnoreCase(mDeviceName) || "execute".equalsIgnoreCase(mDeviceName)) {
            SceneApi sceneApi = new SceneApi(mContext);
            sceneApi.runSceneByName(mNewStatus);
            return true;
        } else if("pause".equalsIgnoreCase(mDeviceName) || "delay".equalsIgnoreCase(mDeviceName)) {
            try {
                Thread.sleep(Integer.parseInt(mNewStatus) * 1000);
            } catch (InterruptedException e) { }
            return true;
        }

        try {
            for (DeviceItem deviceItem : devices) {
                if (fixDeviceName(deviceItem.getDeviceName()).equalsIgnoreCase(fixDeviceName(mDeviceName))) {
                    mDeviceID = deviceItem.getId();
                    mDeviceType = deviceItem.getDeviceTypeHint();
                    if("Garage Door".equalsIgnoreCase(mDeviceType)) {
                        LockApi lockApi = new LockApi(mContext);
                        if("open".equalsIgnoreCase(mNewStatus) || "opened".equalsIgnoreCase(mNewStatus)) {
                            lockApi.setLockState(mDeviceID, "\"motdoor:doorstate\":\"OPEN\"");
                        } else if ("close".equalsIgnoreCase(mNewStatus) || "closed".equalsIgnoreCase(mNewStatus)) {
                            lockApi.setLockState(mDeviceID, "\"motdoor:doorstate\":\"CLOSED\"");
                        }
                        return true;
                    } else if("Lock".equalsIgnoreCase(mDeviceType)) {
                        LockApi lockApi = new LockApi(mContext);
                        if("unlocked".equalsIgnoreCase(mNewStatus) || "unlock".equalsIgnoreCase(mNewStatus)) {
                            lockApi.setLockState(mDeviceID, "\"doorlock:lockstate\":\"UNLOCKED\"");
                        } else if ("locked".equalsIgnoreCase(mNewStatus) || "lock".equalsIgnoreCase(mNewStatus)) {
                            lockApi.setLockState(mDeviceID, "\"doorlock:lockstate\":\"LOCKED\"");
                        }
                        return true;
                    } else if("Thermostat".equalsIgnoreCase(mDeviceType)) {
                        if(isNumeric(mNewStatus)) {
                            //Temperature
                            ThermostatApi thermostatApi = new ThermostatApi(mContext);
                            String[] thermostatStatus = mNewStatus.split(":");
                            thermostatApi.setTemperature(mDeviceID, thermostatStatus[1], thermostatStatus[0]);
                            //thermostatApi.setTemperature(mDeviceID, mNewStatus);
                        } else if("cool|heat|auto|off".contains(mNewStatus.toLowerCase())) {
                            //Mode
                            ThermostatApi thermostatApi = new ThermostatApi(mContext);
                            thermostatApi.setThermostatMode(mDeviceID, mNewStatus);
                        }
                        return true;
                    } else if("Fan Control".equalsIgnoreCase(mDeviceType)) {
                        ControlApi controlApi = new ControlApi(mContext);
                        controlApi.setFanSpeed(mDeviceID, mNewStatus);
                        return true;
                    } else if("Blind".equalsIgnoreCase(mDeviceType) || "SomfyV1Blind".equalsIgnoreCase(mDeviceType)) {
                        ControlApi controlApi = new ControlApi(mContext);
                        controlApi.setBlindState(mDeviceID, mNewStatus);
                        return true;
                    } else if("Dimmer".equalsIgnoreCase(mDeviceType)) {
                        ControlApi controlApi = new ControlApi(mContext);
                        try {
                            Long dimmerValue = new Long(mNewStatus);
                            if(dimmerValue >= 0 && dimmerValue <= 100) {
                                controlApi.setDeviceIntensity(mDeviceID, dimmerValue.intValue());
                            }
                        } catch(Exception e) {
                            controlApi.setDeviceState(mDeviceID, mNewStatus);
                        }
                        return true;
                    } else if("Irrigation".equalsIgnoreCase(mDeviceType)) {
                        //TODO
                        return true;
                    }  else {
                        ControlApi controlApi = new ControlApi(mContext);
                        controlApi.setDeviceState(mDeviceID, mNewStatus);
                        return true;
                    }
                }
            }
        } catch (Exception e) { }

		return false;
	}

	@Override
	protected void onPostExecute(final Boolean success) {
        try {
            if(success) {
                //notificationHelper = new NotificationHelper(mContext);
                //notificationHelper.createNotificationWithSoundAndVibrate(mDeviceName + " has been set to " + mNewStatus + " automatically.");
                logger.log(IrisPlusConstants.LOG_INFO, "Automation task executing");
                logger.log(IrisPlusConstants.LOG_INFO, "Set deviceID " + mDeviceID + "(" + mDeviceName + ") to " + mNewStatus);
            } else {
                logger.log(IrisPlusConstants.LOG_INFO, "Automation task failed");
                logger.log(IrisPlusConstants.LOG_INFO, "Failed to set deviceID " + mDeviceID + "(" + mDeviceName + ") to " + mNewStatus);
            }
        } catch (Exception e) {
            //Ignore - the broadcast was cancelled
        }
	}

    private String fixDeviceName(String deviceName) {
        String retVal = deviceName;
        try {
            retVal = retVal.replaceAll("1", "one");
            retVal = retVal.replaceAll("2", "two");
            retVal = retVal.replaceAll("3", "three");
            retVal = retVal.replaceAll("4", "four");
            retVal = retVal.replaceAll("5", "five");
            retVal = retVal.replaceAll("6", "six");
            retVal = retVal.replaceAll("7", "seven");
            retVal = retVal.replaceAll("8", "eight");
            retVal = retVal.replaceAll("9", "nine");
            retVal = retVal.replaceAll("10", "ten");
            retVal = retVal.replaceAll("0", "zero");
            retVal = retVal.replaceAll(" ", "");
            return retVal;
        } catch (Exception e) {
            return deviceName;
        }
    }

    private boolean isNumeric(String str) {
        try {
            int i = Integer.parseInt(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}