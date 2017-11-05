package nanolog;

import javax.swing.JDialog;

import org.openstreetmap.josm.gui.MainApplication;
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
        for (Layer layer : MainApplication.getLayerManager().getLayers()) {
            if (layer instanceof GpxLayer && ((GpxLayer) layer).isLocalFile())
                return (GpxLayer) layer;
        }
        return null;
    }
}
