// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation.gpx;

/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Enumeration which classifies way points within an elevation profile.
 */
public enum ElevationWayPointKind {
    Plain,                 // Simple way point (equal to no or low slope)
    Highlighted,            // Highlighted waypoint
    StartPoint,            // First way point
    EndPoint,            // Last way point
    MaxElevation,            // Highest way point
    MinElevation,            // Lowest way point
    ElevationGainHigh,        // Elevation gain (high slope 15-25%)
    ElevationLossHigh,        // Elevation loss (high downward slope)
    ElevationGainLow,        // Elevation gain (low slope, 5-14.9%)
    ElevationLossLow,        // Elevation loss (low downward slope)
    ElevationLevelGain,        // Elevation level gain (e. g. crossed 300m from lower elevation)
    ElevationLevelLoss,        // Elevation level (e. g. crossed 300m from higher elevation)
    FullHour            // Full Hour
}
