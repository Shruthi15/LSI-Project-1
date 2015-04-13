package rpctest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SessionController {

	 
	public SessionControllerReturnObject getSession(RPCServerUtil rpcManager, SessionManager sessionmanager, String cookieValue, String serverID, ViewTable viewTable) throws ParseException {
		
		/* get session data from table if replicated locally, otherwise issue
		 * session read through RPC to the servers mentioned in cookieValue
		 */
		 
		if(cookieValue == null || serverID == null)
			return null;
		System.out.println("Inside getSession");
		String sessionID= Utils.parseCookieAndGetSessionID(cookieValue);
		String version = Utils.parseCookieAndGetVersion(cookieValue);
		String readFromClient = null;
		SessionData sessionData = null;
		SessionControllerReturnObject returnobject = null;
		int versionNum;
		String sessionState;
		Date expTime;
		String sessionValues;
		ArrayList<String>resilient_servers = Utils.parseCookieAndGetResilientServers(cookieValue);
		
		
		if(checkIfLocalSameAsServersInCookie(serverID, resilient_servers)){
			//construct sessionData
			System.out.println("I am primary or backup server");
			sessionValues = sessionmanager.getDataFromTable(sessionID);
			if(sessionValues == null){
				System.out.println("I am primary or backup server, and I don't have the session with me");
				return null;
			}
				
			else{
				sessionData = new SessionData();
				returnobject = new SessionControllerReturnObject();
				
				String[] pairs = sessionValues.split(Utils.SESSION_DATA_DELIMITER);
				versionNum = Integer.parseInt(pairs[0]);
				sessionState = pairs[1];
				System.out.print("I am primary/backup and I am getting this session for you :"+sessionID + Utils.SESSION_DATA_DELIMITER + sessionValues);
				SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm:ss a");
				expTime =formatter.parse(pairs[2]);
				
					sessionData.setVersionNum(versionNum);
					sessionData.setSessionState(sessionState);
					sessionData.setExpirationTime(expTime);
					sessionData.setSessionID(sessionID);
				System.out.println("Fetched session from myself.");
				returnobject.server = serverID;
				returnobject.sessiondata = sessionData;
				
				
			}
		}
		else{
			//send a session read to  all the resilient servers
			//send callID'@'OPCODE'@'SessionID for SESSION_READ
			System.out.println("I am not primary or backup server!!");
			RPCClient rpcClient = new RPCClient();
			String data = Integer.toString(rpcClient.getCallID()) +Utils.NETWORK_DELIMITER+ Integer.toString(Utils.SESSION_READ_OPCODE) +Utils.NETWORK_DELIMITER+sessionID;

			System.out.println("Send session read to primary/backup servers. Sending data: "+ data);
			for(String server : resilient_servers){
				System.out.println("Backup server is: "+ server);
				if(!server.equalsIgnoreCase(Utils.SERVER_ID_NULL)){
					System.out.println("Resilient server is: "+ server);	
					readFromClient = rpcClient.ForwardToServer(server, data);


					//Construct session Data from the value returned by rpcClient
					if(readFromClient != null ){
						sessionData = new SessionData();
						returnobject = new SessionControllerReturnObject();
						System.out.println("Session Read successful from server: "+ server + " and response from client is: "+ readFromClient);
						String[] temp = readFromClient.split(Utils.NETWORK_DELIMITER);
						//System.out.println("Temp is : "+ temp);
						if(temp == null)
							System.out.println("Temp is null");
						String[] session_data = temp[1].split(Utils.SESSION_DATA_DELIMITER);
						sessionID = session_data[0];
						
						
						versionNum = Integer.parseInt(session_data[1]);
						sessionState = session_data[2];
						SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMM dd, yyyy HH:mm:ss a");
						expTime = formatter.parse(session_data[3]);
						//if(Utils.checkIfVersionSame(version, versionNum)){
						sessionData.setVersionNum(versionNum);
						sessionData.setSessionState(sessionState);
						sessionData.setExpirationTime(expTime);
						sessionData.setSessionID(sessionID);
						System.out.println("Fetched session from server: "+ server );
						returnobject.server = server;
						returnobject.sessiondata = sessionData;

					}
					else if(readFromClient == null){
						System.out.println("ReadFromClient is null");
						System.out.println("Setting the server to down.");
						String viewtuple = server + Utils.VIEW_DELIMITER + "DOWN" + Utils.VIEW_DELIMITER+ System.currentTimeMillis();
						ArrayList<String> tempview = new ArrayList<>();
						tempview.add(viewtuple);
						ArrayList<String> myview = viewTable.getViewTable();
						viewTable.mergeView(tempview, myview);
						//return null;
					}
				}
			}

		}
		
		return returnobject;
	}

	public ArrayList<String> storeSession(SessionManager sessionmanager, RPCServerUtil rpcManager, ArrayList<String> tempBackupServers, ViewTable viewTable, SessionData storeSessionData, String serverid, String cookievalue){
		System.out.println("Inside store session");
		ArrayList<String> foundBackupServers = new ArrayList<>();
		
		int view_size = viewTable.getViewTable().size();  //-------try for view_size number of times
		System.out.println("i am: "+serverid);
		RPCClient rpcClient = new RPCClient();
		//System.out.println("SessionData: "+ sessionmanager.getDataFromTable(storeSessionData));
		String sessionid = storeSessionData.getSessionID();
		String session_data =   Integer.toString(storeSessionData.getVersionNum()) + Utils.SESSION_DATA_DELIMITER + storeSessionData.getSessionState() + Utils.SESSION_DATA_DELIMITER + Utils.parseExpTimeFromDate(storeSessionData.getExpTimeStamp());
		System.out.println("Session data in StoreSession is: "+ session_data);
		ArrayList<String>resilient_servers = Utils.parseCookieAndGetResilientServers(cookievalue);
		
		
		

			//System.out.println("Constructing packet for session_write");
			System.out.println("I am forwarding the session to be stored in its backup server's session table.");
			String data =  Integer.toString(rpcClient.getCallID()) + Utils.NETWORK_DELIMITER + Integer.toString(Utils.SESSION_WRITE_OPCODE)+ Utils.NETWORK_DELIMITER + sessionid + Utils.SESSION_DATA_DELIMITER + session_data; 
			String readFromClient = null;

			for(String backupserver : tempBackupServers){
				if(!backupserver.equalsIgnoreCase(Utils.SERVER_ID_NULL)){
					System.out.println("Sending to backup server: "+ backupserver);
					System.out.println("Sending data : "+ data);
					readFromClient = rpcClient.ForwardToServer(backupserver, data);

					if(readFromClient != null ){
						System.out.println("Backup server "+ backupserver + "returned some response. Add it to actual backup servers list");
						foundBackupServers.add(backupserver);
					}
					else{
						System.out.println("Backup Server failed to reply.");
						System.out.println("Setting the server to down.");
						String viewtuple = backupserver + Utils.VIEW_DELIMITER + "DOWN" + Utils.VIEW_DELIMITER + System.currentTimeMillis();
						ArrayList<String> tempview = new ArrayList<>();
						tempview.add(viewtuple);
						ArrayList<String> myview = viewTable.getViewTable();
						viewTable.mergeView(tempview, myview);
					}
				}
			}
		
		return foundBackupServers;
		
	}
	

	



	private boolean checkIfLocalSameAsServersInCookie(String serverID, ArrayList<String> resilient_servers) {
		// TODO Auto-generated method stub
		
		if(serverID == null || resilient_servers == null)
			return false;
		
		for(String server : resilient_servers){
			if(server.contentEquals(serverID))
				return true;
		}
		
		return false;
	}

	

}
