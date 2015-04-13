package rpctest;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.UUID;
import java.net.UnknownHostException;

//Hey man!

@SuppressWarnings("unused")
public class RPCClient
{
	public static Integer maxPacketSize = 512;
	public static Integer destPort = Utils.PORT; //Destination Port Number: portProj1bRPC   =   5300; 
	public static Integer waitTime = 20; // The wait time is mentioned in seconds;
	private final int SOCKET_TIMEOUT = 20*1000;
	DatagramSocket rpcSocket = null;
	public static int CallID = 0;
	
	public int getCallID(){
		return CallID += 1;
	}
	
	public RPCClient(){
		
		try {
			rpcSocket = new DatagramSocket();
		} catch (SocketException e1) {

			e1.printStackTrace();
		}

		try {
			rpcSocket.setSoTimeout(SOCKET_TIMEOUT);
		} catch (SocketException e) {

			e.printStackTrace();
		}
		
	}
	
	public String ForwardToServer(String serverIP, String data){
		DatagramPacket recvPkt = null;
		Integer temp = getCallID();
		String callID = temp.toString();
		
		byte [] inBuf = new byte[maxPacketSize];
		byte [] outBuf = new byte[maxPacketSize];
		outBuf = data.getBytes();
	
		
		
		InetAddress destinationAddr = null;
		try {
			destinationAddr = InetAddress.getByName(serverIP);
			
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		}
		
		System.out.println("RPC client sending to server at : " + destinationAddr + ":" + destPort);
		DatagramPacket sendPkt = new DatagramPacket(outBuf,outBuf.length ,destinationAddr, destPort );
		System.out.println("RPC client sending packet: "+ new String(outBuf));
		try {
			
			rpcSocket.send(sendPkt);
		
			System.out.println("RPC client sent packet : "+ new String(sendPkt.getData()));
			
		 recvPkt= new DatagramPacket(inBuf, inBuf.length);
		
		do{
			recvPkt.setLength(inBuf.length);
			rpcSocket.receive(recvPkt);
		}while(checkCallID(callID, recvPkt));
		
		System.out.println("Received the packet: "+ new String(recvPkt.getData()).trim());
			
		}catch (NullPointerException e) {
			
			e.printStackTrace();
		}catch (SocketTimeoutException e) {
			System.out.println("Socket timed out");
			recvPkt = null;
		} catch (IOException e) {
			
			e.printStackTrace();
		}finally{
			rpcSocket.close();
			System.out.println("RPC socket closed");
		}
		
		if(recvPkt != null){
			String receivepacket = new String(recvPkt.getData());
			return receivepacket.trim();
		}
		else
			return null;
	
	}

	private boolean checkCallID(String callID2, DatagramPacket receivePacket) {
		String receivedCallID = Utils.parseReceivePacket(receivePacket);
		
		if(receivedCallID.contentEquals(callID2))
			return true;
		else
			return false;
	}
}
