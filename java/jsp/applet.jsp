<%@ page import="org.openstreetmap.server.*"%>
<%@ page contentType="text/html" %>
<%@ include file="include/top.jsp" %>
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



%>


<div id="main_area">
<applet
code   = "org/openstreetmap/applet/osmApplet.class"
archive= "osm.jar , openmap.jar , xmlrpc-applet.jar , commons-codec-1.1.jar"
width  = "600"
height = "600"
>
<param name="lat" value = "<%=fLat>">
<param name="lon" value = "<%=fLon>">
<param name="scale" value = "<%=fScale>">
</applet>
</div>
<%@ include file="include/bottom.jsp" %>
