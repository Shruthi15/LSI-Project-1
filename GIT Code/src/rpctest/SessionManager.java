package rpctest;
/**
 * 
 */


import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;



/**
 * @author karteeka
 * Each server's sessionTable is managed here
 */
public class SessionManager {
	
	private ConcurrentHashMap<String, String> sessionTable= new ConcurrentHashMap<String, String>();

	public void putDataIntoTable(String SessionId, String SessionData){
		this.getSessionTable().put(SessionId, SessionData);		
	}
	
	public String getDataFromTable(String sessionID){
		return this.getSessionTable().get(sessionID);
	}

	public void removeDataFromTable(String sessionID) {
		// TODO Auto-generated method stub
		this.getSessionTable().remove(sessionID);
	}

	public ConcurrentHashMap<String, String> getSessionTable() {
		return this.sessionTable;
	}

	public void setSessionTable(ConcurrentHashMap<String, String> sessionTable) {
		this.sessionTable = sessionTable;
	}
	

	
	//TODO: Store session with replication
	
	
}
