//License: GPL. For details, see README file.

package org.openstreetmap.josm.plugins.proj4j;

import org.openstreetmap.josm.gui.preferences.projection.ProjectionPreference;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class Proj4J extends Plugin {
    public Proj4J(PluginInformation info) {
        super(info);
        ProjectionPreference.registerProjectionChoice(new Proj4JProjectionChoice());
    }
}
