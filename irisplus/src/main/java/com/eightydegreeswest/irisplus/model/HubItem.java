package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ybelenitsky on 5/26/2015.
 */
public class HubItem implements Serializable {

    private static final long serialVersionUID = 6302362072707519941L;

    String hubName;
    String version;
    String state;
    String macAddress;
    String model;
    String id;
    String role;
    String platformVersion;

    //Advanced
    String battery;
    String localIp;
    String powerSource;
    Date lastZwaveRebuild;
    String zwaveRebuildRecommended;
    String externalIp;
    Date lastRestartTime;

    public HubItem() {
    }

    public String getHubName() {
        return hubName;
    }

    public void setHubName(String hubName) {
        this.hubName = hubName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getPowerSource() {
        return powerSource;
    }

    public void setPowerSource(String powerSource) {
        this.powerSource = powerSource;
    }

    public Date getLastZwaveRebuild() {
        return lastZwaveRebuild;
    }

    public void setLastZwaveRebuild(Date lastZwaveRebuild) {
        this.lastZwaveRebuild = lastZwaveRebuild;
    }

    public String getZwaveRebuildRecommended() {
        return zwaveRebuildRecommended;
    }

    public void setZwaveRebuildRecommended(String zwaveRebuildRecommended) {
        this.zwaveRebuildRecommended = zwaveRebuildRecommended;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public Date getLastRestartTime() {
        return lastRestartTime;
    }

    public void setLastRestartTime(Date lastRestartTime) {
        this.lastRestartTime = lastRestartTime;
    }
}
