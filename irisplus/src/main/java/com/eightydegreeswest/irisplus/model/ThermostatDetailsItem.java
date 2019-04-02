package com.eightydegreeswest.irisplus.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Yuriy on 10/11/15.
 */
public class ThermostatDetailsItem implements Serializable {

    private List<ThermostatItem> thermostats;
    private String currentTemperature;
    private String targetTemperature;
    private String heatTargetTemperature;
    private String coolTargetTemperature;
    private String mode;
    private String status;
    private String humidity;
    private String filterStatus;

    public List<ThermostatItem> getThermostats() {
        return thermostats;
    }

    public void setThermostats(List<ThermostatItem> thermostats) {
        this.thermostats = thermostats;
    }

    public String getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(String currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public String getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(String targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public String getHeatTargetTemperature() {
        return heatTargetTemperature;
    }

    public void setHeatTargetTemperature(String heatTargetTemperature) {
        this.heatTargetTemperature = heatTargetTemperature;
    }

    public String getCoolTargetTemperature() {
        return coolTargetTemperature;
    }

    public void setCoolTargetTemperature(String coolTargetTemperature) {
        this.coolTargetTemperature = coolTargetTemperature;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getFilterStatus() {
        return filterStatus;
    }

    public void setFilterStatus(String filterStatus) {
        this.filterStatus = filterStatus;
    }
}
