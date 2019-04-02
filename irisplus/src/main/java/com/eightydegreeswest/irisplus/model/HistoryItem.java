package com.eightydegreeswest.irisplus.model;

import java.io.Serializable;

public class HistoryItem implements Serializable {

	private static final long serialVersionUID = 5663806579755915134L;
	
	String date;
    String description;
    String id;
	String offset;
    
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}
}