/**
 * Copyright by Christof Dallermassl
 * This program is free software and licensed under GPL.
 */
package at.dallermassl.josm.plugin.surveyor;

import javax.swing.Action;

/**
 * Definition of constants for actions (as {@link Action#SELECTED_KEY} only exists since java 1.6
 * Followed tutorial at http://www.javalobby.org/java/forums/t53484.html
 * @author cdaller
 *
 */
public final class ActionConstants {
 
	/**
	 * 
	 */
	private ActionConstants() { }

    public static final String SELECTED_KEY = "actionConstants.selected";
}