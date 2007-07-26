/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Segment;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.dialogs.ToggleDialog;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * Plugin that allows navigation in josm
 * 
 * @author cdaller
 * 
 */
public class NavigatorPlugin extends Plugin {
    private NavigatorLayer navigatorLayer;
    private NavigatorModel navigatorModel;

    /**
     * 
     */
    public NavigatorPlugin() {
        super();
        
        navigatorModel = new NavigatorModel();
        navigatorLayer = new NavigatorLayer(tr("Navigation"));
        navigatorLayer.setNavigatorNodeModel(navigatorModel);
        
        JMenuBar menu = Main.main.menu;
        JMenu navigatorMenu = new JMenu(tr("Navigation"));
        JMenuItem navigatorMenuItem = new JMenuItem(new NavigatorAction(this));
        navigatorMenu.add(navigatorMenuItem);
        JMenuItem resetMenuItem = new JMenuItem(tr("Reset Graph"));
        resetMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                navigatorModel.resetGraph();
            }
        });
        navigatorMenu.add(resetMenuItem);
        menu.add(navigatorMenu);
    }
    
    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.Plugin#mapFrameInitialized(org.openstreetmap.josm.gui.MapFrame, org.openstreetmap.josm.gui.MapFrame)
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if(newFrame != null) {
            newFrame.toolBarActions.add(
                new IconToggleButton(new NavigatorModeAction(newFrame, navigatorModel, navigatorLayer)));
        }
    }

    /**
     * @param startNode
     * @param endNode
     */
    public void navigate() {
        navigatorLayer.navigate();
    }
}
