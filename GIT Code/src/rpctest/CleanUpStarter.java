/**
 * 
 */
package rpctest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class CleanUpStarter extends Thread {

	
		public void run() {
        	//infinite loop
			System.out.println("inside cleanup");
        	while(true){
        		try {
        			//thread is sleeping for 1 second
					Thread.sleep(1000 * 305);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
        		//once thread wakes up then check and remove stale session from sessionTable
        		removeFromMap(new SessionManager().getSessionTable());
        		System.out.println("cleaned up");
        	}
        }
	
	private static void removeFromMap(ConcurrentHashMap<String, String> sessionTable){
		// TODO Auto-generated method stub
		Iterator<String> values = sessionTable.keySet().iterator();
						while(values.hasNext()) {
							String str = values.next();
							String state = sessionTable.get(str);
							Date expTime = Utils.parseExpTimeFromString(state);
							
							if(state != null &&  expTime.before(new Date())){
								sessionTable.remove(str);
							}
						}
					
		
	}
		
		
	}
