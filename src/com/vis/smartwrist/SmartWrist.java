package com.vis.smartwrist;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SmartWrist extends Activity {
	
	Button connectButton;
	Button openUDPButton;
	EditText ipField;
	EditText portField;
	TextView statusLabel;
	TextView connectionLabel;
	ListView applianceList;
	
	ServerConnection conn;
	SharedPreferences prefs;
	ArrayAdapter<Appliance> appliancesAdapter;
	View selectedItem;
	Compass compass;
	
	private static final String LOG_TAG = "SmartWrist.java";
	
	public static final Appliance[] appliances = new Appliance[] {
		new Appliance("Radio"), 
		new Appliance("TV"), 
		new Appliance("Lampe"), 
		new Appliance("Heizung"),
		new Appliance("Tischlampe")
	};
	
	public static final int VIBRATION_DURATION_ENTER = 150; //ms
	public static final int VIBRATION_DURATION_EXIT = 300; //ms
	
	public static final String APP_STORAGE_PREFIX = "com.vis.smartwrist";
	public static final String STORAGE_SERVER_IP = APP_STORAGE_PREFIX + ".server.ip";
	public static final String STORAGE_SERVER_PORT = APP_STORAGE_PREFIX + ".server.port";
	
	private static final int MENU_QUIT = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point);
        
        prefs = getSharedPreferences(APP_STORAGE_PREFIX, Context.MODE_PRIVATE);
        
        //initialize UI elements
        connectButton = (Button) findViewById(R.id.button_connect);
        openUDPButton = (Button) findViewById(R.id.button_open_socket);
        ipField = (EditText) findViewById(R.id.edit_server_ip);
        portField = (EditText) findViewById(R.id.edit_server_port);
        statusLabel = (TextView) findViewById(R.id.label_status);
        connectionLabel = (TextView) findViewById(R.id.label_connection);
        applianceList = (ListView) findViewById(R.id.list_appliances);
        
        Log.v(LOG_TAG, "Retrieving config from SharedPreferences: " + prefs.getString(STORAGE_SERVER_IP, "") + ":" + prefs.getString(STORAGE_SERVER_PORT, ""));
        ipField.setText(prefs.getString(STORAGE_SERVER_IP, ""));
        portField.setText(prefs.getString(STORAGE_SERVER_PORT, ""));
        
        //fill appliance list
        appliancesAdapter = new ArrayAdapter<Appliance>(this, android.R.layout.simple_list_item_1, appliances);
        applianceList.setAdapter(appliancesAdapter);
        applianceList.setOnItemClickListener(applianceListClickHandler);
        
        compass = new Compass(this);
        compass.start();
        
        conn = new ServerConnection(this);
        addButtonListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_point, menu);
        menu.add(0, MENU_QUIT, 0, "Quit");
        return true;
    }
    
    /* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case MENU_QUIT:
	    	conn.closeUDPSocket();
	    	compass.onPause();
	        SmartWrist.this.finish();
	        return true;
	    }
	    return false;
	}
    
    private void addButtonListener() {
    	System.out.println("addButtonListener()");
    	
    	connectButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String ip = ipField.getText().toString();
				String port = portField.getText().toString();
				
				System.out.println("Setting SharedPreferences to: " + ip + ":" + port);
				prefs.edit().putString(STORAGE_SERVER_IP, ip).commit();
				prefs.edit().putString(STORAGE_SERVER_PORT, port).commit();
				
				try {
					int portI = Integer.parseInt(port);
					System.out.println("Connecting to: " + ip + " (port:" + port + ")");

					conn.register(ip, portI);
//					conn.openUDPSocket();
				} catch (Exception e) {
					setStatus("Could not connect to ip: " + ipField.getText() + " (port: " + port + ")");
					e.printStackTrace();
				}
			}
		});
    	
    	openUDPButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(conn.isRegistered()) {
					conn.openUDPSocket();
				} else {
					setStatus("Not registered at server.");
				}
				
			}
		});
    }
    
    private OnItemClickListener applianceListClickHandler = new OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            Appliance item = (Appliance) applianceList.getItemAtPosition(position);
        	Log.v(LOG_TAG, "Click on: " + item.toString());
        	
        	if(selectedItem != null) {
        		selectedItem.setBackgroundResource(R.color.white);	
        		if(!selectedItem.equals(v) || !item.isHighlighted()) {
        			selectedItem = v;
            		v.setBackgroundResource(R.color.orange);
            		startCalibrationAppliance(item);
        		} else {
        			stopCalibrationAppliance(item);
        		}
        	} 
        	else {
        		selectedItem = v;
        		v.setBackgroundResource(R.color.orange);
        		startCalibrationAppliance(item);
        	}
        }
    };
    
	private void startCalibrationAppliance(Appliance item) {
		Log.v(LOG_TAG, "startCalibrationAppliance: " + item.toString());
		item.highlight();
		item.setAzimuthEnter(readCompass());
	}
	
	private void stopCalibrationAppliance(Appliance item) {
		Log.v(LOG_TAG, "stopCalibrationAppliance: " + item.toString());
		item.setAzimuthExit(readCompass());
		item.dehighlight();
		item.setCalibrated(true);
		appliancesAdapter.notifyDataSetChanged();
	}
	
	private float readCompass() {
		return compass.getAzimuth();
	}
    
    public void setStatus(String msg) {
    	statusLabel.setText(msg);
    }
    
    public void setConnectionLabel(String msg) {
    	connectionLabel.setText(msg);
    }
    
    public void vibrate(int duration) {
    	Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vib.vibrate(duration);
    }
}
