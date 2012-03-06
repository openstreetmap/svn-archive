//    JOSM opendata plugin.
//    Copyright (C) 2011-2012 Don-vip
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
