package com.vis.smartwrist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class ServerConnection {
	
	private SmartWrist activity;
	private String ip;
	private int port;
	private String serverIp; //base server ip
	private int serverTCPPort; //base server port
	private Boolean registered;
	private UDPConnection updc;
	
	private static final String LOG_TAG = "ServerConnection";
	public static final int DEFAULT_PORT = 80;
	public static final int UDP_PORT = 1337;
	private static final String SERVER_COMMAND_REGISTER = "register";
	
	public static final int SERVER_COMPASS_UDP_PORT = 33333;
	
	public ServerConnection(SmartWrist activity) {
		Log.v(LOG_TAG, "new ServerConnection()");
		this.activity = activity;
		registered = false;
	}
	
	/**
	 * opens up tcp connection to server and registers its udp socket by sending ip and port
	 * @throws UnknownHostException
	 * @throws IOException
	 * @return String: Server response
	 */
	public void register(String ip, int port) {
		this.serverIp = ip;
		this.serverTCPPort = port;
		Log.v(LOG_TAG, "register(): " + this.serverIp + " (port:" + this.serverTCPPort + ")");
		try {
			disableStrictMode();
			String urlString = "http://" + this.serverIp + ":" + this.serverTCPPort + "/wristband?cmd=" + SERVER_COMMAND_REGISTER + "&port=" + UDP_PORT;
			Log.v(LOG_TAG, "request url: " + urlString);
			
			URL url = new URL(urlString);
		    URLConnection conn = url.openConnection();
		    conn.setConnectTimeout(2000);
		    conn.setReadTimeout(2000);
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.flush();

		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    String resp = "";
		    while ((line = rd.readLine()) != null) {
		       resp += line;
		    }
		    Log.v(LOG_TAG, "Server response: " + resp);
		    String[] r = resp.split(":");
		    this.ip = r[r.length-2];
		    this.port = Integer.parseInt(r[r.length-1]);
		    Log.v(LOG_TAG, "ip set to: " + this.ip);
		    Log.v(LOG_TAG, "port set to: " + this.port);
		    
		    activity.setConnectionLabel("Device IP: " + this.ip); // + ", udp port: " + this.port);
		    registered = true;
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, "Could not connect to server: " + ip + ":" + port);
			activity.setStatus("Could not connect to server: " + ip + ":" + port);
			e.printStackTrace();
		} 
	}
	
	public void notifyServer(String id, int state)  {
		Log.v(LOG_TAG, "compass reading (" + id + "), state: " + state);
        String udpMsg = id + ":" + state;
        DatagramSocket ds = null;
        try {
        	disableStrictMode();
            ds = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(this.serverIp);
            DatagramPacket dp;
            dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, SERVER_COMPASS_UDP_PORT);
            ds.send(dp);
            System.out.println("UDP client sent: " + udpMsg + " to: " + this.serverIp + " (" + SERVER_COMPASS_UDP_PORT + ")");
        } catch (SocketException e) {
            e.printStackTrace();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
    }
	
	public void openUDPSocket() {
		Log.v(LOG_TAG, "openUPDSocket()");
		this.activity.setStatus("listening on udp port: " + UDP_PORT);
		updc = new UDPConnection(this.activity);
		updc.execute(new String[] {});

	}
	
	public void closeUDPSocket() {
		Log.v(LOG_TAG, "closeUDPSocket()");
		updc.closeSocket();
//		updc.cancel(true);
	}
	
	public Boolean isRegistered() {
		return this.registered;
	}
	
	private void disableStrictMode() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
		Log.v(LOG_TAG, "disableStrictMode()");
		//circumvent strictMode (http://android-developers.blogspot.de/2010/12/new-gingerbread-api-strictmode.html)
		Class strictModeClass=Class.forName("android.os.StrictMode");
        Class strictModeThreadPolicyClass=Class.forName("android.os.StrictMode$ThreadPolicy");
        Object laxPolicy = strictModeThreadPolicyClass.getField("LAX").get(null);
        Method method_setThreadPolicy = strictModeClass.getMethod(
                "setThreadPolicy", strictModeThreadPolicyClass );
        method_setThreadPolicy.invoke(null,laxPolicy);
	}
	
	private class UDPConnection extends AsyncTask<String, Void, String> {
		
		private SmartWrist activity;
		private Boolean listening;
		
		public UDPConnection(SmartWrist activity) {
			this.activity = activity;
			this.listening = false;
		}
		
		public void closeSocket() {
			this.listening = false;
		}
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			
			byte[] message = new byte[256];
			DatagramPacket p = new DatagramPacket(message, message.length);
			DatagramSocket udpSocket;
			try {
				
				disableStrictMode();
				udpSocket = new DatagramSocket(UDP_PORT);
				this.listening = true;
				
				while(this.listening) {
					Log.d(LOG_TAG,"listening on " + UDP_PORT);
//					this.activity.setStatus("listening on udp port: " + UDP_PORT);
					udpSocket.receive(p);
					String cmd = new String(message, 0, p.getLength());
					Log.d(LOG_TAG,"message received: " + cmd);
//					this.activity.setStatus("server msg received: " + cmd);
					
					//TODO: switch different commands (enter, out,..)
					this.activity.vibrate(SmartWrist.VIBRATION_DURATION_ENTER);
				}
				
				udpSocket.close();
				
			} catch (SocketException e) {
				Log.d(LOG_TAG, "SocketException: could not create DatagramSocket");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(LOG_TAG, "IOException (udpSocket.receive())");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				Log.d(LOG_TAG, "ClassNotFoundException (strictModeClass)");
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

	    @Override
	    protected void onPostExecute(String result) {
	      System.out.println("UDPConnection open");
	    }

		
	  }
	
}

