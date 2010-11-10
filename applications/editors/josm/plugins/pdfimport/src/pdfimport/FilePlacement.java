package pdfimport;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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
	
	private AffineTransform transform;


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

	
	public String prepareTransform()
	{
		if (this.minX >= this.maxX){
			return tr("Transform error: Min X must be smaller than max");
		}

		if (this.minY >= this.maxY){
			return tr("Transform error: Min Y must be smaller than max");
		}
		
		
		if (this.minEast < this.maxEast && this.minNorth < this.maxNorth) {
			//no rotation
			this.transform = new AffineTransform();
			this.transform.translate(-this.minX, -this.minY);
			this.transform.scale(
					(this.maxEast - this.minEast) / (this.maxX - this.minX),
					(this.maxNorth - this.minNorth) /  (this.maxY - this.minY));
			this.transform.translate(this.minEast, this.minNorth);
		} else if (this.minEast < this.maxEast && this.minNorth > this.maxNorth) {
			//need to rotate 90 degrees counterclockwise
			//transform to 0..1, 0..1 range
			this.transform.translate(-this.minX, -this.minY);
			this.transform.scale(1/(this.maxX - this.minX), 1/(this.minY - this.maxY));
			
			//rotate -90 degs around center
			this.transform.quadrantRotate(-1,  0.5, 0.5);
			
			//transform back to target range
			this.transform.scale(
					(this.maxEast - this.minEast),
					(this.minNorth - this.maxNorth));
			this.transform.translate(this.minEast, this.maxNorth);			
		} else if (this.minEast > this.maxEast && this.minNorth < this.maxNorth) {
			//need to rotate 90 degrees clockwise
			//transform to 0..1, 0..1 range
			this.transform.translate(-this.minX, -this.minY);
			this.transform.scale(1/(this.maxX - this.minX), 1/(this.maxY - this.minY));
			
			//rotate 90 degs around center
			this.transform.quadrantRotate(1, 0.5, 0.5);
			
			//transform back to target range
			this.transform.scale(
					(this.minEast - this.maxEast),
					(this.maxNorth - this.minNorth));
			this.transform.translate(this.maxEast, this.minNorth);			
		}		
		else
		{
			return tr("Transform error: Unsupported orientation");
		}
		
		return null;
			
	}	
	
	EastNorth en = new EastNorth(0, 0);
	Point2D src = new Point2D.Double();

	public LatLon tranformCoords(Point2D pt) {

		if (this.projection == null){
			return new LatLon(pt.getY() / 1000, pt.getX() / 1000);
		}
		else{
			this.transform.transform(pt, en);
			return this.projection.eastNorth2latlon(en);
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
