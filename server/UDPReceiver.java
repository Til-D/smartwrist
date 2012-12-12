import java.io.*;
import java.net.*;

public class UDPReceiver {

    private final int MAX_UDP_DATAGRAM_LEN = 512;
    private final int UDP_SERVER_PORT = 1337;

	public static void main (String[] args) {
        System.out.println("Start..");
        
        new UDPReceiver().runUdpServer();

        System.out.println("done");
	}

	private void runUdpServer() {
        String lText;
        byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
        DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(UDP_SERVER_PORT);
            //disable timeout for testing
            //ds.setSoTimeout(100000);

            // System.out.println("Hostname: " + ds.getInetAddress().getHostName());

            System.out.println("Waiting to receive...");
            ds.receive(dp);

            System.out.println("host address: " + dp.getAddress().getHostAddress());
            System.out.println("host name: " + dp.getAddress().getHostName());

            lText = new String(lMsg, 0, dp.getLength());
            System.out.println("UDP packet received: " + lText);
            
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
    }
}
