/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.IconToggleButton;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.plugins.Plugin;

/**
 * Plugin that allows navigation in josm
 * 
 * @author cdaller
 * 
 */
public class NavigatorPlugin extends Plugin {
    private static final String KEY_HIGHWAY_WEIGHT_PREFIX = "navigator.weight.";
    private NavigatorLayer navigatorLayer;
    private NavigatorModel navigatorModel;

    /**
     * 
     */
    public NavigatorPlugin() {
        super();
        checkWeights();
        navigatorModel = new NavigatorModel();
        setHighwayTypeWeights();
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
                setHighwayTypeWeights();
            }
        });
        navigatorMenu.add(resetMenuItem);
        menu.add(navigatorMenu);
    }
    
    /**
     * Reads the weight values for the different highway types from the preferences.
     */
    private void setHighwayTypeWeights() {
        Map<String, String> weightMap = Main.pref.getAllPrefix(KEY_HIGHWAY_WEIGHT_PREFIX);
        String type;
        double weight;
        String value;
        for(String typeKey : weightMap.keySet()) {
            type = typeKey.substring(KEY_HIGHWAY_WEIGHT_PREFIX.length());
            weight = Double.parseDouble(weightMap.get(typeKey));
            navigatorModel.setHighwayTypeWeight(type, weight);
        }
    }
    
    /**
     * Checks if there are any highway weights set in the preferences. If not, default 
     * values are used.
     */
    private void checkWeights() {
        setDefaultWeight("motorway", 100.0);
        setDefaultWeight("primary", 80.0);
        setDefaultWeight("secondary", 70.0);
        setDefaultWeight("tertiary", 60.0);
        setDefaultWeight("unclassified", 60.0);
        setDefaultWeight("residential", 40.0);
        setDefaultWeight("pedestrian", 0.0);
        setDefaultWeight("cycleway", 0.0);
        setDefaultWeight("footway", 0.0);
    }
    
    private void setDefaultWeight(String type, double value) {
        if(!Main.pref.hasKey(KEY_HIGHWAY_WEIGHT_PREFIX + type)) {
            Main.pref.put(KEY_HIGHWAY_WEIGHT_PREFIX + type, String.valueOf(value));
        }
    }
    
    /* (non-Javadoc)
     * @see org.openstreetmap.josm.plugins.Plugin#mapFrameInitialized(org.openstreetmap.josm.gui.MapFrame, org.openstreetmap.josm.gui.MapFrame)
     */
    @Override
    public void mapFrameInitialized(MapFrame oldFrame, MapFrame newFrame) {
        if(newFrame != null) {
            IconToggleButton button = new IconToggleButton(new NavigatorModeAction(newFrame, navigatorModel, navigatorLayer)); 
            newFrame.toolBarActions.add(button);
            newFrame.toolGroup.add(button);
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
