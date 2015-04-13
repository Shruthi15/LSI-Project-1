/**
 * 
 */
package rpctest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
/**
 * @author karteeka
 *
 */
@SuppressWarnings("unused")
public class SimpleDBManager {

	/**
	 * @param args
	 */
	private HashMap<String, String>SimpleDBView = new HashMap<>();
	public static AmazonSimpleDB sdb;
	public static String myDomain = "CS5300_PROJECT1B";
	public static String serverViewTuple = "Server View Tuple";
	
	
	
	public SimpleDBManager(){
 
		
	}
	
	public void init() throws FileNotFoundException, IllegalArgumentException, IOException
	{
		String secretKey="";
		String	accessKey = "";
		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		sdb = new AmazonSimpleDBClient(awsCredentials);
		sdb.createDomain(new CreateDomainRequest(myDomain));
	}

	
	public ArrayList<String> getView(){
		ArrayList<String> viewList = new ArrayList<>(); 
				String view = null;
		
		String selectExpression = "select * from " + myDomain;
        System.out.println("Selecting: " + selectExpression + "\n");
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        for (Item item : sdb.select(selectRequest).getItems()) {
            //System.out.println("  Item");
            System.out.println("    Name: " + item.getName());
            for (Attribute attribute : item.getAttributes()) {
                System.out.println("      Attribute");
                System.out.println("        Name:  " + attribute.getName());
                System.out.println("        Value: " + attribute.getValue());
                view = attribute.getValue();
                if(view == null)
                	return null;
                else
                	viewList.add(view);
                
            }
        }
		
		return viewList;
		
	}
	
	public void putView(String viewTuple, String IP){
		if(IP == null)
			System.out.println("Ayyo IP is null!");
		if(sdb == null)
			System.out.println("SDB itself is null");
		
		try{
			ReplaceableAttribute replaceableAttribute = new ReplaceableAttribute()
		.withName(serverViewTuple)
        .withValue(viewTuple)
        .withReplace(true);
			
		sdb.putAttributes(new PutAttributesRequest()
        .withDomainName(myDomain)
        .withItemName(IP)
        .withAttributes(replaceableAttribute));
	}catch(NullPointerException e){
		e.printStackTrace();
	}
		
	}

	public void putMergedView(ArrayList<String> set) {
		
		if(set == null)
			return;
		//Merged Views Look Like ServerID|Status|Time$ServerID|Status|Time
		String[] temp;
		for(String token : set){
			temp = token.split(Pattern.quote(Utils.VIEW_DELIMITER));
			putView(token, temp[0]);
		}
		
		
	}
}
