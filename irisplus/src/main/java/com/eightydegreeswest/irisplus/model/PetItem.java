package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;

public class PetItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

	String petName;
    int icon;
    String state;
    String id;

    public PetItem() {
        this.setIcon(R.drawable.ic_pet);
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
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

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}