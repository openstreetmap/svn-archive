// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.buildings_tools;

import javax.swing.JMenu;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.Projections;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class BuildingsToolsPlugin extends Plugin {
    public static final Projection MERCATOR = Projections.getProjectionByCode("EPSG:3857"); // Mercator

    public static EastNorth latlon2eastNorth(LatLon p) {
        return MERCATOR.latlon2eastNorth(p);
    }

    public static LatLon eastNorth2latlon(EastNorth p) {
        return MERCATOR.eastNorth2latlon(p);
    }

    public BuildingsToolsPlugin(PluginInformation info) {
        super(info);
        JMenu dataMenu = MainApplication.getMenu().dataMenu;
        MainMenu.add(dataMenu, new BuildingSizeAction());
        MainMenu.add(dataMenu, new BuildingCircleAction());
        MainMenu.add(dataMenu, new BuildingRectangleAction());
        MainMenu.add(dataMenu, new MergeAddrPointsAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            MainApplication.getMap().addMapMode(new IconToggleButton(new DrawBuildingAction()));
        }
    }
}
