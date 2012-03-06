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

public enum MifProjection {
	Albers_Equal_Area_Conic(9),
	Azimuthal_Equidistant_polar_aspect_only(5),
	Cylindrical_Equal_Area(2),
	Eckert_IV(14),
	Eckert_VI(15),
	Equidistant_Conic_also_known_as_Simple_Conic(6),
	Gall(17),
	Hotine_Oblique_Mercator(7),
	Lambert_Azimuthal_Equal_Area_polar_aspect_only(4),
	Lambert_Conformal_Conic(3),
	Lambert_Conformal_Conic_modified_for_Belgium_1972(19),
	Longitude_Latitude(1),
	Mercator(10),
	Miller_Cylindrical(11),
	New_Zealand_Map_Grid(18),
	Mollweide(13),
	Polyconic(27),
	Regional_Mercator(26),
	Robinson(12),
	Sinusoidal(16),
	Stereographic(20),
	Swiss_Oblique_Mercator(25),
	Transverse_Mercator_also_known_as_Gauss_Kruger(8),
	Transverse_Mercator_modified_for_Danish_System_34_Jylland_Fyn(21),
	Transverse_Mercator_modified_for_Sjaelland(22),
	Transverse_Mercator_modified_for_Danish_System_45_Bornholm(23),
	Transverse_Mercator_modified_for_Finnish_KKJ(24);
	
	private final Integer code;
	private MifProjection(Integer code) {
		this.code = code;
	}
	public final Integer getCode() {
		return code;
	}
	public static MifProjection forCode(Integer code) {
		for (MifProjection p : values()) {
			if (p.getCode().equals(code)) {
				return p;
			}
		}
		return null;
	}
}
