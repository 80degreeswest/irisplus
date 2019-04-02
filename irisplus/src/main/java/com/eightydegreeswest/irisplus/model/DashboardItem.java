package com.eightydegreeswest.irisplus.model;

import java.io.Serializable;

public class DashboardItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

	String heading;
    String status;

    public DashboardItem() {}

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }
}