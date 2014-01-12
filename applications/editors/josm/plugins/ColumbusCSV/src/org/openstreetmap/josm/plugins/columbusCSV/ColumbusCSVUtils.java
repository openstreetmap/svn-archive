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
package org.openstreetmap.josm.plugins.columbusCSV;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;

public class ColumbusCSVUtils {
	/**
	 * Private constructor for the utility class.
	 */
	private ColumbusCSVUtils() {
		
	}
	
	/**
	 * Shows an error message.
	 * @param txt Message to show.
	 */
	public static void showErrorMessage(String txt) {
		showMessage(txt, tr("Error"), JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Shows an information message.
	 * @param txt Message to show.
	 */
	public static void showInfoMessage(String txt) {
		showMessage(txt, tr("Information"), JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Shows a warning message.
	 * @param txt Message to show.
	 */
	public static void showWarningMessage(String txt) {
		showMessage(txt, tr("Warning"), JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Generic method to show a message.
	 * @param txt Message to show
	 * @param caption Title of message box
	 * @param icon Icon to show (question, warning,...)
	 */
	public static void showMessage(String txt, String caption, int icon) {
		if (isStringNullOrEmpty(txt)) return;
		
		JOptionPane.showMessageDialog(Main.parent, tr(txt), caption, icon);
	}
	
	/**
	 * Check, if a string is either null or empty.
	 * 
	 * @param txt
	 *            . The text to check for.
	 * @return True, if given text is either null or empty.
	 */
	public static boolean isStringNullOrEmpty(String txt) {
		return txt == null || txt.length() == 0;
	}
	
	/**
	 * Parses a float number from a string.
	 * @param txt
	 * @return The corresponding float instance or Float.NaN, if txt was empty or contained an invalid float number.
	 */
	public static Float floatFromString(String txt) {
		Float f;
		
		if (isStringNullOrEmpty(txt)) return Float.NaN;
		
		try {
			f = Float.parseFloat(txt);
		} catch (NumberFormatException nex) {
			f = Float.NaN;
		}
		return f;
	}	
}
