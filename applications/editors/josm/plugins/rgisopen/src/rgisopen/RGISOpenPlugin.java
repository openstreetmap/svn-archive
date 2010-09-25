package rgisopen;

import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.KeyEvent;
import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * Main Plugin class.
 */
public class RGISOpenPlugin extends Plugin implements LayerChangeListener {
    private JMenu menu = null;
    private RGISLayerFromFileAction rgisAction = null;

    public RGISOpenPlugin( PluginInformation info ) {
        super(info);

        if( Main.main.menu != null ) {
            for( int i = 0; i < Main.main.menu.getMenuCount(); i++ ) {
                JMenu m = Main.main.menu.getMenu(i);
                if( m.getName() != null && m.getName().equals(marktr("PicLayer")) ) {
                    menu = m;
                }
            }
            if( menu == null ) {
                menu = Main.main.menu.addMenu(marktr("RGIS"), KeyEvent.VK_I, Main.main.menu.defaultMenuPos, null);
            }
        }

        if( menu != null ) {
            rgisAction = new RGISLayerFromFileAction();
            menu.add(rgisAction);
            rgisAction.setEnabled(false);
        }

        MapView.addLayerChangeListener(this);
    }

    public void activeLayerChange( Layer oldLayer, Layer newLayer ) {
    }

    public void layerAdded( Layer arg0 ) {
        rgisAction.setEnabled(true);
    }

    public void layerRemoved( Layer arg0 ) {
        rgisAction.setEnabled(!Main.map.mapView.getAllLayers().isEmpty());
    }
};
