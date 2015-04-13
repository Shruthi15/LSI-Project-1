package rpctest;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Servlet implementation class Welcome
 */
//@WebServlet("/Welcome")
@SuppressWarnings("unused")
public class Welcome extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String COOKIE_ID = "CS5300PROJ1SESSION";
    
	public static SessionManager sessionmanager = new SessionManager();
	
	public static RPCServerUtil rpcManager = new RPCServerUtil(sessionmanager);
	
	public static SessionController sessionController = new SessionController();
	
	public static SimpleDBManager simpleDbManager = new SimpleDBManager();
	
	public static ViewTable ViewTable = new ViewTable();
	
	public static Server_Utils server = new Server_Utils();
	
	public static final String DEFAULT_MSG = "Hello User!";
	
	public static final int SESSION_TIMEOUT_SECS = 60*5;
	
	public static final int COOKIE_MAX_AGE = SESSION_TIMEOUT_SECS;
	
	public static String serveridlocal = null;
	
	public static String viewTuple = null;
	
	
	
	String serverStatus = null;
	String sessionID;
	int versionNum = 0;
	String locationmetadata;
	String sessionstate;
	Date expirationtime = null;
	Date discardTime=null;
	SessionData sessiondata;

	private SessionControllerReturnObject returnobject;

	
	/**
     * @throws IOException 
	 * @throws IllegalArgumentException 
	 * @throws FileNotFoundException 
	 * @see HttpServlet#HttpServlet()
     */
    public Welcome() throws FileNotFoundException, IllegalArgumentException, IOException {
        super();
        RPCServer t = new RPCServer();
		Thread starter = new Thread(t);
		starter.start();
		System.out.println("Starting RPC Server Thread from Welcome.java");
		GossipThread t1 = new GossipThread();
		Thread gossipstarter = new Thread(t1);
		gossipstarter.start();
		CleanUpStarter clean = new CleanUpStarter();
		Thread t2 = new Thread(clean);
		t2.start();
		System.out.println("Starting Gossip Thread from Welcome.java");
		
        try {
			serveridlocal = server.getEC2InstanceIPAddress();
        	//serveridlocal = server.getLocalIPAddress();
        	//serveridlocal = "192.168.0.3";
		} catch (Exception e) {
			
			e.printStackTrace();
		}
        
        serverStatus = "UP";
       // viewTuple = Utils.constructViewTuple(serveridlocal,serverStatus, System.currentTimeMillis() );
        //View_Utils.addMyselfToMyViewTable(ViewTable, viewTuple);
        
        String testviewtuple = Utils.constructViewTuple(serveridlocal, serverStatus, System.currentTimeMillis());
        View_Utils.addMyselfToMyViewTable(ViewTable, testviewtuple);
        
        
        System.out.println("Added view locally to my view Table" + testviewtuple);
        System.out.println("Now Adding myself to SimpleDB");
        
        simpleDbManager.init();
        
        addMySelfToSimpleDB(simpleDbManager, testviewtuple, serveridlocal);
       
        
        //get views from SimpleDB
        ArrayList<String> viewListFromSimpleSDB = simpleDbManager.getView();
       
       
        if(viewListFromSimpleSDB == null)
        	System.out.println("Simple DB view list is empty");
        else{
        	System.out.println("Printing view list from SimpleDB");
        	for(String token : viewListFromSimpleSDB){
        		System.out.println(" Viewtuple: " + token);
        	}
        }
        //merge my viewTable and viewListFromSimpleTable and update view table with the merged view
        ArrayList<String> myViewTable = ViewTable.getViewTable();
        //System.out.println("my view table as a string: " + myViewTableAsString);
        ArrayList<String> mergedView = ViewTable.mergeView(myViewTable, viewListFromSimpleSDB);
        System.out.println("My view Table: "+ ViewTable.getViewTable());
        System.out.println("Merged view : "+ mergedView);
        //put back the merged view to SimpleDB
        simpleDbManager.putMergedView(ViewTable.getViewTable());
                
    }

	private void addMySelfToSimpleDB(SimpleDBManager simpleDbManager2, String viewTuple2, String serveridlocal2) {
		
		System.out.println("Adding viewTuple: "+ viewTuple2);
		System.out.println("Adding this server ID: "+ serveridlocal2);
        simpleDbManager2.putView(viewTuple2, serveridlocal2);
        System.out.println("Added myself to simple DB");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/HTML");
		PrintWriter out = response.getWriter();
		
		
		Cookie[] cookies = request.getCookies();
		String cookieValue = getCookieValue(cookies);
		//SessionData returnobject.sessiondata;
		ArrayList<String>backupServers = new ArrayList<String>();
		String display;
		String backup_Server;
		String replace = request.getParameter("replacetext");
		boolean logout = false;
		returnobject = null;
		
		
		
		//Check cookieValue - if cookieValue is null(it doesn't have our COOKIE_ID) --> first time/cookie expired
		if(cookieValue == null){
			System.out.println("CookieValue doesn't have our COOKIE_ID");
			//check if cookie expired for an existing session --> determining first time access
			if(returnobject == null){
				System.out.println("No previous existing session!");

				/* first time access
				 * create session for him
				 */
				returnobject = new SessionControllerReturnObject();
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, 300);
				expirationtime = cal.getTime();
				cal.add(Calendar.SECOND, 5);
				discardTime=cal.getTime();
				returnobject.sessiondata = new SessionData(DEFAULT_MSG, serveridlocal, 0, discardTime);
				System.out.println("Created new session for user!");
				if(returnobject.sessiondata == null)
					System.out.println("But session data is null! Creation of new session for user failed");

				
				String sessionValues = Integer.toString(returnobject.sessiondata.getVersionNum()) + Utils.SESSION_DATA_DELIMITER + returnobject.sessiondata.getSessionState() + Utils.SESSION_DATA_DELIMITER + Utils.parseExpTimeFromDate(returnobject.sessiondata.getExpTimeStamp());
				sessionmanager.putDataIntoTable(returnobject.sessiondata.getSessionID(), sessionValues );
				System.out.println("Added session Locally");

				
				ArrayList<String> tempBackupServers = View_Utils.findBackupServers(ViewTable.getViewTable(), serveridlocal);
				
				for(String s : tempBackupServers)
					System.out.println("Temporary Backup server: " + s);
				
				System.out.println("Attempt to get resiliency by writing session to these backup servers.");
				SessionData storeSessionData = returnobject.sessiondata;
				
				backupServers = sessionController.storeSession(sessionmanager, rpcManager, tempBackupServers, ViewTable , storeSessionData, serveridlocal, cookieValue);
				
				System.out.println("Number of actual backup servers found: "+ backupServers.size());
				String mycookieValue = Utils.constructFirstTimeCookie(returnobject.sessiondata, backupServers, serveridlocal);
				Cookie newCookie = new Cookie(COOKIE_ID, mycookieValue);
				newCookie.setMaxAge(COOKIE_MAX_AGE);

				display = "Welcome.jsp";
				addToResponse(request, response, newCookie, mycookieValue, returnobject.sessiondata.getSessionState(), returnobject.sessiondata.getExpTimeStamp(), display, serveridlocal, ViewTable.getViewTable(),discardTime, logout, serveridlocal);


			}
			else{
				/*cookie expired for an existing session 
				 * check if that session also expired --> user had pressed logout
				 * if session didn't expire, but cookie expired-->garbage collect that session
				 */
				System.out.println("Cookie expired for an existing session!");
				if(Utils.checkSessionExpired(returnobject.sessiondata.getExpTimeStamp())){
					System.out.println("Session also expired so we remove it from the session Table");
					sessionmanager.removeDataFromTable(returnobject.sessiondata.getSessionID());
					returnobject.sessiondata = null;
					sessionstate = "SESSION HAS TIMED OUT!!!!";
					displaySessionTimeout(request,response);
				}
				else{
					System.out.println("It will be garbage collected later on anyway.");
				}
			}

		}

		else if(cookieValue != null){  //cookieValue is not null---> session is active
			if (request.getParameter("Replace") != null){ 

				try {
					returnobject = sessionController.getSession(rpcManager, sessionmanager, cookieValue, serveridlocal, ViewTable);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(returnobject != null){
					sessionstate = replace;
					returnobject.sessiondata.setSessionState(sessionstate);
					display = "Welcome.jsp";
					returnobject.sessiondata.incrementVersionNum();
					
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND, 300);
					expirationtime = cal.getTime();
					cal.add(Calendar.SECOND, 5);
					discardTime=cal.getTime();
					
					returnobject.sessiondata.setExpirationTime(discardTime);
					String sessionValues = Integer.toString(returnobject.sessiondata.getVersionNum()) + Utils.SESSION_DATA_DELIMITER + returnobject.sessiondata.getSessionState() + Utils.SESSION_DATA_DELIMITER + Utils.parseExpTimeFromDate(returnobject.sessiondata.getExpTimeStamp());
					sessionmanager.putDataIntoTable(returnobject.sessiondata.getSessionID(), sessionValues );
					System.out.println("Added session Locally");

					
					ArrayList<String> tempBackupServers = View_Utils.findBackupServers(ViewTable.getViewTable(), serveridlocal);
					
					for(String s : tempBackupServers)
						System.out.println("Temporary Backup server: " + s);
					
					System.out.println("Attempt to get resiliency by writing session to these backup servers.");
					SessionData storeSessionData = returnobject.sessiondata;

					
					String sessionid = returnobject.sessiondata.getSessionID();
					System.out.println("Session id is:" + sessionid);
					
				
					backupServers = sessionController.storeSession(sessionmanager, rpcManager, tempBackupServers, ViewTable , storeSessionData, serveridlocal, cookieValue);

					System.out.println("Number of actual backup servers found: "+ backupServers.size());
					String mycookieValue = Utils.constructFirstTimeCookie(returnobject.sessiondata, backupServers, serveridlocal);
					Cookie newCookie = new Cookie(COOKIE_ID, mycookieValue);
					System.out.println("inside replace..cookie value is: "+ mycookieValue);
					newCookie.setMaxAge(COOKIE_MAX_AGE);
					response.addCookie(newCookie);
					addToResponse(request, response, newCookie, mycookieValue, sessionstate, expirationtime, display, serveridlocal, ViewTable.getViewTable(), discardTime, false,returnobject.server);
				}
				else{
					display = "Error.jsp";
					displayError(display, request,response);
				}
			}
			 if(request.getParameter("Refresh") != null){

				try {
					returnobject = sessionController.getSession(rpcManager, sessionmanager, cookieValue, serveridlocal, ViewTable);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Inside Refresh\n");
				if(returnobject != null){
					sessionstate = returnobject.sessiondata.getSessionState();
					System.out.println("Session state is: "+sessionstate);
					
					if(sessionstate == null || sessionstate.equals(""))
						sessionstate = DEFAULT_MSG;

					returnobject.sessiondata.incrementVersionNum();
					
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.SECOND, 300);
					expirationtime = cal.getTime();
					cal.add(Calendar.SECOND, 5);
					discardTime=cal.getTime();
					
					returnobject.sessiondata.setExpirationTime(discardTime);
					
					display = "Welcome.jsp";

					String sessionValues = Integer.toString(returnobject.sessiondata.getVersionNum()) + Utils.SESSION_DATA_DELIMITER + returnobject.sessiondata.getSessionState() + Utils.SESSION_DATA_DELIMITER + Utils.parseExpTimeFromDate(returnobject.sessiondata.getExpTimeStamp());
					sessionmanager.putDataIntoTable(returnobject.sessiondata.getSessionID(), sessionValues );
					System.out.println("Inside refresh..Added session Locally");

					
					ArrayList<String> tempBackupServers = View_Utils.findBackupServers(ViewTable.getViewTable(), serveridlocal);
					
					for(String s : tempBackupServers)
						System.out.println("Inside refresh..Temporary Backup server: " + s);
					
					System.out.println("Inside refresh..Attempt to get resiliency by writing session to these backup servers.");
					SessionData storeSessionData = returnobject.sessiondata;

					
					String sessionid = returnobject.sessiondata.getSessionID();
					System.out.println("Inside refresh..Session id is:" + sessionid);
					
				
					backupServers = sessionController.storeSession(sessionmanager, rpcManager, tempBackupServers, ViewTable , storeSessionData, serveridlocal, cookieValue);

					System.out.println("Inside refresh..Number of actual backup servers found: "+ backupServers.size());
					
					String mycookieValue = Utils.constructFirstTimeCookie(returnobject.sessiondata, backupServers, serveridlocal);
					Cookie newCookie = new Cookie(COOKIE_ID, mycookieValue);
					System.out.println("inside refresh..cookie value is: "+ mycookieValue);


					newCookie.setMaxAge(COOKIE_MAX_AGE);
					response.addCookie(newCookie);
					addToResponse(request, response, newCookie, mycookieValue, sessionstate, expirationtime, display, serveridlocal, ViewTable.getViewTable(),discardTime, false, returnobject.server);
				}
				else{
					display = "Error.jsp";
					displayError(display, request, response);
				}

			}
			 if(request.getParameter("Logout")!= null){
				
				//logout = true;
				//if(returnobject.sessiondata != null){
					/* get sessiondata for this sessionID */
				System.out.println("Inside logout");
				
					cookieValue = null;
					Cookie newCookie = new Cookie(COOKIE_ID, cookieValue);
					newCookie.setValue(null);
					newCookie.setMaxAge(0);
					response.addCookie(newCookie);
					
					displaySessionTimeout(request, response);
				
			}
		}
		
	}
			
	

	
	private void displaySessionTimeout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		RequestDispatcher view;
		view = request.getRequestDispatcher("Logout.jsp");
		try {
			view.forward(request, response);
		} catch (ServletException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//view.forward(request, response);
	}

	private void displayError(String display, HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		// TODO Auto-generated method stub
		RequestDispatcher view;
		view = request.getRequestDispatcher(display);
		view.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private void addToResponse(HttpServletRequest request, HttpServletResponse response, Cookie newCookie, String mycookieValue,
			String sessionstate, Date expirationtime, String display, String serverID, ArrayList<String> arrayList, Date discardTime, boolean logout, Object fetchserver) {
		
		RequestDispatcher view;
		if(!logout){
			
			
			request.setAttribute("message", sessionstate);
			request.setAttribute("cookievalue", mycookieValue);
			request.setAttribute("sessionexptime", expirationtime);
			request.setAttribute("discardtime", discardTime);
			request.setAttribute("serverid", serverID);
			request.setAttribute("fetchserver", fetchserver);
			request.setAttribute("myviewtable", arrayList);
			Map<String,String> temphash = new HashMap<>();
			temphash.putAll(sessionmanager.getSessionTable());
			request.setAttribute("sessiontable", temphash);
			
		}
		response.addCookie(newCookie);
		view = request.getRequestDispatcher(display);
		try {
			view.forward(request, response);
		} catch (ServletException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getCookieValue(Cookie[] cookies) {

		String myCookieValue = null;
		if(cookies == null){
			return null;
		}
				
		for(int i = 0; i < cookies.length; i++){
			if(cookies[i].getName().equals(COOKIE_ID)){
				myCookieValue = cookies[i].getValue();
				break;
			}
		}
		return myCookieValue;
	}

	public String getSessionID(Cookie[] cookies){
		if(cookies == null){
			return null;
		}
		
		String sessionID = null, versionNum = null;
		
		for(int i = 0; i < cookies.length; i++){
			if(cookies[i].getName().equals(COOKIE_ID)){
				String[] sessionsplit = cookies[i].getValue().split("_");
				sessionID = sessionsplit[0];
				versionNum = sessionsplit[1];
			}
		}
		
		return sessionID;
		
	}

}
