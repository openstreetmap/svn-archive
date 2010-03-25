package org.openstreetmap.josm.plugins.turnrestrictions.preferences;

/**
 * Defines the preference keys used for preferences of the turnrestrictions
 * plugin 
 *
 */
public interface PreferenceKeys {
	/**
	 * Indicates which of two sets of road sign icons to use. Supported
	 * values are:
	 * <ul>
	 *   <li><tt>set-a</tt> - the set of icons in the directory <tt>/images/types/set-a</tt></li>
	 *   <li><tt>set-b</tt> - the set of icons in the directory <tt>/images/types/set-b</tt></li>
	 * </ul>
	 * 
	 */
	String ROAD_SIGNS = "turnrestrictions.road-signs";
}
