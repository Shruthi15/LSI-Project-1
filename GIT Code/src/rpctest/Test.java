/**
 * 
 */
package rpctest;



/**
 * @author karteeka
 *
 */
public class Test {
	
	public static SessionManager sessionmanager= new SessionManager();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		

		RPCClient rpcclient = new RPCClient();
		sessionmanager.putDataIntoTable("Hi", "1#yobaby#JUN 12, 1989 15:30:48 a");
		
		String serverIP = Server_Utils.getLocalIPAddress();
		System.out.println("My IP" + serverIP);
		
		String data = "1@2@Hi2#1#yobaby#JUN 12, 1989 15:30:48 a";
		
		rpcclient.ForwardToServer(serverIP, data);

	}

}
