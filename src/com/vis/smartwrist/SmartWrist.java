package com.vis.smartwrist;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SmartWrist extends Activity {
	
	Button connectButton;
	Button openUDPButton;
	EditText ipField;
	EditText portField;
	TextView statusLabel;
	TextView connectionLabel;
	
	ServerConnection conn;
	SharedPreferences prefs;
	String[] appliances;
	
	private static final String LOG_TAG = "SmartWrist.java";
	public static final int VIBRATION_DURATION_ENTER = 150; //ms
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
        
        Log.v(LOG_TAG, "Retrieving config from SharedPreferences: " + prefs.getString(STORAGE_SERVER_IP, "") + ":" + prefs.getString(STORAGE_SERVER_PORT, ""));
        ipField.setText(prefs.getString(STORAGE_SERVER_IP, ""));
        portField.setText(prefs.getString(STORAGE_SERVER_PORT, ""));
        
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
    
    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ip = Formatter.formatIpAddress(inetAddress.hashCode());
                        return ip;
                    }
                }
            }
        } catch (SocketException ex) {
            System.out.println(ex.toString());
        }
        return null;
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