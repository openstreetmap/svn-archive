<%@ page import="java.util.*"%>
<%@ page import="org.openstreetmap.server.*"%>
<%@ page import="com.bbn.openmap.*"%>
<%@ page import="com.bbn.openmap.proj.*"%>
<%@ page contentType="text/html" %>
<%@ include file="include/top.jsp" %>

<div id="main_area">

<!--

This code is getting increasingly horrible and hacky. Don't worry about it.
We will replace it all with nice ruby at some point soon.

-->


<%
String lat = request.getParameter("lat");
String lon = request.getParameter("lon");
String sScale = request.getParameter("scale");
String from = request.getParameter("from");
String searchTerm = request.getParameter("searchTerm");
boolean bSuccessParsing = false;

double clon = 0;
double clat = 0;
double scale = 0;

if(from != null && from.equals("frontpage"))
{

  try
  {
  //user came from frontpage, assume lat lon and try and figure it out

    if( searchTerm.indexOf(' ') != -1 )
    {
      StringTokenizer st = new StringTokenizer( searchTerm );
      clat = Double.parseDouble(st.nextToken());
      clon = Double.parseDouble(st.nextToken());
      scale = 0.0001;
      bSuccessParsing = true;
    }


  }
  catch(Exception e)
  {
    scale = 0.001;
    clat = 51.526447f;
    clon = -0.14746371f;

  }
}
else
{
  if(lat == null || lon == null || sScale == null)
  {

    scale = 0.001;
    clat = 51.526447;
    clon = -0.14746371;

  }
  else
  {
    try
    {
      // try and parse the stuff
      scale = Double.parseDouble(sScale);
      clat = Double.parseDouble(lat);
      clon = Double.parseDouble(lon);
      bSuccessParsing = true;

    }
    catch(Exception e)
    {
      scale = 0.001;
      clat = 51.526447;
      clon = -0.14746371;


    }
  }

}



String sURL = "/map/map.png?lat=" + clat + "&lon=" + clon + "&scale=" +scale;
String sAppletURL = "/edit/applet.jsp?lat=" + clat + "&lon=" + clon + "&scale=10404.917";

double width = 700;
double height  = 500;

double dlon = width / 2 * scale;
double dlat = height / 2 * scale * Math.cos(clat * Math.PI / 180);


String sLeftURL = getURL(scale,clat, clon - dlon);
String sRightURL = getURL(scale,clat, clon + dlon);
String sUpURL = getURL(scale,clat + dlat, clon);
String sDownURL = getURL(scale,clat - dlat, clon);
String sZoominURL = getURL(scale / 1.5 ,clat, clon);
String sZoomoutURL = getURL(scale * 1.5 ,clat, clon);


String sLandsatURL = "http://tile.openstreetmap.org/cgi-bin/steve/mapserv?map=/usr/lib/cgi-bin/steve/steve.map&service=WMS&WMTVER=1.0.0&REQUEST=map&STYLES=&TRANSPARENT=TRUE&LAYERS=landsat&width=" + 700 
  + "&height=" + 500
  + "&bbox="
  + (clon - dlon ) + ","
  + (clat - dlat ) + ","
  + (clon + dlon ) + ","
  + (clat + dlat );
%>
<%!
private String getURL(double dScale, double dLat, double dLon)
{
  return "viewMap.jsp?lat=" + dLat + "&lon=" + dLon + "&scale=" +dScale;

}
%>

<!-- FIXME: do all this with css -->

<div id="mapToolbar">
<table border="0">
<tr><td>

<table border="0">
<tr>
<td colspan="2" align="center">

<a href="<%=sUpURL%>">
<img src="/images/map_up.png" border="0">
</a>
</td>
</tr>
<tr>
<td>
<a href="<%=sLeftURL%>">
<img src="/images/map_left.png" border="0">
</a>
</td>
<td>

<a href="<%=sRightURL%>">
<img src="/images/map_right.png" border="0">
</a>
</td>
</tr>
<tr>
<td colspan="2" align="center">

<a href="<%=sDownURL%>">
<img src="/images/map_down.png" border="0">
</a>
</td>
</tr>
</table>

</td>
<td>

<a href="<%=sZoominURL%>"><img src="/images/map_zoomin.png" border="0"></a>
<br><br>
<a href="<%=sZoomoutURL%>"><img src="/images/map_zoomout.png" border="0"></a>
</td>

</td>
</table>
</div>

<div id="mapToolbarRight">

<% if(bLoggedIn)
{
%>
<img src="/images/stock_edit.png" border="0"><a href="<%=sAppletURL%>">edit this map...</a>
<%
} else {
%>
Log in to edit map
<%
}
%>


</div>

<div id="mapImage">

 <div style="position: absolute; left: 0px; top: 0px; height: 500px; width: 700
 px; padding: 1em;">
 
  <img src="<%=sLandsatURL%>" width="700" height="500" alt="Your map">

 </div>

<div style="position: absolute; left: 0px; top: 0px; height: 500px; width: 700
 px; padding: 1em;">
 
  <img src="<%=sURL%>" width="700" height="500" alt="Your map">

 </div>


 
<div style="position: absolute; left: 0px; top: 500px; height: 20px; width: 700
 px; padding: 1em;">
Latitude=<%=clat%>, Longitude=<%=clon%>, Scale=<%=scale%><br>
<%
if( !bSuccessParsing )
{
  %>
    Sorry, I didn't understand that lattitude and longitude, defaulting to some values in London.

    <%

}
%>

</div>
</div>



</div>
<%@ include file="include/bottom.jsp" %>
