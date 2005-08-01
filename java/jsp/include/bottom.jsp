
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

  <ul>
    <li>
      <img src="/images/stock_edit-16.png"><a href="viewMap.jsp">View map</a>
    </li>
<%
if(bLoggedIn)
{
  %>
    <li>
      <img src="/images/icon_key.png"><a href="editKeys.jsp">Edit keys</a>
    </li>
    <li>
      <img src="/images/stock_save-16.png"><a href="uploadGPX.jsp">Upload GPX</a>
    </li>
    <%
}
%>
    <li>
      <img src="/images/stock_help-16.png"><a href="/wiki/">Help</a>
    </li>
    <li>
      <img src="/images/stock_stop-16.png"><a href="/bugzilla/">Report a bug</a>
    </li>
    
  </ul>
 
  Internet Explorer has known problems with this site.<br><br>
<%
if(bLoggedIn)
{
  %>
  You need to <a href="login.jsp">log in</a> to use more tools :-)<br><br>
  <%
}

osmSH.closeDatabase();

%>
</div>

</body>
</html>
