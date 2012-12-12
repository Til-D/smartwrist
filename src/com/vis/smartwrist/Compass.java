package com.vis.smartwrist;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Compass implements SensorEventListener {
	
	private SensorManager mSensorManager;
	Sensor accelerometer;
	Sensor magnetometer;
	private SmartWrist activity;
	private float azimuth;
	
	private static final String LOG_TAG = "Compass.java";
	
	public Compass(SmartWrist activity) {
		Log.v(LOG_TAG, "new Compass()");
		this.activity = activity;
		mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	    
	    if (accelerometer != null){
			Log.v(LOG_TAG, "Accelerometer available.");
		} else {
			Log.e(LOG_TAG,  "Accelerometer not available");
		}
	    if (magnetometer != null){
			Log.v(LOG_TAG, "Magnetometer available.");
		} else {
			Log.e(LOG_TAG,  "Magnetometer not available");
		}
	    
	    setAzimuth(0);
	    
	    mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	float[] mGravity;
	float[] mGeomagnetic;
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
			if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
				mGeomagnetic = event.values;
		    if (mGravity != null && mGeomagnetic != null) {
		      float R[] = new float[9];
		      float I[] = new float[9];
		      boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
		      if (success) {
		    	  float orientation[] = new float[3];
		    	  SensorManager.getOrientation(R, orientation);
		    	  setAzimuth(orientation[0]); // orientation contains: azimut, pitch and roll
		    	  Log.v(LOG_TAG, "sensor value changed: " + this.azimuth);
		      }
		}
	}
	
	protected void onPause() {
	    // Unregister the listener on the onPause() event to preserve battery life;
	    mSensorManager.unregisterListener(this);
	}

	protected void onResume() {
		mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
	    mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	public float getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}

}
