/**
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.elevation;

/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 * Enumeration which classifies way points within an elevation profile.
 */
public enum ElevationWayPointKind {
	Plain, 				// Simple way point (equal to no or low slope)
	Highlighted,			// Highlighted waypoint
	StartPoint,			// First way point
	EndPoint,			// Last way point
	MaxElevation,			// Highest way point
	MinElevation,			// Lowest way point 
	ElevationGainHigh,		// Elevation gain (high slope 15-25%)
	ElevationLossHigh,		// Elevation loss (high downward slope)
	ElevationGainLow,		// Elevation gain (low slope, 5-14.9%)
	ElevationLossLow,		// Elevation loss (low downward slope)
	ElevationLevelGain,		// Elevation level gain (e. g. crossed 300m from lower elevation)
	ElevationLevelLoss,		// Elevation level (e. g. crossed 300m from higher elevation)
	FullHour			// Full Hour	
}
