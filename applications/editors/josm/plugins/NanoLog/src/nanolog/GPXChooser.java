package nanolog;

import javax.swing.*;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.layer.GpxLayer;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * A dialog to choose GPS trace.
 *
 * @author zverik
 */
public class GPXChooser extends JDialog {
    public static GpxLayer chooseLayer() {
        // temporary plug: return first found local layer
        return topLayer();
    }

    public static GpxLayer topLayer() {
        // return first found local layer
        for( Layer layer : Main.getLayerManager().getLayers() ) {
            if( layer instanceof GpxLayer && ((GpxLayer)layer).isLocalFile() )
                return (GpxLayer)layer;
        }
        return null;
    }
}
