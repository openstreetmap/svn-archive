// License: GPL. For details, see LICENSE file.
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
