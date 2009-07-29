package ch.guggis.josm.bern.servlet;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class BoundingBox {

	public static final NumberFormat COORD_FORMAT = new DecimalFormat(
			"###0.0000000", new DecimalFormatSymbols(Locale.US));

	static public BoundingBox convertWGS84toCH1903(BoundingBox bbox) {
		BoundingBox ret = new BoundingBox();
		WGS84Coordinates wgs84Coordinates = new WGS84Coordinates(
				bbox.getLeft(), bbox.getBottom());
		CH1903Coordinates c1 = wgs84Coordinates.convertToCHCoordinates();
		wgs84Coordinates = new WGS84Coordinates(bbox.getRight(), bbox.getTop());
		CH1903Coordinates c2 = wgs84Coordinates.convertToCHCoordinates();

		ret.setLeft(c1.getY());
		ret.setBottom(c1.getX());
		ret.setRight(c2.getY());
		ret.setTop(c2.getX());
		return ret;
	}

	private double left;
	private double bottom;
	private double right;
	private double top;

	public BoundingBox() {
	}

	public BoundingBox(double left, double bottom, double right, double top) {
		this.left = left;
		this.bottom = bottom;
		this.right = right;
		this.top = top;
	}

	public void fromString(String bbox) throws IllegalArgumentException,
			NumberFormatException {
		String[] segments = bbox.split(",");
		if (segments == null || segments.length != 4) {
			throw new IllegalArgumentException(
					"unexpected format of 'bbox'. got '" + bbox + "'");
		}
		for (int i = 0; i < 4; i++) {
			try {
				double d = Double.parseDouble(segments[i]);
				switch (i) {
				case 0:
					left = d;
					break;
				case 1:
					bottom = d;
					break;
				case 2:
					right = d;
					break;
				case 3:
					top = d;
					break;
				default: /* should not happen */
				}
			} catch (NumberFormatException e) {
				throw e;
			}
		}
	}

	@Override
	public String toString() {
		String bbox = String.format("BBOX=%s,%s,%s,%s", COORD_FORMAT
				.format(left), COORD_FORMAT.format(bottom), COORD_FORMAT
				.format(right), COORD_FORMAT.format(top));
		return bbox;
	}

	public double getLeft() {
		return left;
	}

	public void setLeft(double left) {
		this.left = left;
	}

	public double getBottom() {
		return bottom;
	}

	public void setBottom(double bottom) {
		this.bottom = bottom;
	}

	public double getRight() {
		return right;
	}

	public void setRight(double right) {
		this.right = right;
	}

	public double getTop() {
		return top;
	}

	public void setTop(double top) {
		this.top = top;
	}

}
