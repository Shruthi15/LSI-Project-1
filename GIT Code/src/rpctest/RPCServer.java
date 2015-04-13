package rpctest;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import jdk.nashorn.internal.runtime.RewriteException;


public class RPCServer implements Runnable {
   
  DatagramSocket rpcSocket = null;
  
  
     
   public RPCServer() {
      try {
    	 
    	System.out.println("Inside RPC Server");
    	
    	rpcSocket = new DatagramSocket(Utils.PORT);
    	 
    	
	  	       
      } catch (SocketException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }
   
  
	public void run()
	{
		while(true)
		{
			
			System.out.println("Running RPC Server Thread");
			byte[] inputBuffer= new byte[512];
			byte[] outputBuffer= new byte[512];
			
			
			DatagramPacket receivePacket= new DatagramPacket(inputBuffer, inputBuffer.length);
		    
			try
			{
				/*Receive packet here */
				System.out.println("RPC server is going to receive packet");
				rpcSocket.receive(receivePacket);
				System.out.println("RPC server received packet: " + new String(receivePacket.getData()).trim());
				InetAddress returnAddr= receivePacket.getAddress();
				System.out.println("Rpc Server received from client: "+ returnAddr.getHostAddress());
				System.out.println("Add this server to view table");
				String serverup = returnAddr.getHostAddress();
				String viewtuple = serverup + Utils.VIEW_DELIMITER + "UP" + Utils.VIEW_DELIMITER +System.currentTimeMillis();
				ArrayList<String> tempview = new ArrayList<>();
				tempview.add(viewtuple);
				ArrayList<String> myview = Welcome.ViewTable.getViewTable();
				Welcome.ViewTable.mergeView(tempview, myview);
				int returnPort= receivePacket.getPort();
				System.out.println("RPC server return port: "+ Integer.toString(returnPort));
				String recieved=new String(receivePacket.getData()).trim();
				String[] packetData = recieved.split(Utils.NETWORK_DELIMITER);
				System.out.println("Packet data is: "+ packetData);
				String actiontaken = actionToTake(packetData);
				outputBuffer=actiontaken.getBytes();
				System.out.println("RPC server sending packet: "+ actiontaken);
				DatagramPacket sendPacket= new DatagramPacket(outputBuffer, outputBuffer.length, returnAddr, returnPort);
				
				rpcSocket.send(sendPacket);
				System.out.println("RPC server sent packet.");
				
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private String actionToTake(String[] packetData)
	{
	    //packet in the form callID@OPcode@packetData
		
		int opCode = Integer.parseInt(packetData[1]);
		String response=null;
		ByteArrayOutputStream opStream = new ByteArrayOutputStream();
	    ObjectOutput output;
		//Read session
		if(opCode==1) 
		{
			response =RPCServerUtil.readContent(packetData);
			
		}
		//Write session
		if(opCode==2)
		{
			response=RPCServerUtil.writeContent(packetData);
		}
		//TODO: GOSSIP with servers
		if(opCode==3) 
		{
			response=RPCServerUtil.ExchangeViews(packetData);
			
		}
			        
		System.out.println("Server response: " + response);
		
				
	  return response;
	}
}
			
