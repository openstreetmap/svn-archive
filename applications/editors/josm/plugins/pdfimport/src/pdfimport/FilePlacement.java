package pdfimport;

import java.util.Properties;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;

public class FilePlacement {
	public Projection projection = null;
	public double minX = 0;
	public double maxX = 1;
	public double minY = 0;
	public double maxY = 1;

	public double minEast = 0;
	public double maxEast = 10000;
	public double minNorth = 0;
	public double maxNorth = 10000;

	public void setPdfBounds(double minX, double minY, double maxX, double maxY){
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public void setEastNorthBounds(double minEast, double minNorth, double maxEast, double maxNorth) {
		this.minEast = minEast;
		this.maxEast = maxEast;
		this.minNorth = minNorth;
		this.maxNorth = maxNorth;
	}

	public Properties toProperties() {
		Properties p = new Properties();
		if (projection != null) {
			p.setProperty("Projection", projection.getClass().getCanonicalName());
		}

		p.setProperty("minX", Double.toString(minX));
		p.setProperty("maxX", Double.toString(maxX));
		p.setProperty("minY", Double.toString(minY));
		p.setProperty("maxY", Double.toString(maxY));
		p.setProperty("minEast", Double.toString(minEast));
		p.setProperty("maxEast", Double.toString(maxEast));
		p.setProperty("minNorth", Double.toString(minNorth));
		p.setProperty("maxNorth", Double.toString(maxNorth));

		return p;
	}

	public void fromProperties(Properties p){

		String className = p.getProperty("Projection", null);
		projection = null;

		if (className != null) {
			for(Projection proj: Projection.allProjections){
				if (proj.getClass().getCanonicalName().equals(className)){
					projection = proj;
					break;
				}
			}
		}

		minX = parseProperty(p, "minX", minX);
		maxX = parseProperty(p, "maxX", maxX);
		minY = parseProperty(p, "minY", minY);
		maxY = parseProperty(p, "maxY", maxY);

		minEast = parseProperty(p, "minEast", minEast);
		maxEast = parseProperty(p, "maxEast", maxEast);
		minNorth = parseProperty(p, "minNorth", minNorth);
		maxNorth = parseProperty(p, "maxNorth", maxNorth);
	}

	private double parseProperty(Properties p, String name, double defaultValue){
		if (!p.containsKey(name)) {
			return defaultValue;
		}

		String value = p.getProperty(name);

		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			return defaultValue;
		}

	}

	public LatLon tranformCoords(double x, double y) {

		if (this.projection == null){
			return new LatLon(y/1000, x/1000);
		}
		else{
			x = (x - this.minX) * (this.maxEast - this.minEast) / (this.maxX - this.minX)  + this.minEast;
			y = (y - this.minY) * (this.maxNorth - this.minNorth) /  (this.maxY - this.minY) + this.minNorth;
			return this.projection.eastNorth2latlon(new EastNorth(x, y));
		}
	}

	public EastNorth reverseTransform(LatLon coor) {
		if (this.projection == null){
			return new EastNorth(coor.lon() * 1000, coor.lat() * 1000);
		}
		else{
			return null;
		}
	}

}
