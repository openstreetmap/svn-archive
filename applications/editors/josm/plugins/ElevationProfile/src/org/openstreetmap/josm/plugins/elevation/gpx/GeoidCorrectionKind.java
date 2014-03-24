// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gpx;

/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Enumeration for available elevation correction modes.
 */
public enum GeoidCorrectionKind {
    /** Elevation values remain unchanged */
    None,
    /** Automatic correction by geoid lookup table */
    Auto,
    /** Fixed value */
    Fixed
}
