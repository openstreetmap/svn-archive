// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.graphview.plugin.layer;

import java.awt.Color;

import org.openstreetmap.josm.plugins.graphview.core.graph.GraphNode;
import org.openstreetmap.josm.plugins.graphview.core.transition.Segment;
import org.openstreetmap.josm.plugins.graphview.core.visualisation.ColorScheme;
import org.openstreetmap.josm.plugins.graphview.plugin.preferences.GraphViewPreferences;

/**
 * color scheme using node and segment colors from preferences
 */
public class PreferencesColorScheme implements ColorScheme {

    private final GraphViewPreferences preferences;

    public PreferencesColorScheme(GraphViewPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public Color getNodeColor(GraphNode node) {
        return preferences.getNodeColor();
    }

    @Override
    public Color getSegmentColor(Segment segment) {
        return preferences.getSegmentColor();
    }

}
