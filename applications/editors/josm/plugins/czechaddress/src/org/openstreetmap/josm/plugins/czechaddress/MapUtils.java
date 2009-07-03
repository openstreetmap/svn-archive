package org.openstreetmap.josm.plugins.czechaddress;

import java.util.Collection;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.AutoScaleAction;
import org.openstreetmap.josm.data.osm.OsmPrimitive;

/**
 * Collection of utilities for manipulating the JOSM map.
 *
 * <p>This set of state-less utilities, which can be handy in all parts of
 * the plugin. Therefore all methods are {@code static} and the class is
 * {@code abstract}.</p>
 *
 * @author Radomír Černoch radomir.cernoch@gmail.com
 */
public abstract class MapUtils {

    /**
     * Selects and zooms the JOSM viewport to given primitives.
     */
    public static void zoomToMany(Collection<OsmPrimitive> primitives) {
        Main.ds.setSelected(primitives);
        (new AutoScaleAction("selection")).actionPerformed(null);
    }

    /**
     * Selects and zooms the JOSM viewport to given primitive.
     */
    public static void zoomTo(OsmPrimitive primitive) {
        Main.ds.setSelected(primitive);
        (new AutoScaleAction("selection")).actionPerformed(null);
    }
}
