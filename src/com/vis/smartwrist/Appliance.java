package com.vis.smartwrist;

import android.util.Log;

public class Appliance {
	
	private Boolean selected; //list selection
	private Boolean highlighted; //appliance selection
	private String name;
	private Boolean calibrated;
	private float azimuthEnter;
	private float azimuthExit;
	
	private static final String LOG_TAG = "Appliance.java";
	
	public static final int STATE_OFF = 0;
	public static final int STATE_ON = 1;
	
	public Appliance (String name) {
		this.name = name;
		this.calibrated = false;
		this.selected = false;
		this.dehighlight();
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
	
	public Boolean inRange (float p) {
//		boolean inRange = false;
//		switch(direction) {
//			case Compass.DIRECTION_FROM_LEFT:
//				if(this.azimuthEnter >= p && p >= this.azimuthExit) {
//					inRange = true;
//				}
//				break;
//			case Compass.DIRECTION_FROM_RIGHT:
//				if(this.azimuthExit <= p && p <= this.azimuthEnter) {
//					inRange = true;
//				}
//				break;
//		}
//		return inRange;
		Boolean inRange = false;
		if(this.isCalibrated()) {
//			Log.v(LOG_TAG, "is calibrated");
			if(this.azimuthEnter >= p && p >= this.azimuthExit) {
	//			this.select();
				inRange = true;
			}
		}
		return inRange;
	}

	public Boolean isHighlighted() {
		return highlighted;
	}
	
	public void highlight() {
		this.highlighted = true;
	}

	public void dehighlight() {
		this.highlighted = false;
	}
	
}
