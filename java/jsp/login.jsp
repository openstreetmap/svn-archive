<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page import="org.openstreetmap.server.osmServerHandler" %>

<%@ page contentType="text/html" %>
<html>

<%@ include file="include/top.jsp" %>
<%

String action = request.getParameter("action");

if( action == null)
{
  %>
    <h1>Login:</h1><br>
    <form action="http://www.openstreetmap.org/api/login.jsp">
    <table>
    <tr><td>email address:</td><td><input type="text" name="email"></td></tr>
    <tr><td>password:</td><td><input type="password" name="pass"></td></tr>
    <tr><td></td><td><input type="submit" value="Go!"></td></tr>
    </table>
    <input type="hidden" name="action" value="login">
    </form>

    <%
}
else
{

  if( action.equals("login")
      && pass != null
      && email != null)
  {

    osmServerHandler osmSH = new osmServerHandler();

    String token = osmSH.login(email, pass);

    if( token.equals("ERROR") )
    {
      %> login failed, sorry <%
    }
    else
    {
      String cookieName = "openstreetmap";
      Date now = new Date();
      String timestamp = now.toString();
      Cookie cookie = new Cookie (cookieName, token);
      cookie.setMaxAge(10 * 60);
      response.addCookie(cookie);
      %>Login success!<%
    }
  }
}

out.flush();

%>

<%@ include file="include/bottom.jsp" %>

</html>
