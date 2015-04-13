/**
 * 
 */
package rpctest;

import java.util.Random;

/**
 * @author karteeka
 *
 */
public class GossipThread implements Runnable {

	
	private static final int GOSSIP_SECS = 60;
	
	public void run() {
		
		
		
		Random generator = new Random();
		
		
		while (true){
			
			/* gossip with another serve chosen at random */
			Gossip.gossip();
			System.out.println("Inside gossip thread");
			
			try {
				Thread.sleep((GOSSIP_SECS/2) + generator.nextInt(GOSSIP_SECS));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	
	
	
	
	
	

}
