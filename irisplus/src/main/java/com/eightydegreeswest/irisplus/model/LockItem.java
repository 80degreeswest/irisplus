package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;

public class LockItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

	String lockName;
    String state;
    String id;
    String type;
    boolean buzzIn;

    public LockItem() {
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        this.lockName = lockName;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean isBuzzIn() {
        return buzzIn;
    }

    public void setBuzzIn(boolean buzzIn) {
        this.buzzIn = buzzIn;
    }
}