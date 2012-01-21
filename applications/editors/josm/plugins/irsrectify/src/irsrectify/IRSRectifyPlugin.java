package irsrectify;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.JosmUserIdentityManager;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.ImageryLayer;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;
import org.openstreetmap.josm.tools.Shortcut;

public class IRSRectifyPlugin extends Plugin {
    static private int newLayerNameCounter = 0;

    private MapFrame frame;

    public IRSRectifyPlugin(PluginInformation info) {
        super(info);
        Main.main.menu.toolsMenu.add(new IRSRectifyAction());
    }

    @Override
    public void mapFrameInitialized(MapFrame mf, MapFrame newFrame) {
        frame = newFrame;
    }

    public class IRSRectifyAction extends JosmAction {

        public IRSRectifyAction() {
            super(tr("Update IRS adjustment layer"), "irsrectify.png", tr("Update IRS adjustment layer"),
                    Shortcut.registerShortcut("menu:irsrectify", tr("Update IRS adjustment layer"), KeyEvent.VK_I, Shortcut.GROUP_LAYER), false);
        }

        public void actionPerformed(ActionEvent e) {
            ImageryLayer l = findImageryLayer();
            if( l == null )
                return;

            // calculate offset from wms layer
            double dx = l.getDx();
            double dy = l.getDy();
            if( dx == 0 && dy == 0 ) {
                JOptionPane.showMessageDialog(Main.parent, tr("This option creates IRS adjustment layer and a little way inside it. You need to adjust WMS layer placement first.\nResulting layer is to be saved as .osm and sent to Komzpa (me@komzpa.net) with [irs rectify] in subject."));
                return;
            }
            // create an offset way and add to dataset
            Node center = new Node(frame.mapView.getCenter());
            Node offset = new Node(center.getEastNorth().add(dx, dy));
            Way way = new Way();
            way.addNode(center);
            way.addNode(offset);
            way.put("timestamp", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            String userName = JosmUserIdentityManager.getInstance().getUserName();
            if( userName != null )
                way.put("user", userName);

            OsmDataLayer data = findOrCreateDataLayer();
            data.data.addPrimitive(center);
            data.data.addPrimitive(offset);
            data.data.addPrimitive(way);
            data.data.setSelected(way.getPrimitiveId());
            frame.mapView.setActiveLayer(data);
        }

        private ImageryLayer findImageryLayer() {
            if( frame == null || frame.mapView == null )
                return null;
            for( Layer l : frame.mapView.getAllLayers() )
                if( l instanceof ImageryLayer )
                    return (ImageryLayer)l;
            return null;
        }

        private OsmDataLayer findOrCreateDataLayer() {
            if( frame == null || frame.mapView == null )
                return null;

            OsmDataLayer l = frame.mapView.getEditLayer();
            if( isOffsetLayer(l) )
                return l;

            // try to find among all layers
            for( Layer layer : frame.mapView.getAllLayers() )
                if( layer instanceof OsmDataLayer && isOffsetLayer((OsmDataLayer)layer) )
                    return (OsmDataLayer) layer;

            // if there are none, create one
            String name = tr("IRS Adjustment Layer");
            if( ++newLayerNameCounter > 1 )
                name = name + " " + newLayerNameCounter;
            l = new OsmDataLayer(new DataSet(), name, null);
            Main.main.addLayer(l);
            return l;
        }

        private boolean isOffsetLayer( OsmDataLayer l ) {
            if( l == null || l.data == null ) return false;
            for( Way way : l.data.getWays() ) {
                if( !way.isDeleted() && (way.getNodesCount() != 2 || !way.hasKey("timestamp") || !way.hasKey("user")) )
                    return false;
            }
            return true;
        }

        @Override
        protected void updateEnabledState() {
            setEnabled(findImageryLayer() != null);
        }
    }
}
