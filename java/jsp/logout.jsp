<%@ page import="java.util.*"%>
<%@ page import="org.openstreetmap.server.*"%>
<%@ page contentType="text/html" %>
<%@ include file="include/top.jsp" %>
<div id="main_area">

<%

String sCookieName = "openstreetmap";
Date now = new Date();
String timestamp = now.toString();
Cookie cookie = new Cookie (sCookieName, "loggedOut");
cookie.setMaxAge(10 * 60);
response.addCookie(cookie);

bLoggedIn = false;
%>
You've been logged out.

</div>
<%@ include file="include/bottom.jsp" %>
