// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

/**
 * MapInfo Interchange File (MIF) ellipsoids, based on this specification:<ul>
 * <li><a href="https://github.com/tricycle/electrodrive-market-analysis/blob/master/specifications/Mapinfo_Mif.pdf">Mapinfo_Mif.pdf</a></li>
 * </ul>
 * This file has been stored in reference directory to avoid future dead links.
 */
public enum MifEllipsoid {
    AIRY,
    MODIFIED_AIRY,
    AUSTRALIAN_NATIONAL,
    BESSEL,
    BESSEL_1841,
    CLARKE_1866,
    MODIFIED_CLARKE_1866,
    CLARKE_1880,
    MODIFIED_CLARKE_1880,
    EVEREST,
    MODIFIED_FISCHER_1960,
    HELMERT_1906,
    HOUGH,
    MODIFIED_EVEREST,
    GRS_67,
    GRS_80,
    INTERNATIONAL,
    KRASSOVSKY,
    SOUTH_AMERICAN_1969,
    WGS_60,
    WGS_66,
    WGS_72,
    WGS_84
}
