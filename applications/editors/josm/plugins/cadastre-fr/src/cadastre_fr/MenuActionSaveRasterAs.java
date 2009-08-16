package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

public class MenuActionSaveRasterAs extends JosmAction {

    public static String name = "Save image as PNG";
    
    private static final long serialVersionUID = 1L;
    
    private WMSLayer wmsLayer;

    public MenuActionSaveRasterAs(WMSLayer wmsLayer) {
        super(tr(name), "save", tr("Export as PNG format (only raster images)"), null, false);
        this.wmsLayer = wmsLayer;
    }

    public void actionPerformed(ActionEvent arg0) {
        File file;
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showSaveDialog(Main.parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            BufferedImage bi = wmsLayer.images.get(0).image; 
            try {
                ImageIO.write(bi, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
