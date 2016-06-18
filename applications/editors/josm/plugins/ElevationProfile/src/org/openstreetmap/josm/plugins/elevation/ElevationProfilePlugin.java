// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.elevation;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.plugins.elevation.actions.AddElevationLayerAction;
import org.openstreetmap.josm.plugins.elevation.gui.ElevationProfileDialog;
import org.openstreetmap.josm.plugins.elevation.gui.ElevationProfileLayer;

/**
 * Plugin class for displaying an elevation profile of the tracks.
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */
public class ElevationProfilePlugin extends Plugin {

    private static ElevationProfileLayer currentLayer;

    /**
     * Initializes the plugin.
     * @param info Context information about the plugin.
     */
    public ElevationProfilePlugin(PluginInformation info) {
        super(info);

        createColorMaps();

        // TODO: Disable this view as long as it is not stable
        MainMenu.add(Main.main.menu.imagerySubMenu, new AddElevationLayerAction(), false, 0);
    }

    /**
     * Called after Main.mapFrame is initialized. (After the first data is loaded).
     * You can use this callback to tweak the newFrame to your needs, as example install
     * an alternative Painter.
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        super.mapFrameInitialized(oldFrame, newFrame);

        if (newFrame != null) {
            ElevationMapMode eleMode = new ElevationMapMode("Elevation profile", newFrame);
            newFrame.addMapMode(new IconToggleButton(eleMode));
            ElevationProfileDialog eleProfileDlg = new ElevationProfileDialog();
            eleProfileDlg.addModelListener(eleMode);
            eleProfileDlg.setProfileLayer(getCurrentLayer());
            newFrame.addToggleDialog(eleProfileDlg);
        }
    }

    /**
     * Gets the elevation profile layer which decorates the current layer
     * with some markers.
     * @return
     */
    public static ElevationProfileLayer getCurrentLayer(){
        if(currentLayer == null){
            currentLayer = new ElevationProfileLayer(tr("Elevation Profile"));
            Main.main.addLayer(currentLayer);
        }
        return currentLayer;
    }

    private void createColorMaps() {
        // Data taken from http://proceedings.esri.com/library/userconf/proc98/proceed/to850/pap842/p842.htm
        ColorMap.create("Physical_US",
                new Color[]{
                        new Color(18,129,242),
                        new Color(113,153,89),
                        new Color(117,170,101),
                        new Color(149,190,113),
                        new Color(178,214,117),
                        new Color(202,226,149),
                        new Color(222,238,161),
                        new Color(242,238,161),
                        new Color(238,222,153),
                        new Color(242,206,133),
                        new Color(234,182,129),
                        new Color(218,157,121),
                        new Color(194,141,125),
                        new Color(214,157,145),
                        new Color(226,174,165),
                        new Color(222,186,182),
                        new Color(238,198,210),
                        new Color(255,206,226),
                        new Color(250,218,234),
                        new Color(255,222,230),
                        new Color(255,230,242),
                        new Color(255,242,255)
        },
                // elevation in meters - the page above uses feet, so these values differs slightly
                new int[]{
                        -3000,
                        0,
                        150,
                        300,
                        450,
                        600,
                        750,
                        900,
                        1050,
                        1200,
                        1350,
                        1500,
                        1650,
                        1800,
                        1950,
                        2100,
                        2250,
                        2400,
                        2550,
                        2700,
                        2750,
                        3000
        }
                );
    }
}
