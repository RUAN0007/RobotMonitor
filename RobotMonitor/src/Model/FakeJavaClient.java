package Model;


import java.io.IOException;
import java.net.InetAddress;

public class FakeJavaClient extends JavaClient {

	 public FakeJavaClient(InetAddress address, int port) throws IOException{
		   
		 
		   System.out.println("Fake Connection established");
	   }
	   
	   public void send(String msg) throws IOException
	    {
		   System.out.println("Send Fake CMD: " + msg);
	    }

	   public String recv() throws IOException
	    {
		   System.out.print("Receiving Fake CMD: " );
		    String received =  "ACK";
		   
	 	   System.out.println(" " + received);
	 	   return received;
	    }
	   
	   public String sendForResponse(String msg) throws IOException{
		   this.send(msg);
		   return this.recv();
	   }
	   
	   public void close() throws IOException{
		   System.out.println("Fake Socket closing...");
		 
	   }
}
