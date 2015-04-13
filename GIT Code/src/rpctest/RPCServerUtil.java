package rpctest;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

@SuppressWarnings("unused")
public class RPCServerUtil {
	
	private static SessionManager sessionmanager;
	
	public RPCServerUtil(SessionManager sessionmanager2) {
		// TODO Auto-generated constructor stub
		sessionmanager = sessionmanager2;
	}
	
	public static String readContent(String[] dataContent)
	{
		String callid = dataContent[0];
		String sessionID = dataContent[2];
		
		String response=null;
		String sessionValues= sessionmanager.getDataFromTable(sessionID);
		
		
		if(sessionValues!=null){
		
			String[] Array = sessionValues.split(Utils.SESSION_DATA_DELIMITER);
			response=callid+Utils.NETWORK_DELIMITER+ sessionID + Utils.SESSION_DATA_DELIMITER+ Array[0]+Utils.SESSION_DATA_DELIMITER+Array[1]+Utils.SESSION_DATA_DELIMITER+Array[2];
			System.out.println("Server data: "+ sessionID+Utils.SESSION_DATA_DELIMITER+ Array[0]+Utils.SESSION_DATA_DELIMITER+Array[1]+Utils.SESSION_DATA_DELIMITER+Array[2]);
		}
		return response;
	}
	
	public static String writeContent(String[] dataContent)
	{
		String callid = dataContent[0];
		String packetdata = dataContent[2];
		String response = null;
		String[] pairs = packetdata.split(Utils.SESSION_DATA_DELIMITER);
		String sessionid = pairs[0];
		String version = pairs[1];
		String message = pairs[2];
		String expirationTime = pairs[3];
		
		String sessionData = version+Utils.SESSION_DATA_DELIMITER + message+Utils.SESSION_DATA_DELIMITER+expirationTime;
		System.out.println("Server data: "+sessionid + Utils.SESSION_DATA_DELIMITER + version+Utils.SESSION_DATA_DELIMITER+message+Utils.SESSION_DATA_DELIMITER+expirationTime);
		sessionmanager.putDataIntoTable(sessionid, sessionData);
		System.out.println("Session write successful!");
		response=callid + Utils.NETWORK_DELIMITER;
		return response;
	}
	
	public static String ExchangeViews(String[] packetData) {
		// TODO Auto-generated method stub
		
		//Input is a view encoded in string format : CALLID@OPCODE@Viewstring
		String callid = packetData[0];
		String ViewString = packetData[2];
		
		ArrayList<String> receivedViewList = new ArrayList<>();
		
		String[] viewTuples = ViewString.split(Utils.SIMPLE_DB_DELIMITER);
		
		for(int i=0; i<viewTuples.length; i++)
			receivedViewList.add(viewTuples[i]);
		
		ArrayList<String> myViewList = new ArrayList<>();
		
		myViewList = Welcome.ViewTable.getViewTable();
		
		//merge both views
		
		ArrayList<String> mergedViewList = new ArrayList<>();
		mergedViewList	=Welcome.ViewTable.mergeView(receivedViewList, myViewList);
		
		String returnMergedViewList = null;
		returnMergedViewList = mergedViewList.get(0);
		for(int i=1; i< mergedViewList.size(); i++){
			returnMergedViewList =returnMergedViewList+Utils.SIMPLE_DB_DELIMITER+ mergedViewList.get(i);
		}
		String response = null;
		response = callid + Utils.NETWORK_DELIMITER + returnMergedViewList;
		return response;
		
		
		
		
		
	}

}
