<%@ page import="org.openstreetmap.test.makeImage"%><%@ page import="org.openstreetmap.server.osmServerHandler"%><%@ page import="javax.imageio.*"%><%@ page import="java.io.*"%><%@ page import="java.awt.image.*"%><%@ page contentType="image/png" %><%

float scale = Float.parseFloat( request.getParameter("scale") );
float lat = Float.parseFloat( request.getParameter("lat") );
float lon = Float.parseFloat( request.getParameter("lon") );


makeImage mi = new makeImage();

BufferedImage bi = mi.getImageFromCoord(lat,lon,scale);

OutputStream os = response.getOutputStream();
ImageIO.write(bi, "png", os);
os.close();


%>
