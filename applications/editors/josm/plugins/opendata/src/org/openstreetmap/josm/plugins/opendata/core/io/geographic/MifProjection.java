// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

/**
 * MapInfo Interchange File (MIF) projections, based on this specification:<ul>
 * <li><a href="https://github.com/tricycle/electrodrive-market-analysis/blob/master/specifications/Mapinfo_Mif.pdf">Mapinfo_Mif.pdf</a></li>
 * </ul>
 * This file has been stored in reference directory to avoid future dead links.
 */
public enum MifProjection {
    Albers_Equal_Area_Conic(9, "aea"),
    Azimuthal_Equidistant_polar_aspect_only(5, "aeqd"),
    Cylindrical_Equal_Area(2, "cea"),
    Eckert_IV(14, "eck4"),
    Eckert_VI(15, "eck6"),
    Equidistant_Conic_also_known_as_Simple_Conic(6, "eqdc"),
    Gall(17, "gall"),
    Hotine_Oblique_Mercator(7, "omerc"),
    Lambert_Azimuthal_Equal_Area_polar_aspect_only(4, "laea"),
    Lambert_Conformal_Conic(3, "lcc"),
    Lambert_Conformal_Conic_modified_for_Belgium_1972(19, "lcca"),
    Longitude_Latitude(1, "lonlat"),
    Mercator(10, "merc"),
    Miller_Cylindrical(11, "mill"),
    New_Zealand_Map_Grid(18, "nzmg"),
    Mollweide(13, "moll"),
    Polyconic(27, "poly"),
    Regional_Mercator(26, "merc"),
    Robinson(12, "robin"),
    Sinusoidal(16, "sinu"),
    Stereographic(20, "stere"),
    Swiss_Oblique_Mercator(25, "somerc"),
    Transverse_Mercator_also_known_as_Gauss_Kruger(8, "tmerc"),
    Transverse_Mercator_modified_for_Danish_System_34_Jylland_Fyn(21, "tmerc"),
    Transverse_Mercator_modified_for_Sjaelland(22, "tmerc"),
    Transverse_Mercator_modified_for_Danish_System_45_Bornholm(23, "tmerc"),
    Transverse_Mercator_modified_for_Finnish_KKJ(24, "tmerc");

    private final Integer code;
    private final String proj4id;

    MifProjection(Integer code, String proj4id) {
        this.code = code;
        this.proj4id = proj4id;
    }

    public final Integer getCode() {
        return code;
    }

    /**
     * Replies the Proj.4 identifier.
     *
     * @return The Proj.4 identifier (as reported by cs2cs -lp).
     * If no id exists, return {@code null}.
     */
    public final String getProj4Id() {
        return proj4id;
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
