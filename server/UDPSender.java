import java.io.*;
import java.net.*;

public class UDPSender {
	
    private final int UDP_SERVER_PORT = 1337;
    private final String UDP_SERVER_IP = "129.69.180.224"; //129.69.180.222";

	public static void main (String[] args) {
		System.out.println("Start..");
		
        new UDPSender().runUdpClient();

		System.out.println("done");
	}

	private void runUdpClient()  {
        String udpMsg = "hello world from UDP client " + UDP_SERVER_PORT;
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(UDP_SERVER_IP);
            DatagramPacket dp;
            dp = new DatagramPacket(udpMsg.getBytes(), udpMsg.length(), serverAddr, UDP_SERVER_PORT);
            ds.send(dp);
            System.out.println("UDP client sent: " + udpMsg + " to: " + UDP_SERVER_IP + " (" + UDP_SERVER_PORT + ")");
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
}
