<%@ page import="org.openstreetmap.server.*"%>
<%@ page contentType="text/html" %>
<%@ include file="include/top.jsp" %>
<div id="main_area">
<applet code=org/openstreetmap/applet/osmApplet.class
archive="osm.jar , openmap.jar , xmlrpc-applet.jar , commons-codec-1.1.jar"
width=600 height=600>
</applet>
</div>
<%@ include file="include/bottom.jsp" %>
