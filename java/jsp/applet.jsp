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
code="org/openstreetmap/processing/OSMApplet.class"
archive="OSMApplet.jar, xmlrpc-2.0-beta.jar, commons-codec-1.3.jar, core.jar"
width="700"
height="500"

>
<param name="clat" value="<%=fLat%>">
<param name="clon" value="<%=fLon%>">
<param name="scale" value="<%=fScale%>">
<param name="token" value="<%=sToken%>">
<param name="wmsurl" value="http://www.openstreetmap.org/tile/0.1/wms?map=/usr/lib/cgi-bin/steve/steve.map&service=WMS&WMTVER=1.0.0&REQUEST=map&STYLES=&TRANSPARENT=TRUE&LAYERS=landsat">

</applet>
</div>
<%@ include file="include/bottom.jsp" %>
