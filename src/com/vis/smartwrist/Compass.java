package com.vis.smartwrist;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/** Manages the device sensors used for detecting device orientation (azimuth, x-axis)
 * 
 * @author tilman
 *
 */
public class Compass implements SensorEventListener {
	
	private SensorManager mSensorManager;
	private Sensor orientation;
	private SmartWrist activity;
	private float azimuth;
	private Boolean active;
	protected float[] accelVals;
	
	public static final int DIRECTION_FROM_LEFT = 0;
	public static final int DIRECTION_FROM_RIGHT = 1;
	
	private static final String LOG_TAG = "Compass.java";
	private static final float ALPHA = 0.2f;
	
	@SuppressWarnings("deprecation")
	public Compass(SmartWrist activity) {
		Log.v(LOG_TAG, "new Compass()");
		this.activity = activity;
		this.active = false;
		
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

	/** invoked with each compass reading; cycles through list of appliances and checks if azimuth within an appliance's range
	 *  
	 */
	public void onSensorChanged(SensorEvent event) {
		
        accelVals = event.values; //optional: use lowpass filter: lowPass( event.values, accelVals );
        
        //TODO: use y +-angle b to select a square rather than a simple range
		float x = accelVals[0]; //event.values[0];
		float y = accelVals[1]; //event.values[1];
		float z = accelVals[2]; //event.values[2];
		
		//print out compass reading to UI
		activity.setStatus("Compass reading: " + x);
		
		Appliance[] appliances = SmartWrist.appliances;
		for(int i=0; i<appliances.length; i++) {
			Appliance appl = (Appliance) appliances[i];
			
			if(appl.inRange(x)) {

				if(!appl.isSelected()) {
					appl.select();
					//highlighting doesn't work. Why? Fuck you! That's why.
					appl.getListview().setBackgroundResource(R.color.green);
					if(!activity.conn.isRegistered())
						activity.vibrate(SmartWrist.VIBRATION_DURATION_ENTER);
					activity.conn.notifyServer(appl.getName(), Appliance.STATE_ON);
				}
			} 
			else {
				if(appl.isSelected()) {
					appl.deselect();
					appl.getListview().setBackgroundResource(R.color.white);
					if(!activity.conn.isRegistered())
						activity.vibrate(SmartWrist.VIBRATION_DURATION_EXIT);
					activity.conn.notifyServer(appl.getName(), Appliance.STATE_OFF);
				}
			}
		}
		setAzimuth(x);
	}
	
	/**
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://developer.android.com/reference/android/hardware/Sensor.html#TYPE_ACCELEROMETER
	 */
	protected float[] lowPass( float[] input, float[] output ) {
	    if ( output == null ) return input;

	    for ( int i=0; i<input.length; i++ ) {
	        output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
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
