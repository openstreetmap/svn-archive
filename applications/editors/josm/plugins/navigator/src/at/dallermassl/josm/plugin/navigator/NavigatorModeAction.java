/**
 * 
 */
package at.dallermassl.josm.plugin.navigator;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.tools.ImageProvider;

import org.openstreetmap.josm.gui.MapFrame;

/**
 * @author cdaller
 *
 */
public class NavigatorModeAction extends MapMode {
    private NavigatorModel navigatorModel;
    private NavigatorLayer navigatorLayer;
    private boolean layerAdded;
    
    public NavigatorModeAction(MapFrame mapFrame, NavigatorModel navigatorModel, NavigatorLayer navigationLayer) {
        super(tr("Navigator"), "navigation", tr("Set start/end for autorouting"), KeyEvent.VK_F, mapFrame, ImageProvider.getCursor("crosshair", "selection"));
        this.navigatorModel = navigatorModel;
        this.navigatorLayer = navigationLayer;
    }
    
    @Override public void enterMode() {
        super.enterMode();
        Main.map.mapView.addMouseListener(this);
        if(!layerAdded) {
            System.out.println("Adding navigatorlayer " + navigatorLayer);
            Main.main.addLayer(navigatorLayer);
            layerAdded = true;
        }
    }

    @Override public void exitMode() {
        super.exitMode();
        Main.map.mapView.removeMouseListener(this);
        //Main.main.removeLayer(navigatorLayer);
    }

    @Override public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            navigatorModel.clearNodes();
        } else if (e.getButton() == MouseEvent.BUTTON1) {        
            Node node = Main.map.mapView.getNearestNode(e.getPoint());
            System.out.println("selected node " + node);
            if(node == null) {
                return;
            }
            navigatorModel.addNode(node);
        }
        Main.map.repaint();
    }



}
