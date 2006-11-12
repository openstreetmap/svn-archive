package landsat;

import java.io.IOException;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapView;

public class OSGBLayer extends WMSLayer {

	public OSGBLayer(String constURL) {
		super(constURL);
	}

	public void grab(double minlat,double minlon,double maxlat,double maxlon)
	throws IOException {
		MapView mv = Main.map.mapView;
		OSGBImage npeImage = new OSGBImage(url);
		npeImage.grab(mv,minlat,minlon,maxlat,maxlon);
		wmsImages.add(npeImage);
	}
}
