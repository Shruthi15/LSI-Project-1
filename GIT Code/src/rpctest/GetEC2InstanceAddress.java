/**
 * 
 */
package rpctest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author karteeka
 *
 */
public class GetEC2InstanceAddress implements ServletContextListener{

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		try {
			//Welcome.serveridlocal = Welcome.server.getEC2InstanceIPAddress();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
