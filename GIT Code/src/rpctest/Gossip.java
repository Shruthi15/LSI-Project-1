/**
 * 
 */
package rpctest;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author karteeka
 *
 */
public class Gossip {
	
	public static void gossip(){
		for(int i =0; i< Welcome.ViewTable.getViewTable().size(); i++){
			String temp = Welcome.ViewTable.getViewTable().get(i);
			System.out.println("Inside gossip view tuple is: "+ temp );
			String[] serverid = temp.split(Pattern.quote(Utils.VIEW_DELIMITER));
			System.out.println("Inside gossip : server is id: "+ serverid[0]);
			if(serverid[0].equalsIgnoreCase(Welcome.serveridlocal)){
				gossipWithSimpleDB();
			}
			else{
				gossipWithServer(serverid[0]);
			}
		}
		
	}

	public static void gossipWithSimpleDB(){
		
		 	
	        //get views from SimpleDB
	        ArrayList<String> viewListFromSimpleSDB = Welcome.simpleDbManager.getView();
	       
	       
	        if(viewListFromSimpleSDB == null){
	        	System.out.println("Simple DB view list is empty");
	        	return;
	        }
	        else{
	        	System.out.println("Printing view list from SimpleDB");
	        	for(String token : viewListFromSimpleSDB){
	        		System.out.println(" Viewtuple: " + token);
	        	}
	        }
	        //merge my viewTable and viewListFromSimpleTable and update view table with the merged view
	        ArrayList<String> myViewTable = Welcome.ViewTable.getViewTable();
	        //System.out.println("my view table as a string: " + myViewTableAsString);
	        ArrayList<String> mergedView = Welcome.ViewTable.mergeView(myViewTable, viewListFromSimpleSDB);
	        System.out.println("My view Table: "+ Welcome.ViewTable.getViewTable());
	        System.out.println("Merged view : "+ mergedView);
	        //put back the merged view to SimpleDB
	        Welcome.simpleDbManager.putMergedView(Welcome.ViewTable.getViewTable());
	                
		
		
	}
	
	public static void gossipWithServer(String server){
		RPCClient rpcclient = new RPCClient();
		int OPCODE = Utils.GOSSIP_OPCODE;
		int callid = rpcclient.getCallID();
		
		
		String myViewTable = null;
		for(int i=0; i<Welcome.ViewTable.getViewTable().size(); i++){
			myViewTable = Welcome.ViewTable.getViewTable().get(i) + Utils.SIMPLE_DB_DELIMITER;
		}
		
		String data = Integer.toString(callid) + Utils.NETWORK_DELIMITER + Integer.toString(OPCODE) + Utils.NETWORK_DELIMITER + myViewTable;
		
		String readFromClient = rpcclient.ForwardToServer(server, data);
		//Construct session Data from the value returned by rpcClient
		if(readFromClient != null ){
			String[] temp = readFromClient.split(Utils.NETWORK_DELIMITER);
			String[] viewlist = temp[1].split(Utils.SIMPLE_DB_DELIMITER);
			
			ArrayList<String> updateMyViewList = new ArrayList<>();
			for(int i=0; i<viewlist.length;i++)
				updateMyViewList.add(viewlist[i]);
			
			ArrayList<String> myview = Welcome.ViewTable.getViewTable() ;
			myview = updateMyViewList;
			Welcome.ViewTable.setViewTable(myview);
		}
		else{
			//loop through my view table, find server and get the full tuple
			//set it to down and update the view table
			
			ArrayList<String>MyViewTable = Welcome.ViewTable.getViewTable();
			
			for(int i=0; i<MyViewTable.size(); i++){
				String[] pairs = MyViewTable.get(i).split(Utils.VIEW_DELIMITER);
				if(pairs[0].equalsIgnoreCase(server)){
					pairs[1] = "DOWN";
					String tempViewTuple = pairs[0] + Utils.VIEW_DELIMITER + pairs[1] + Utils.VIEW_DELIMITER + pairs[2];
					MyViewTable.set(i, tempViewTuple);
				}
			}
			
			Welcome.ViewTable.setViewTable(MyViewTable);
		}
	}
	
}
