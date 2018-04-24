// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pointinfo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.gui.Notification;

public final class PointInfoUtils {

    private PointInfoUtils() {
        // Hide default constructor for utilities classes
    }

    /**
     * Show notification.
     * @param message Message to shown.
     * @param type Type if message (info, warning, error, plain).
     */
    public static void showNotification(String message, String type) {
        Notification note = new Notification(message);

        if ("info".equals(type))
            note.setIcon(JOptionPane.INFORMATION_MESSAGE);
        else if ("warning".equals(type))
            note.setIcon(JOptionPane.WARNING_MESSAGE);
        else if ("error".equals(type))
            note.setIcon(JOptionPane.ERROR_MESSAGE);
        else
            note.setIcon(JOptionPane.PLAIN_MESSAGE);

        note.setDuration(Notification.TIME_SHORT);
        note.show();
    }

    /**
     * Return text representation of coordinates.
     * @param lat the lat part of coordinates
     * @param lon the lon part of coordinates
     * @return String coordinatesText
     */
    public static String formatCoordinates(double lat, double lon) {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' ');
        DecimalFormat df = new DecimalFormat("#.00000", symbols);

        return "(" + df.format(lat) + ", " + df.format(lon) + ")";
    }
}
