<%@ page import="java.util.*" %>
<%@ page import="java.util.zip.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.openstreetmap.util.*"%>
<%@ page import="org.openstreetmap.server.*"%>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page contentType="text/html" %>

<%@ include file="include/top.jsp" %>


<div id="main_area">
<%

if( !bLoggedIn )
{
  %>
    <%@ include file="include/loginMessage.jsp" %>
  <%
}
else
{

  osmServerHandler osmSH = new osmServerHandler();

  Vector keys = osmSH.getAllKeys(sToken);

  Enumeration e = keys.elements();

  %>
   <table id="keyvalue" border="0" width="100%">
   <tr>
   <th>
   Key name
   </th>
   <th>
   Last edited by
   </th>
   <th>
   Last edited at
   </th>
   </tr>
   <%

  while( e.hasMoreElements() )
  {
    %>
      <tr><td><i>
      <%
    out.print( (String)e.nextElement() );
    %>
      </i></td><td>
     <%
    out.print( (String)e.nextElement() );
        %>
      </td><td>
     <%
    out.print( new Date(Long.parseLong((String)e.nextElement()) ));
    %>
      </td></tr>
     <%


  }
    
    %>
      </table>
     <%



  
}
%>
</div>
<%@ include file="include/bottom.jsp" %>
