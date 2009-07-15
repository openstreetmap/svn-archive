package org.openstreetmap.josm.plugins.graphview.core.visualisation;

/**
 * immutable representation of a latitude-longitude pair.
 */
public final class LatLonCoords {

	private final double lat;
	private final double lon;

	public LatLonCoords(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

}
