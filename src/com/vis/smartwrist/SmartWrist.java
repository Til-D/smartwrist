package com.vis.smartwrist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

/** main activity for SmartWrist: connects to a server, opens up a udp socket for receiving data packages that are translated into
 * vibrations. Calibration for azimuth ranges of appliances allows definition spaces in a room that when entered, create a device vibration
 * and send out a udp package to the server. 
 * 
 * @author tilman
 *
 */
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
	public static final int VIBRATION_DURATION_EXIT = 150; //ms
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
        ipField.setSelected(false);
        portField.setText(prefs.getString(STORAGE_SERVER_PORT, ""));
        portField.setSelected(false);
        
        //fill appliance list
        //TODO: add remove calibration option to each item
        appliancesAdapter = new ArrayAdapter<Appliance>(this, android.R.layout.simple_list_item_1, appliances);
        applianceList.setAdapter(appliancesAdapter);
        applianceList.setOnItemClickListener(applianceListClickHandler);
        
        //add views to appliance objects
        for(int i=0; i<applianceList.getCount(); i++) {
        	Appliance appl = (Appliance) appliancesAdapter.getItem(i);
        	View v = appliancesAdapter.getView(i, null, null);
    		appl.setListview(v);
        }
        
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
	@SuppressWarnings("finally")
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
		    switch (item.getItemId()) {
			    case MENU_QUIT: {
			    	Log.v(LOG_TAG, "QUIT");
			    	conn.closeUDPSocket();
			    	compass.onPause();
			    	System.exit(0); 
			    }
		    return false;
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
    		return true;
    	}
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
				} catch (Exception e) {
					setStatus("Could not connect to ip: " + ipField.getText() + " (port: " + port + ")");
					e.printStackTrace();
				}
			}
		});
    	
    	openUDPButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				conn.openUDPSocket();
			}
		});
    }
    
    /** Calibration list: one click highlights the list element and marks the left boundary of the appliance range.
     * A second click dehighlights the list element and marks the right boundary of the appliance range. Sets appliance.isCalibrated to true.
     * 
     */
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
    
	/** sets status label used to display system state
	 * 
	 * @param msg message to be displayed
	 */
    public void setStatus(String msg) {
    	statusLabel.setText(msg);
    }
    
    /** sets connection state label displaying device ip
     * 
     * @param msg message to be displayed
     */
    public void setConnectionLabel(String msg) {
    	connectionLabel.setText(msg);
    }
    
    /** Shows a popup dialog box with dedicated message
     * 
     * @param msg
     * @param title
     */
    public void showDialogBox(String title, String msg) {
    	new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(msg)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
                dialog.cancel();
            }
         })
         .show();
    }
    
    public void vibrate(int duration) {
    	Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vib.vibrate(duration);
    }
}
