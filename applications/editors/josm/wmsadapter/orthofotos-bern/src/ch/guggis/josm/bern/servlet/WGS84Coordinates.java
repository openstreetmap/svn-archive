package ch.guggis.josm.bern.servlet;

/**
 * represents geodetic coordinates according to the World Geodetic System 1984.
 * Latitude and longitude are in decimal degrees.
 * 
 */

public class WGS84Coordinates {

	private double lon;
	private double lat;

	public WGS84Coordinates() {
	}

	public WGS84Coordinates(double lon, double lat) {
		this.lon = lon;
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	/**
	 * converts WGS84 coordinates to CH coordinates
	 * 
	 * @return CH coordinates
	 * @see http://de.wikipedia.org/wiki/Schweizer_Koordinatensystem#
	 *      Umrechnung_WGS84_auf_CH1903
	 */
	public CH1903Coordinates convertToCHCoordinates() {

		double phi = 3600d * this.lat;
		double lambda = 3600d * this.lon;

		double phiprime = (phi - 169028.66d) / 10000d;
		double lambdaprime = (lambda - 26782.5d) / 10000d;

		// precompute squares for lambdaprime and phiprime
		//
		double lambdaprime_2 = Math.pow(lambdaprime, 2);
		double phiprime_2 = Math.pow(phiprime, 2);

		double north = 200147.07d + 308807.95d * phiprime + 3745.25d
				* lambdaprime_2 + 76.63d * phiprime_2 - 194.56d * lambdaprime_2
				* phiprime + 119.79d * Math.pow(phiprime, 3);

		double east = 600072.37d + 211455.93d * lambdaprime - 10938.51d
				* lambdaprime * phiprime - 0.36d * lambdaprime * phiprime_2
				- 44.54d * Math.pow(lambdaprime, 3);

		CH1903Coordinates chCoord = new CH1903Coordinates(north, east);

		return chCoord;
	}

}
