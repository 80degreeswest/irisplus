package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;
import java.util.List;

public class IrrigationItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

	String deviceName;
    String onOffState;
    String state;       //WATERING, IDLE, DELAY
    String control;     //MANUAL, SCHEDULE, AUTO
    String id;
    String type;
    String mode;
    String next;

    List<IrrigationZoneItem> zones;

    public IrrigationItem() {

    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOnOffState() {
        return onOffState;
    }

    public void setOnOffState(String onOffState) {
        this.onOffState = onOffState;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getControl() {
        return control;
    }

    public void setControl(String control) {
        this.control = control;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<IrrigationZoneItem> getZones() {
        return zones;
    }

    public void setZones(List<IrrigationZoneItem> zones) {
        this.zones = zones;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}