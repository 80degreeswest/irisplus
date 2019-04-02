package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;

public class ControlItem implements Serializable {
	 
	private static final long serialVersionUID = 6302362072707519941L;
	
	String controlName;
    String type;
    int icon;
    String status;
    String state;
    int devicesOn;
    int devicesOff;
    boolean hasDimmers;
    String id;
    int intensity;
    boolean dimmer;
    String speed;
    boolean hasSpeed;
	boolean shade;
	String nextEvent;

    public ControlItem() {
    	this.setIcon(R.drawable.ic_control);
    }

	public String getControlName() {
		return controlName;
	}

	public void setControlName(String controlName) {
		this.controlName = controlName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public int getDevicesOn() {
		return devicesOn;
	}

	public void setDevicesOn(int devicesOn) {
		this.devicesOn = devicesOn;
	}

	public int getDevicesOff() {
		return devicesOff;
	}

	public void setDevicesOff(int devicesOff) {
		this.devicesOff = devicesOff;
	}

	public boolean isHasDimmers() {
		return hasDimmers;
	}

	public void setHasDimmers(boolean hasDimmers) {
		this.hasDimmers = hasDimmers;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getIntensity() {
		return intensity;
	}

	public void setIntensity(int intensity) {
		this.intensity = intensity;
	}

	public boolean isDimmer() {
		return dimmer;
	}

	public void setDimmer(boolean dimmer) {
		this.dimmer = dimmer;
	}

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public boolean isHasSpeed() {
        return hasSpeed;
    }

    public void setHasSpeed(boolean hasSpeed) {
        this.hasSpeed = hasSpeed;
    }

	public String getNextEvent() {
		return nextEvent;
	}

	public void setNextEvent(String nextEvent) {
		this.nextEvent = nextEvent;
	}

	public boolean isShade() {
		return shade;
	}

	public void setShade(boolean shade) {
		this.shade = shade;
	}
}