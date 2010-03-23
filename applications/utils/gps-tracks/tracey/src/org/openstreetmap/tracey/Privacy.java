package org.openstreetmap.tracey;

/** An emuneration representing all valid values for a GPS trace's privacy setting at openstreetmap.org.
 *
 * @author Jonathan Bennett
 */

public enum Privacy {

	PRIVATE,
	TRACKABLE,
	IDENTIFIABLE;

	public String getValue() {
		return this.toString().toLowerCase();
	}
}
