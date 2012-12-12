package com.vis.smartwrist;

import android.util.Log;

public class Appliance {
	
	private Boolean selected;
	private String name;
	private Boolean calibrated;
	private float azimuthEnter;
	private float azimuthExit;
	
	private static final String LOG_TAG = "Appliance.java";
	
	public Appliance (String name) {
		this.name = name;
		this.calibrated = false;
		this.selected = false;
	}
	
	public Boolean isSelected () {
		return this.selected;
	}
	
	public void select() {
		this.selected = true;
	}
	
	public void deselect() {
		this.selected = false;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		String ret;
		if (this.isCalibrated()) {
			ret = this.getName() + " (calibrated)";
		} else {
			ret = this.getName();
		}
		return ret;
	}
	
	public Boolean isCalibrated() {
		return this.calibrated;
	}
	
	public void setCalibrated(Boolean b) {
		this.calibrated = b;
		if(b) {
			Log.v(LOG_TAG, this.getName() + " calibrated: (" + azimuthEnter + ", " + azimuthExit + ")");
		}
	}

	public float getAzimuthEnter() {
		return azimuthEnter;
	}

	public void setAzimuthEnter(float azimuthEnter) {
		this.azimuthEnter = azimuthEnter;
	}

	public float getAzimuthExit() {
		return azimuthExit;
	}

	public void setAzimuthExit(float azimuthExit) {
		this.azimuthExit = azimuthExit;
	}
	
}
