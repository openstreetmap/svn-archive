//License: GPL. For details, see README file.

package org.openstreetmap.josm.plugins.epsg31287;

import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Epsg31287 extends Plugin {
    public Epsg31287(PluginInformation info) {
        super(info);
        Projections.addProjection(new ProjectionEPSG31287());
    }
}
