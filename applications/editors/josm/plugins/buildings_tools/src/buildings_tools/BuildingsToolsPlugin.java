package buildings_tools;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.projection.Projection;
import org.openstreetmap.josm.data.projection.ProjectionInfo;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

public class BuildingsToolsPlugin extends Plugin {
    public static Projection proj = ProjectionInfo.getProjectionByCode("EPSG:3857"); // Mercator

    public static EastNorth latlon2eastNorth(LatLon p) {
        return proj.latlon2eastNorth(p);
    }

    public static LatLon eastNorth2latlon(EastNorth p) {
        return proj.eastNorth2latlon(p);
    }

    public BuildingsToolsPlugin(PluginInformation info) {
        super(info);
        Main.main.menu.editMenu.addSeparator();
        MainMenu.add(Main.main.menu.editMenu, new BuildingSizeAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if (oldFrame == null && newFrame != null) {
            Main.map.addMapMode(new IconToggleButton(new DrawBuildingAction(Main.map)));
        }
    }
}
