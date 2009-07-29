package ch.guggis.josm.bern.servlet;

/**
 * represents a pair of CH1903 coordinates
 * 
 * @see <a
 *      href="http://de.wikipedia.org/wiki/Schweizer_Koordinatensystem">Schweizer
 *      Koordinatensystem(Wikipedia)</a>
 */
public class CH1903Coordinates {

	private double x;
	private double y;

	public CH1903Coordinates() {
		this(0, 0);
	}

	public CH1903Coordinates(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
}
