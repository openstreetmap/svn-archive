<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.apache.xmlrpc.*" %>
<%@ page import="org.openstreetmap.server.osmServerHandler" %>

<%@ page contentType="text/html" %>
<%@ include file="include/top.jsp" %>

<div id="main_area">

<%

String action = request.getParameter("action");
String email = request.getParameter("email");
String pass = request.getParameter("pass");

if( action == null)
{
  %>
    <h1>Login:</h1><br>
    Please login or <a href="newUser.jsp">create an account</a>.<br>
    <form action="/edit/login.jsp">
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
      String sCookieName = "openstreetmap";
      Date now = new Date();
      String timestamp = now.toString();
      Cookie cookie = new Cookie (sCookieName, token);
      cookie.setMaxAge(10 * 60);
      response.addCookie(cookie);
      bLoggedIn = true;
      %>Login success!<%
    }
  }
}

out.flush();

%>
</div>
<%@ include file="include/bottom.jsp" %>
