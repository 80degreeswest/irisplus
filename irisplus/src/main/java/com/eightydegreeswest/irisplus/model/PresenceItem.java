package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;

public class PresenceItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

	String name;
    int icon;
    String state;
    String id;
    Long lastChanged;
    String devicesAtHome;

    public PresenceItem() {
        this.setIcon(R.drawable.ic_presence);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(Long lastChanged) {
        this.lastChanged = lastChanged;
    }

    public String getDevicesAtHome() {
        return devicesAtHome;
    }

    public void setDevicesAtHome(String devicesAtHome) {
        this.devicesAtHome = devicesAtHome;
    }
}