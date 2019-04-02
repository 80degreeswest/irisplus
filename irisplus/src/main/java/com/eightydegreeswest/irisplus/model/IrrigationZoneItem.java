package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;

public class IrrigationZoneItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

	String id;
    String name;
    int number;
    boolean active;
    String status;
    String defaultDuration;

    public IrrigationZoneItem() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDefaultDuration() {
        return defaultDuration;
    }

    public void setDefaultDuration(String defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}