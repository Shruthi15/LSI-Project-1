package rpctest;
/**
 * 
 */


import java.net.DatagramPacket;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author karteeka
 *
 */
public class Utils {

	public static final int RESILIENCY_FACTOR = 3;
	public static final String SERVER_ID_NULL = "0.0.0.0";
	public static final int PORT = 5300;
	public static final String COOKIE_DELIMITER = "_";
	public static final String REPLICA_DELIMITER = ",";
	public static final int SESSION_READ_OPCODE = 1;
	public static final String ERROR = "Error!";
	public static final String SESSION_DATA_DELIMITER = "#";
	public static final String NETWORK_DELIMITER = "@";
	public static final String VIEW_DELIMITER = "|";
	public static final String SIMPLE_DB_DELIMITER = "%";
	public static final int SESSION_WRITE_OPCODE = 2;
	public static final int GOSSIP_OPCODE = 3;
	
	
	public static String parseExpTimeFromDate(Date d){
		
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm:ss a");
		String expTime = formatter.format(d);
		return expTime;
		
	}
	
	public static Date parseExpTimeFromString(String state) {
		// TODO Auto-generated method stub
		if(state == null)
			return null;
		System.out.println("String expiration time is:" + state);
		Date exp = null; 
		String[] pairs = state.split(SESSION_DATA_DELIMITER);
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm:ss a");
		try {
			System.out.println(pairs[2]);
			exp = formatter.parse(pairs[3]);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return exp;
	}

	public static String parseReceivePacket(DatagramPacket receivePacket) {
		
		if(receivePacket == null)
			return null;
		
		String callid;
		
		String[] packetData = new String(receivePacket.getData(),0,receivePacket.getLength()).split(Utils.NETWORK_DELIMITER);
		
		callid = packetData[0];
		return callid;
	}

	public static String parseSessionIDFromString(String packetdata) {
		
		if(packetdata == null)
			return null;
		
		String[] pairs = packetdata.split(SESSION_DATA_DELIMITER);
		//Packet data is in the form of SESSIONID#VERSIONNUM#SESSIONSTATE#EXPIRATIONTIME
		String sessionID = pairs[0];
		System.out.println("Session ID is:" +sessionID);
		return sessionID;
	}

	

	public static String parseSessionStateFromString(String packetdata) {
		
		if(packetdata == null)
			return null;
		
		String[] pairs = packetdata.split(SESSION_DATA_DELIMITER);
		//Packet data is in the form of SESSIONID#VERSIONNUM#SESSIONSTATE#EXPIRATIONTIME
		String state = pairs[2];
		System.out.println("Session state is:" +state);
		return state;
		
	}
	
	public static String parseExpirationTimeFromString(String packetdata) {
		
		if(packetdata == null)
			return null;
		System.out.println("Inside parseExpirationTime" + "packetdata is :" + packetdata);
		String[] pairs = packetdata.split(SESSION_DATA_DELIMITER);
		//Packet data is in the form of SESSIONID#VERSIONNUM#SESSIONSTATE#EXPIRATIONTIME
		String expTime = pairs[3];
		System.out.println("Exp Time is:" +expTime);
		return expTime;
		
	}

	public static String parseCookieAndGetSessionID(String cookieValue) {
		// TODO Auto-generated method stub
		if(cookieValue == null)
			return null;
		
		String[] temp = cookieValue.split(Utils.COOKIE_DELIMITER);
		String sessionID = temp[0];
		return sessionID;
	}

	public static ArrayList<String> parseCookieAndGetResilientServers(
			String cookieValue) {
		// TODO Auto-generated method stub
		if(cookieValue == null)
			return null;
		
		System.out.println("Cookie value is "+cookieValue);
		ArrayList<String> replicas = new ArrayList<>();
		String[] temp = cookieValue.split(COOKIE_DELIMITER);
		String[] servers = temp[2].split(Pattern.quote(REPLICA_DELIMITER));
		for(int i=0; i<servers.length; i++){
			if(!servers[i].equalsIgnoreCase(""))
				replicas.add(servers[i]);
		}
		
		for(int i=0;i<replicas.size();i++)
		{
			if(replicas.get(i).equals("SERVER")){
				replicas.remove(i);
			}	
		}
		
		System.out.println("Resilient servers are: "+ replicas);
		return replicas;
	}

	public static String parseCookieAndGetVersion(String cookieValue) {
		// TODO Auto-generated method stub
		if(cookieValue == null)
			
			return null;
		
		String[] temp = cookieValue.split(COOKIE_DELIMITER);
		String version = temp[1];
		return version;
	}

	public static boolean checkIfVersionSame(String version, int versionNum) {
		// TODO Auto-generated method stub
		
		String versiontemp = Integer.toString(versionNum);
		if(version.contentEquals(versiontemp))
			return true;
		else
			return false;
	}

	public static boolean checkSessionExpired(Date expTimeStamp) {
		// TODO Auto-generated method stub
		
		
		if(expTimeStamp != null)
			if(System.currentTimeMillis() > new Timestamp(expTimeStamp.getTime()).getTime())
				return true;
		
		
		return false;
	}

	public static String constructFirstTimeCookie(SessionData sessionData,
			ArrayList<String> backupServers, String serverid2) {
		// TODO Auto-generated method stub
		StringBuilder locationmetadata = new StringBuilder();
		
		
		
		
		locationmetadata.append(serverid2);
		
		for(int i=0; i < backupServers.size(); i++){
			
				locationmetadata.append(Utils.REPLICA_DELIMITER);
				locationmetadata.append(backupServers.get(i));
			
			
		}
		if(backupServers.size() == 0){
			for(int i=0; i<Utils.RESILIENCY_FACTOR; i++){
				locationmetadata.append(REPLICA_DELIMITER);
				locationmetadata.append("SERVER_ID_NULL");
			}
		}
		else if(backupServers.size()<Utils.RESILIENCY_FACTOR)
		{
			for(int i=0; i<	(Utils.RESILIENCY_FACTOR-backupServers.size());i++)
			{	locationmetadata.append(REPLICA_DELIMITER);
				locationmetadata.append("SERVER_ID_NULL");

			}
		}
		System.out.println("Location metadata is: "+ locationmetadata.toString().trim());
		String cookievalue= sessionData.getSessionID() + COOKIE_DELIMITER + sessionData.getVersionNum() + COOKIE_DELIMITER + locationmetadata.toString();
		return cookievalue;
	}
	public static String constructCookie(SessionData sessionData,
			ArrayList<String> backupServers, String serverid2) {
		// TODO Auto-generated method stub
		StringBuilder locationmetadata = new StringBuilder();
		//locationmetadata.append(serverid2);
		
		
		//locationmetadata.append(Utils.REPLICA_DELIMITER);
		
		String backups = null;
		for(int i=0; i < backupServers.size(); i++){
			String temp = backupServers.get(i);
			if(!temp.equalsIgnoreCase(Utils.SERVER_ID_NULL)){
			backups = temp + REPLICA_DELIMITER;
			}
			else{
				backups = "SERVER_ID_NULL" + REPLICA_DELIMITER;
							}
		}
		if(backupServers.size() == 0){
			for(int i=0; i<Utils.RESILIENCY_FACTOR; i++)
			backups = backups + "SERVER_ID_NULL" + REPLICA_DELIMITER;
		}
		
		/*if(backupServers.size()<Utils.RESILIENCY_FACTOR)
		{
			for(int i=0; i<	(Utils.RESILIENCY_FACTOR-backupServers.size());i++)
			{
				locationmetadata.append("SERVER_ID_NULL");

			}
		}*/
		System.out.println("Location metadata is: "+ locationmetadata.toString().trim());
		String cookievalue= sessionData.getSessionID() + COOKIE_DELIMITER + sessionData.getVersionNum() + COOKIE_DELIMITER + backups;
		System.out.println("cookie value now with backup servers is: "+ cookievalue);
		return cookievalue;
	}
	public static String constructViewTuple(String serveridlocal,
			String serverStatus, long currentTimeMillis) {
		String view = serveridlocal + VIEW_DELIMITER + serverStatus + VIEW_DELIMITER + Long.toString(currentTimeMillis);
		return view;
	}
}
