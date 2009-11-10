// License: GPL. v2 and later. Copyright 2008-2009 by Pieren <pieren3@gmail.com> and others
package cadastre_fr;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;

public class MenuActionSaveRasterAs extends JosmAction {

    public static String name = "Save image as PNG";
    
    private static final long serialVersionUID = 1L;
    
    private WMSLayer wmsLayer;
    
    public class FiltrePng extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) { 
                return true;
            } 
            return file.getName().toLowerCase().endsWith(".png");
        }

        @Override
        public String getDescription() {
            return tr("PNG files (*.png)");
        }
        
    }
    
    FiltrePng filtrePng = new FiltrePng();

    public MenuActionSaveRasterAs(WMSLayer wmsLayer) {
        super(tr(name), "save", tr("Export as PNG format (only raster images)"), null, false);
        this.wmsLayer = wmsLayer;
    }

    public void actionPerformed(ActionEvent arg0) {
        File file;
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(filtrePng);
        int returnVal = fc.showSaveDialog(Main.parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            if (!file.getName().endsWith(".png"))
                file = new File(file.getParent(), file.getName()+".png");
            BufferedImage bi = wmsLayer.images.get(0).image; 
            try {
                ImageIO.write(bi, "png", file);
/*
                FileOutputStream flux = new FileOutputStream(file);
                BufferedOutputStream fluxBuf = new BufferedOutputStream(flux);
                JPEGImageEncoder codec = JPEGCodec.createJPEGEncoder(fluxBuf, JPEGCodec.getDefaultJPEGEncodeParam(bi));
                codec.encode(bi);
                fluxBuf.close();
*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
