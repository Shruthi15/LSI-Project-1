package rpctest;
/**
 * 
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author karteeka
 * get server's IP address
 * which is server
 *
 */
public class Server_Utils {

	/**
	 * @param args
	 */
	private String serverID;
	
	/* Reference : http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html */
	public String getEC2InstanceIPAddress() throws Exception{
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("/opt/aws/bin/ec2-metadata --public-ipv4");
		InputStream IO = proc.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(IO));
		String line = reader.readLine();
		String values[] = line.split(":");
		this.serverID = values[1].trim();
		String ipAddress = this.serverID;
		return ipAddress;
		
	}
	
	/* Reference : http://stackoverflow.com/questions/8083479/java-getting-my-ip-address */
	public static String getLocalIPAddress(){
		String ip = null;
		 try {
		        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		        while (interfaces.hasMoreElements()) {
		            NetworkInterface iface = interfaces.nextElement();
		            // filters out 127.0.0.1 and inactive interfaces
		            if (iface.isLoopback() || !iface.isUp())
		                continue;

		            Enumeration<InetAddress> addresses = iface.getInetAddresses();
		            while(addresses.hasMoreElements()) {
		                InetAddress addr = addresses.nextElement();
		                ip = addr.getHostAddress();
		                
		            }
		        }
		    } catch (SocketException e) {
		        throw new RuntimeException(e);
		    }
		 //ipAddress = ip;
		 return ip;
			
		}
	

	
}
