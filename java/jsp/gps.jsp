<html>
<head>
<title>OpenStreetMap</title>
</head>
<body>
<%@ page import="java.sql.*" %>

Enter a text note below:<br><Br>

<FORM method="GET" action="gps.jsp">
<INPUT type="text" name="txt" size="30" maxlength="255">
<INPUT type="submit" value="Add text note!">
</form>
<%

String txt = request.getParameter("txt");

double x = 0;
double y = 0;
long timestamp = 0;
try {
  Class.forName("com.mysql.jdbc.Driver").newInstance();
  java.sql.Connection conn;
  conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1/openstreetmap","openstreetmap","openstreetmap");
  Statement stmt = conn.createStatement();

  if( txt != null && !txt.equals(""))
  {
    stmt.execute("insert into tempNotes values ('" + txt + "'," + System.currentTimeMillis()+ ")");

  }

  ResultSet rs = stmt.executeQuery("select x(g),y(g),timestamp from tempPoints order by timestamp desc limit 1");


  rs.next();

  x = rs.getDouble(1);
  y = rs.getDouble(2);
  timestamp = rs.getLong(3);


  stmt.close();
  conn.close();


} catch (Exception e) {
}  





%>
<br>
Last recorded point was:
<table>
<tr>
  <td align="right">
    <b>Lat:</b>
  </td>
  <td>
    <%= x %>
  </td>
<tr>
  <td align="right">
    <b>Lon:</b>
  </td>
  <td>
    <%= y %>
  </td>
<tr>
  <td align="right">
    <b>Time:</b>
  </td>
  <td>
    <%=new java.util.Date(timestamp)%>
  </td>
</tr>
</table>


</body>
</html>
