// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.columbusCSV;

import static org.openstreetmap.josm.tools.I18n.tr;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;

/**
 * Utility functions. 
 * @author Oliver Wieland <oliver.wieland@online.de>
 * 
 */
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
