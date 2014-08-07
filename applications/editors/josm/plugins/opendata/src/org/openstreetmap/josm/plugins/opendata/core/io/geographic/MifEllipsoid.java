// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.opendata.core.io.geographic;

/**
 * MapInfo Interchange File (MIF) ellipsoids, based on this specification:<ul>
 * <li><a href="https://github.com/tricycle/electrodrive-market-analysis/blob/master/specifications/Mapinfo_Mif.pdf">Mapinfo_Mif.pdf</a></li>
 * </ul>
 * This file has been stored in reference directory to avoid future dead links.
 */
public enum MifEllipsoid {
	Airy,
	Modified_Airy,
	Australian_National,
	Bessel,
	Bessel_1841,
	Clarke_1866,
	Modified_Clarke_1866,
	Clarke_1880,
	Modified_Clarke_1880,
	Everest,
	Modified_Fischer_1960,
	Helmert_1906,
	Hough,
	Modified_Everest,
	GRS_67,
	GRS_80,
	International,
	Krassovsky,
	South_American_1969,
	WGS_60,
	WGS_66,
	WGS_72,
	WGS_84
}
