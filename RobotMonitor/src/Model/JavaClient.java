package Model;

import java.lang.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author xu0012ng
 */
public class JavaClient {
   private Socket socket = null;
   private BufferedReader reader = null;
   private BufferedWriter writer = null;
   
   public JavaClient(InetAddress address, int port) throws IOException{
	   
	   //TODO
	   //Comment it out for testing
//      socket = new Socket(address, port);
//      reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//      writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  
	   System.out.println("Connectio established");
   }
   
   public void send(String msg) throws IOException
    {
	   System.out.println("Seng CMD: " + msg);
	   //TODO
//        writer.write(msg, 0, msg.length());
//        writer.flush();
    }

   public String recv() throws IOException
    {//TODO
     //   return reader.readLine();
	   return "33333";
    }
   
   public String sendForResponse(String msg) throws IOException{
	   this.send(msg);
	   return this.recv();
   }
   
   public void close() throws IOException{
	   //TODO
	   System.out.println("Socket closing...");
//	   this.reader.close();
//	   this.writer.close();
//	   this.socket.close();
   }
    /**
     * @param args the command line arguments
     */
//    public static void main(String[] args) {
//        // TODO code application logic here
//        try {
//            InetAddress host = InetAddress.getByName("172.21.150.95"); // 172.21.150.95 for eth0, 192.168.6.1 for wlan
//            JavaClient client = new JavaClient(host, 6666);
//            while (true) {
//                client.send("Hello server.\n");
//                String response = client.recv();
//                System.out.println("Client received: " + response);
//            }
//        }
//        catch (IOException e) {
//            System.out.println("Caught Exception: " + e.toString());
//        }        
//    }
    
}
