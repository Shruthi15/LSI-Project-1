package rpctest;
/**
 * 
 */


import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * @author karteeka
 *
 */
@SuppressWarnings("unused")
public class SessionData {
	private static int global_unique_number = 1;
	private String sessionID;
	private int versionNum;
	private String sessionState;
	private Date expirationTimeStamp;
	
	public SessionData(){
		
	}
	
	
	public SessionData(String dEFAULT_MSG, String localIPAddress, int i,
			Date expirationtime) {
		// TODO Auto-generated constructor stub
		this.versionNum = i;
		this.expirationTimeStamp = expirationtime;
		String uniqueNumber = Integer.toString(incremnetSessionNum());
		this.sessionID = uniqueNumber+"-"+localIPAddress;
		this.sessionState = dEFAULT_MSG;
	}
	public int incremnetSessionNum(){
		return this.global_unique_number ++;
	}
	public String getSessionID(){
		return this.sessionID;
	}
	
	public int getVersionNum(){
		return this.versionNum;
	}
	
	public String getSessionState(){
		return this.sessionState;
	}
	
	public Date getExpTimeStamp(){
		return this.expirationTimeStamp;
	}
		
	public void setSessionState(String msg){
		this.sessionState = msg;
	}
	
	public void setVersionNum(int version){
		this.versionNum = version;
	}
	
	public void setSessionID(String sessionString){
		this.sessionID = sessionString;
	}

	public void incrementVersionNum(){
		int version = getVersionNum();
		this.versionNum = version + 1;
	}
	public void setExpirationTime(Date expiration){
		this.expirationTimeStamp = expiration;
	}
	
	
}
