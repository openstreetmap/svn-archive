<%@ page import="org.openstreetmap.test.makeImage"%>
<%@ page import="org.openstreetmap.server.osmServerHandler"%>
<%@ page import="javax.imageio.*"%>
<%@ page import="java.io.*"%>
<%@ page import="java.awt.image.*"%>
<%@ page contentType="image/png" %><%

float scale = Float.parseFloat( request.getParameter("scale") );
float lat = Float.parseFloat( request.getParameter("lat") );
float lon = Float.parseFloat( request.getParameter("lon") );

int w,h;

try {
	w = Integer.parseInt( request.getParameter("width") );
	h = Integer.parseInt( request.getParameter("height") );
	if (w <= 0) {
		w = makeImage.DEFAULT_WIDTH;
	}
	if (h <= 0) {
		h = makeImage.DEFAULT_HEIGHT;
	}
}
catch (Exception e) {
	w = makeImage.DEFAULT_WIDTH;
	h = makeImage.DEFAULT_HEIGHT;
}

makeImage mi = new makeImage();

BufferedImage bi = mi.getImageFromCoord(w,h,lat,lon,scale);

OutputStream os = response.getOutputStream();
ImageIO.write(bi, "png", os);
os.close();


%>
