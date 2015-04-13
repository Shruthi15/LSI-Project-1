<%@page import="java.util.regex.Pattern"%>
<%@page import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title> CS 5300 ALSI PROJ1</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<h1>${message}</h1>

	<form action = "Welcome" method="get">

	<br><br><br><input type="submit" name="Replace"  value = "Replace"/>
	<input type="text" name="replacetext" maxlength="256" />
	<br><input type="submit" name="Refresh" value = "Refresh"/>
	<br><input type="submit" name="Logout"  value = "Logout"/>
</form>
</head>

<p1>Cookie Value SessionID_VersionNum_locationmetadata:     ${cookievalue} </p1>
<br><br><p2> Session Expiration time :  ${sessionexptime} </p2>
<br><br><p5> Session Discard time :  ${discardtime} </p5>
<br><br><p3> My Server ID : ${serverid} </p3>
<br><br><p4> Fetched session from : ${fetchserver} </p4>

<br><br>My View Table:<br>
<%  if(request.getAttribute("myviewtable")!=null){
ArrayList<String> list = (ArrayList<String>) request.getAttribute("myviewtable");
%>
<table>
<%for(int i=0;i<list.size();i++)
{
		
		String temp[]=list.get(i).split(Pattern.quote("|"));
		if(temp[1].equalsIgnoreCase("up"))
		{
			 %><tr bgcolor="green">
			 <th><%=temp[0] %></th>
			  <th><%=temp[1] %></th>
			  <th><%=temp[2] %></th>
			  </tr>  <%
		}
		else
		{
			%><tr bgcolor="red">
			 <th><%=temp[0] %></th>
			  <th><%=temp[1] %></th>
			  <th><%=temp[2]%></th>
			  </tr>  
			  
	<%
		}
 }	}%>
 </table>
 
 <br><br><br>Session Table: <br>
<%  if(request.getAttribute("sessiontable")!=null){
	Map<String,String> temphash = new HashMap<String,String>();
	temphash=(HashMap<String,String>)request.getAttribute("sessiontable");
%>
<table>	
<% for(Map.Entry<String,String> entry : temphash.entrySet()){
	String key = entry.getKey();
	String value = entry.getValue();
	String[] temp = value.split("#");
	
	%><tr>
	<td><%=key %></td>
	 <td><%=temp[0] %></td>
	  <td><%=temp[1] %></td>
	  <td><%=temp[2] %></td>
	 
	  </tr>  
	  <% 
}
}
%>
</table>
<body>

</body>
</html>