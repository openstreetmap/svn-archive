<%@ page import="java.io.*" %>
<%@ page import="org.apache.commons.fileupload.*" %>
<%@ page contentType="text/html" %>

<%!


%>

<html>

<%

String action = request.getParameter("action");

if( action == null )
{

  %>
  
    <h1>Upload a GPX File</h1>
    <br><br>
    <form action="http://www.openstreetmap.org/api/uploadGPX.jsp">
    <table>
    <tr><td>email address:</td><td><input type="text" name="email"></td></tr>
    <tr><td>password:</td><td><input type="password" name="pass1"></td></tr>
    <tr><td>file:</td><td><input type="file" name="file"></td></tr>
    <tr><td></td><td><input type="submit" value="Go!"></td></tr>
    </table>
    <input type="hidden" name="action" value="send">

    </form>
    
    
  <%


}




%>
