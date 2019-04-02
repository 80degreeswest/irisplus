package com.eightydegreeswest.irisplus.model;

import com.eightydegreeswest.irisplus.R;

import java.io.Serializable;

public class SceneItem implements Serializable {

	private static final long serialVersionUID = 6302362072707519941L;

	String sceneName;
    String id;
    boolean enabled;
	long lastFired;
	boolean firing;

    public SceneItem() {

    }

	public String getSceneName() {
		return sceneName;
	}

	public void setSceneName(String sceneName) {
		this.sceneName = sceneName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getLastFired() {
		return lastFired;
	}

	public void setLastFired(long lastFired) {
		this.lastFired = lastFired;
	}

	public boolean isFiring() {
		return firing;
	}

	public void setFiring(boolean firing) {
		this.firing = firing;
	}
}