// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.Notification;

public abstract class PointInfoUtils {

    /**
     * Show notification.
     * @param message Message to shown.
     * @param type Type if message (info, warning, error, plain).
     */
    public static void showNotification(String message, String type) {
        Notification note = new Notification(message);

        if (type.equals("info"))
            note.setIcon(JOptionPane.INFORMATION_MESSAGE);
        else if (type.equals("warning"))
            note.setIcon(JOptionPane.WARNING_MESSAGE);
        else if (type.equals("error"))
            note.setIcon(JOptionPane.ERROR_MESSAGE);
        else
            note.setIcon(JOptionPane.PLAIN_MESSAGE);

        note.setDuration(Notification.TIME_SHORT);
        note.show();
    }

    /**
     * Return text representation of coordinates.
     # @param  lat Lat coordinate
     # @param  lon Lon coordinate
     * @return String coordinatesText
     */
    public static String formatCoordinates(double lat, double lon) {

        String r = "";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#.00000", symbols);

        r = "(" + df.format(lat) + ", " + df.format(lon) + ")";
        return r;
    }
}
