// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

public enum MifUnit {
	Centimeters(6),
	Chains(31),
	Feet(3),
	Inches(2),
	Kilometers(1),
	Links(30),
	Meters(7),
	Miles(0),
	Millimeters(5),
	Nautical_Miles(9),
	Rods(32),
	US_Survey_Feet(8),
	Yards(4);
		
	private final Integer code;
	private MifUnit(Integer code) {
		this.code = code;
	}
	public final Integer getCode() {
		return code;
	}
	public static MifUnit forCode(Integer code) {
		for (MifUnit p : values()) {
			if (p.getCode().equals(code)) {
				return p;
			}
		}
		return null;
	}
}
