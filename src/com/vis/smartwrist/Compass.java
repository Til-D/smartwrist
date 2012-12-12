package com.vis.smartwrist;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Compass implements SensorEventListener {
	
	private SensorManager mSensorManager;
	Sensor orientation;
	private SmartWrist activity;
	private float azimuth;
	private Boolean active;
	
	public static final int DIRECTION_FROM_LEFT = 0;
	public static final int DIRECTION_FROM_RIGHT = 1;
	
	private static final String LOG_TAG = "Compass.java";
	private static final int MAX_TICKS = 1;
	private int ticks;
	private static final float ALPHA = 0.2f;
	
	public Compass(SmartWrist activity) {
		Log.v(LOG_TAG, "new Compass()");
		this.activity = activity;
		this.active = false;
		this.ticks = 0;
		
		mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
	    orientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	    
	    if (orientation != null){
			Log.v(LOG_TAG, "orientation available.");
		} else {
			Log.e(LOG_TAG,  "orientation not available");
		}
	    
	    setAzimuth(0);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		//only check every x ticks (MAX_TICKS)
		if (ticks==0) {
			Appliance[] appliances = SmartWrist.appliances;
			for(int i=0; i<appliances.length; i++) {
				Appliance appl = (Appliance) appliances[i];
				
				if(appl.inRange(x)) {
//					//enter from left
//					if (this.getAzimuth() >= x) {
//						appl.select();
//						activity.vibrate(SmartWrist.VIBRATION_DURATION_ENTER);
//					}
//					//enter from right
//					else {
//						
//					}
					if(!appl.isSelected()) {
						appl.select();
						activity.vibrate(SmartWrist.VIBRATION_DURATION_ENTER);
						activity.conn.notifyServer(appl.getName(), Appliance.STATE_ON);
					}
				} 
				else {
					if(appl.isSelected()) {
						appl.deselect();
						activity.vibrate(SmartWrist.VIBRATION_DURATION_EXIT);
						activity.conn.notifyServer(appl.getName(), Appliance.STATE_OFF);
					}
				}
			}
			
			
		} 
		ticks = (ticks + 1) % MAX_TICKS;
		setAzimuth(x);
	}
	
	protected void start() {
		this.active = true;
		mSensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_UI);
	}
	
	protected void onPause() {
	    // Unregister the listener on the onPause() event to preserve battery life;
		this.active = false;
	    mSensorManager.unregisterListener(this);
	}

	protected void onResume() {
		this.active = true;
	    mSensorManager.registerListener(this, orientation, SensorManager.SENSOR_DELAY_UI);
	}

	public float getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}
	
	public Boolean isActive() {
		return this.active;
	}

}
