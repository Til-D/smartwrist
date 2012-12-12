import java.io.*;
import java.net.*;
import java.lang.*;
import java.net.Socket;
import java.net.ServerSocket;

public class WristServer {

    private final int MAX_UDP_DATAGRAM_LEN = 512;
    private final int SERVER_PORT = 1337;

	public static void main (String[] args) {
        System.out.println("Starting..");
        
        WristServer server = new WristServer();
        server.getServerInfo();
        server.runTCPServer();

        System.out.println("done");
	}

    private void runTCPServer() {
        
        try {
         ServerSocket srvr = new ServerSocket(SERVER_PORT);
         Socket skt = srvr.accept();
         String clientIp = skt.getInetAddress().getHostAddress();
         System.out.println("Handling client at " + clientIp + " on port " + skt.getPort());

        BufferedReader  in  = new BufferedReader(new InputStreamReader(skt.getInputStream()));
        PrintWriter out = new PrintWriter(skt.getOutputStream(), true);

         String msg = "empty";

         //read input
         // while (!in.ready()) {}
       
            while ((msg = in.readLine()) != null) {
                System.out.println("Client message: " + msg);
                String port = msg.split(":")[1];
                out.println("Device registered as: " + clientIp + ':' + port);
            }
                  
         //write response
         // String resp = data + msg;
         // byte[] byteBuffer = resp.getBytes();
         // System.out.print("Sending response: '" + resp + "'\n");
         // out.write(byteBuffer);


         out.close();
         in.close();
         skt.close();
         srvr.close();
      }
      catch(Exception e) {
         System.out.print("Whoops! It didn't work!\n");
      }
    }

    public void getServerInfo() {
       try {
       java.net.InetAddress i = java.net.InetAddress.getLocalHost();
       System.out.println("GET SERVER INFO ********");
       System.out.println(i);                  // name and IP address
       System.out.println(i.getHostName());    // name
       System.out.println(i.getHostAddress()); // IP address only
       System.out.println("************************");
       }
       catch(Exception e){e.printStackTrace();}
     }

	private void runUdpServer() {
        String lText;
        byte[] lMsg = new byte[MAX_UDP_DATAGRAM_LEN];
        DatagramPacket dp = new DatagramPacket(lMsg, lMsg.length);
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(SERVER_PORT);
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
