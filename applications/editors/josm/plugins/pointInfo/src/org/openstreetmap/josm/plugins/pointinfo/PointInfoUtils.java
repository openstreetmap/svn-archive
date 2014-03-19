/**
 *  PointInfo - plugin for JOSM
 *  Marian Kyral
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

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
    public static void showNotification (String message, String type) {
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
    public static String formatCoordinates (double lat, double lon) {

      String r = "";
      DecimalFormatSymbols symbols = new DecimalFormatSymbols();
      symbols.setDecimalSeparator('.');
      symbols.setGroupingSeparator(' ');
      DecimalFormat df = new DecimalFormat("#.00000", symbols);

      r = "(" + df.format(lat) + ", " +
                df.format(lon) + ")";
      return r;
    }

}
