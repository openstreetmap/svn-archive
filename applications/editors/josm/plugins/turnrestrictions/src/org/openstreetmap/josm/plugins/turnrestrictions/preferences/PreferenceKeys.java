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
	
	/**
	 * Indicates whether the Basic Editor should include a widget for for displaying
	 * and editing the via-objects of a turn restriction.
	 * 
	 * Supported values are:
	 * <ul>
	 *   <li><tt>true</tt> - display the list of vias in the basic editor </li>
	 *    <li><tt>false</tt> - don't display the list of vias in the basic editor </li>
	 * </ul>
	 */
	String SHOW_VIAS_IN_BASIC_EDITOR = "turnrestrictions.show-vias-in-basic-editor";
}
