import java.lang.*;
import java.io.*;
import java.net.*;

class TCPSender {

   public static void main(String args[]) {
      try {
         Socket skt = new Socket("129.69.180.218", 1337);
         BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
         PrintWriter out = new PrintWriter(skt.getOutputStream(), true);

         out.println("register:1337");

         System.out.print("Received string: '");
         System.out.println("Server response: " + in.readLine());

         // while (!in.ready()) {}
         // System.out.println(in.readLine()); // Read one line and output it

         // System.out.print("'\n");
         out.close();
         in.close();
         skt.close();
      }
      catch(Exception e) {
         System.out.print("Whoops! It didn't work!\n");
      }
   }
}