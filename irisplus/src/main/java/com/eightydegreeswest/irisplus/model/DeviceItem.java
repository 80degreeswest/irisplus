package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;

public class DeviceItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

    private String deviceName;
    private int icon;
    private String type;
    private String batteryPercentage;
    private String batteryLevel;
    private String signal;
    private String temperature;
    private String state;
    private String status;
    private String id;
    private String nextEvent;
    private String power;
    private String deviceTypeHint;

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public DeviceItem() { this.setIcon(R.drawable.ic_device); }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(String batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNextEvent() {
        return nextEvent;
    }

    public void setNextEvent(String nextEvent) {
        this.nextEvent = nextEvent;
    }

    public String getDeviceTypeHint() {
        return deviceTypeHint;
    }

    public void setDeviceTypeHint(String deviceTypeHint) {
        this.deviceTypeHint = deviceTypeHint;
    }
}