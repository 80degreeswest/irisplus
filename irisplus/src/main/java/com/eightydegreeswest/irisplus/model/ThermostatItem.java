package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;

public class ThermostatItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

	String id;
    String name;
    int icon;

    public ThermostatItem() {
    	this.setIcon(R.drawable.ic_thermostat);
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

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}