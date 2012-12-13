package com.vis.smartwrist;

import android.util.Log;
import android.view.View;

/** Data structure and methods for appliances
 * 
 * @author tilman
 *
 */
public class Appliance {
	
	private Boolean selected; //list selection
	private Boolean highlighted; //appliance selection
	private String name;
	private Boolean calibrated;
	private View listview;
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
	
	/** Checks if azimuth is within appliance bounds
	 * 
	 * @param p float: compass azimuth
	 * @return Boolean, if azimuth in range of appliance bounds
	 */
	public Boolean inRange (float p) {
		Boolean inRange = false;
		if(this.isCalibrated()) {
			if(this.azimuthEnter >= this.azimuthExit) {
				if(p >= 0 && p <= this.azimuthExit) {
//					Log.v(LOG_TAG, "in range: " + p + " [" + this.azimuthEnter + ", " + this.azimuthExit + "]");
					inRange = true;
				}
				else {
					if(p >= this.azimuthEnter) {
//						Log.v(LOG_TAG, "in range: " + p + " [" + this.azimuthEnter + ", " + this.azimuthExit + "]");
						inRange = true;
					}
				}
			}
			else {
				if(this.azimuthEnter <= p && p <= this.azimuthExit) {
//					Log.v(LOG_TAG, "in range: " + p + " [" + this.azimuthEnter + ", " + this.azimuthExit + "]");
					inRange = true;
				}
			}
		}
		return inRange;
	}
	
	public void resetBounds() {
		this.azimuthEnter = 0;
		this.azimuthExit = 0;
		this.calibrated = false;
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
	
	public static float min(float[] array) {
	      // Validates input
	      if (array == null) {
	          throw new IllegalArgumentException("The Array must not be null");
	      } else if (array.length == 0) {
	          throw new IllegalArgumentException("Array cannot be empty.");
	      }
	  
	      // Finds and returns min
	      float min = array[0];
	      for (int i = 1; i < array.length; i++) {
	          if (Float.isNaN(array[i])) {
	              return Float.NaN;
	          }
	          if (array[i] < min) {
	              min = array[i];
	          }
	      }
	  
	      return min;
	  }

	public View getListview() {
//		Log.v(LOG_TAG, "retrieving ListView for: " + this.getName());
		return this.listview;
	}

	public void setListview(View listview) {
//		Log.v(LOG_TAG, "ListView set for: " + this.getName());
		this.listview = listview;
	}
}
