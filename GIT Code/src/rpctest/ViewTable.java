/**
 * 
 */
package rpctest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author karteeka
 *TODO : REMOVE ViewData class as it is not needed for sets
 */
public class ViewTable {

	private ArrayList<String> viewTable;
	
	
	public ViewTable(){
	this.viewTable = new ArrayList<String>();	
		
	}
	public ArrayList<String> getViewTable(){
		return this.viewTable;
	}
	
	public void setViewTable(ArrayList<String>viewTable){
		
		this.viewTable = viewTable;
	}
	public void addView(String viewTuple){
		
		this.viewTable.add(viewTuple);
	}
	
	public void removeView(String viewString){
		
		this.viewTable.remove(viewString);
	}
	
	public ArrayList<String> mergeView(ArrayList<String> view1, ArrayList<String> view2){
		
		
		ArrayList<String>mergedView = new ArrayList<>();
		String[]  temp2, temp3;
		for(String token : view1){
			boolean flag = false;
			String[] temp1 = token.split(Pattern.quote(Utils.VIEW_DELIMITER));  //temp1[0] = IP, temp1[1] = up/down, temp1[2] = timestamp
			 for(String token2 : view2){
				 temp2 = token2.split(Pattern.quote(Utils.VIEW_DELIMITER));
				 if(temp1[0].equals( temp2[0])){
					 if(Long.parseLong(temp1[2]) >= Long.parseLong(temp2[2])){
						 mergedView.add(token);
						 flag=true;
					 }
					 else{
						 mergedView.add(token2);
					 }
				 }
			 }
			 if(!flag)
			 mergedView.add(token);
			
		}
		ArrayList<String> temp = new ArrayList<>();
		temp = mergedView;
		
		for(int i=0; i<view2.size(); i++){
			boolean flag = false;
			temp2 = view2.get(i).split(Pattern.quote(Utils.VIEW_DELIMITER));
			for(int j=0; j<mergedView.size(); j++){
				temp3=mergedView.get(j).split(Pattern.quote(Utils.VIEW_DELIMITER));
				if(temp2[0].equals(temp3[0])){    //temp2[0] = IP, temp3[0] = IP
					flag = true;
					//break;
			}

		}
			if(!flag)
			mergedView.add(view2.get(i));
		}
		this.viewTable = mergedView;
		return mergedView;
		}
	
	
}
