<html>
<head>
  <link rel="stylesheet" type="text/css" href="/css/style.css" />
  <title>OpenStreetMap</title>
</head>
<body>

<%@ include file="loggedin.jsp" %>

<div id="top_bar">
  <img src="/images/user.png">
<%
 
if(bLoggedIn)
{
  %><a href="logout.jsp">logout</a><%
}
else
{
  %><a href="login.jsp">Login</a> or <a href="newUser.jsp">create an account</a><%
}
%>
  
</div>

<div id="logo">
  <h1>OpenStreetMap</h1>
  <img src="/images/mag_map_medium.png" width="150" height="150">
</div>

<div id="left_menu">
  <h1><img src="/images/toolbox.png">Toolbox:</h1>
<%
if(bLoggedIn)
{
  %>
  <ul>
    <li>
      <img src="/images/stock_edit-16.png"><a href="applet.jsp">Edit map</a>
    </li>
    <li>
      <img src="/images/stock_save-16.png"><a href="uploadGPX.jsp">Upload GPX</a>
    </li>
    <li>
      <img src="/images/stock_help-16.png"><a href="/wiki/">Help</a>
    </li>
    <li>
      <img src="/images/stock_stop-16.png"><a href="/bugzilla/">Report a bug</a>
    </li>
    
  </ul>
  <%
}
else
{
  %>
  You need to log in to use the tools :-)
  <%
}
%>
</div>

<div id="bottom_bar">
  <a href="http://creativecommons.org/licenses/by-sa/2.0/">
  <img src="/images/somerights20.png" border="0"></a><br>
  This site is licensed under a <a href="http://creativecommons.org/licenses/by-sa/2.0/">Creative Commons License</a>
      
</div>


