package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;
import static org.openstreetmap.josm.tools.I18n.marktr;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

@SuppressWarnings("serial")
public class MenuActionRefineGeoRef extends JosmAction {

    public static String name = marktr("Refine georeferencing");

    private WMSLayer wmsLayer;
    private RasterImageGeoreferencer rasterImageGeoreferencer;

    public MenuActionRefineGeoRef(WMSLayer wmsLayer) {
        super(tr(name), null, tr("Improve georeferencing (only raster images)"), null, false);
        this.wmsLayer = wmsLayer;
        rasterImageGeoreferencer = new RasterImageGeoreferencer();
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if(!wmsLayer.isRaster())
        {
            System.out.println("MenuActionRefineGeoRef called for unexpected layer type");
            return;
        }
        if (CadastrePlugin.isCadastreProjection()) {
            //wmsLayer = WMSDownloadAction.getLayer();
        } else {
            CadastrePlugin.askToChangeProjection();
        }
        rasterImageGeoreferencer.addListener();
        rasterImageGeoreferencer.startGeoreferencing(wmsLayer);
    }

}
