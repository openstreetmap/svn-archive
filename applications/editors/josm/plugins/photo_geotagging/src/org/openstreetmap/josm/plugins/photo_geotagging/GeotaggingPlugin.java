//License: GPL (v2 or above)
package org.openstreetmap.josm.plugins.photo_geotagging;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;

import javax.swing.JMenuItem;

import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.geoimage.GeoImageLayer;
import org.openstreetmap.josm.gui.layer.geoimage.ImageEntry;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * This plugin is used to write latitude and longitude information
 * to the EXIF header of jpg files.
 * It extends the core geoimage feature of JOSM by adding a new entry
 * to the right click menu of any image layer.
 *
 * The real work (writing lat/lon values to file) is done by the pure Java
 * sanselan library.
 */
public class GeotaggingPlugin extends Plugin {
    public GeotaggingPlugin(PluginInformation info) {
        super(info);
        GeoImageLayer.registerMenuAddition(new GeotaggingMenuAddition());
    }

    /**
     * Adds a menu entry to the right click menu of each geoimage layer.
     */
    class GeotaggingMenuAddition implements GeoImageLayer.LayerMenuAddition {
        public Component getComponent(Layer layer) {
            JMenuItem geotaggingItem = new JMenuItem(tr("Write coordinates to image header"), ImageProvider.get("geotagging"));;
            geotaggingItem.addActionListener(new GeotaggingAction((GeoImageLayer) layer));
            geotaggingItem.setEnabled(enabled((GeoImageLayer) layer));
            return geotaggingItem;
        }

        /**
         * Check if there is any suitable image.
         */
        private boolean enabled(GeoImageLayer layer) {
            for (ImageEntry e : layer.getImages()) {
                if (e.getPos() != null && e.getGpsTime() != null)
                    return true;
            }
            return false;
        }
    }
}
