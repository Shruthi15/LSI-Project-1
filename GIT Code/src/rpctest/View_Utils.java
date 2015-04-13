/**
 * 
 */
package rpctest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;

/**
 * @author karteeka
 *
 */
public class View_Utils {

	public static String parseViewAndGetServerID(String viewTuple ){
		String serverID = "";
		
		String[] viewContent = viewTuple.split("_");
		
		serverID = viewContent[0];
		
		return serverID;
	}
	
	public static String parseViewAndGetStatus(String viewTuple){
		String status = null;
		
		String[] viewContent = viewTuple.split("_");
		
		status = viewContent[1];
		
		return status;
	}
	
	public static long parseViewAndGetTimeStamp(String viewTuple){
		long time;
		String[] viewContent = viewTuple.split("_");
		
		time = Long.parseLong(viewContent[2]);
		
		return time;
	}
	
	public static Set<String> constructSetFromString(String s){
		Set<String> mySet = new HashSet<String>();
		if(s == null){
			System.out.println("String passed is null");
			return null;
		}
		String[] breakString = s.split(Utils.SIMPLE_DB_DELIMITER);
		for(int i=0; i<breakString.length; i++){
			mySet.add(breakString[i]);
		}
		
		return mySet;
	}
	
	public static String constructStringFromSet(Set<String>s){
		
		StringBuilder sb = new StringBuilder();
		for(String token : s){
			sb.append(token);
			sb.append(Utils.SIMPLE_DB_DELIMITER);
		}
		
		return sb.toString();
	}

	public static void addMyselfToMyViewTable(ViewTable viewTable,
			String viewTuple) {
		// TODO Auto-generated method stub
		viewTable.addView(viewTuple);
		
	}

	public static ArrayList<String> findBackupServers(ArrayList<String> viewTable, String serveridlocal) {
		
		ArrayList<String> backupServers = new ArrayList<>();
		
		
		for(int i=0; i< viewTable.size(); i++){
			String[] view_Tuple = viewTable.get(i).split(java.util.regex.Pattern.quote(Utils.VIEW_DELIMITER));
			if((!serveridlocal.equalsIgnoreCase(view_Tuple[0])) && (view_Tuple[1].equalsIgnoreCase("up"))  ){
				backupServers.add(view_Tuple[0]);
			}
			else{
				backupServers.add(Utils.SERVER_ID_NULL);
			}
		}
		
		
		
		// TODO Auto-generated method stub
		return backupServers;
		
		
		
	}
		
		
		
		
	
}
