<%@ page import="org.openstreetmap.server.*"%>
<%@ page import="com.bbn.openmap.*"%>
<%@ page import="com.bbn.openmap.proj.*"%>
<%@ page contentType="text/html" %>
<%@ include file="include/top.jsp" %>
<div id="main_area">

<%
String lat = request.getParameter("lat");
String lon = request.getParameter("lon");
String scale = request.getParameter("scale");

float fLon = 0;
float fLat = 0;
float fScale = 0;

if(lat == null || lon == null || scale == null)
{

  fScale = 10404.917f;
  fLat = 51.526447f;
  fLon = -0.14746371f;

}
else
{
  try
  {
    // try and parse the stuff
    fScale = Float.parseFloat(scale);
    fLat = Float.parseFloat(lat);
    fLon = Float.parseFloat(lon);

  }
  catch(Exception e)
  {
    fScale = 10404.917f;
    fLat = 51.526447f;
    fLon = -0.14746371f;


  }



}

MapBean mb = new MapBean();
mb.setScale(fScale);
mb.setCenter(fLat, fLon);

Projection p = mb.getProjection();

float p1lat = p.getUpperLeft().getLatitude();
float p1lon = p.getUpperLeft().getLongitude();

float p2lat = p.getLowerRight().getLatitude();
float p2lon = p.getLowerRight().getLongitude();

String sURL = "viewMapImage.jsp?lat=" + fLat + "&lon=" + fLon + "&scale=" +fScale;
String sAppletURL = "applet.jsp?lat=" + fLat + "&lon=" + fLon + "&scale=" +fScale;

String sLeftURL = getURL(fScale,fLat, fLon - (p2lon - p1lon)/4);
String sRightURL = getURL(fScale,fLat, fLon + (p2lon - p1lon)/4);
String sUpURL = getURL(fScale,fLat + (p1lat - p2lat)/4, fLon);
String sDownURL = getURL(fScale,fLat - (p1lat - p2lat)/4, fLon);
String sZoominURL = getURL(fScale / 1.5f ,fLat, fLon);
String sZoomoutURL = getURL(fScale * 1.5f ,fLat, fLon);



%>
<%!
private String getURL(float fScale, float fLat, float fLon)
{
  return "viewMap.jsp?lat=" + fLat + "&lon=" + fLon + "&scale=" +fScale;

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
<img src="/images/stock_edit.png" border="0"><a href="<%=sAppletURL%>">edit this map...</a>

</div>

<div id="mapImage">
<img src="<%=sURL%>" width="600" height="600" alt="Your map">

</div>



</div>
<%@ include file="include/bottom.jsp" %>
