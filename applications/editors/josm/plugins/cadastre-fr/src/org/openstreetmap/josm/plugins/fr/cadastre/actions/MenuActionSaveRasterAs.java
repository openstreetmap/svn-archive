// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.fr.cadastre.actions;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.fr.cadastre.wms.WMSLayer;
import org.openstreetmap.josm.tools.Logging;

public class MenuActionSaveRasterAs extends JosmAction {

    public static final String NAME = marktr("Save image as...");

    private static final long serialVersionUID = 1L;

    private WMSLayer wmsLayer;

    public static class FiltrePng extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getName().toLowerCase(Locale.ENGLISH).endsWith(".png");
        }

        @Override
        public String getDescription() {
            return tr("PNG files (*.png)");
        }
    }

    public static class FiltreTiff extends FileFilter {
        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            return file.getName().toLowerCase(Locale.ENGLISH).endsWith(".tif");
        }

        @Override
        public String getDescription() {
            return tr("GeoTiff files (*.tif)");
        }
    }

    FiltreTiff filtreTiff = new FiltreTiff();
    FiltrePng filtrePng = new FiltrePng();

    /**
     * Constructs a new {@code MenuActionSaveRasterAs}.
     * @param wmsLayer WMS layer
     */
    public MenuActionSaveRasterAs(WMSLayer wmsLayer) {
        super(tr(NAME), "save", tr("Export image (only raster images)"), null, false);
        this.wmsLayer = wmsLayer;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        File file;
        JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(filtreTiff);
        fc.addChoosableFileFilter(filtrePng);
        fc.setFileFilter(filtreTiff);
        int returnVal = fc.showSaveDialog(MainApplication.getMainFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            BufferedImage bi = wmsLayer.getImage(0).image;
            if (fc.getFileFilter().equals(filtrePng)) {
                if (!file.getName().endsWith(".png"))
                    file = new File(file.getParent(), file.getName()+".png");
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
                    Logging.error(e);
                }
            } else if (fc.getFileFilter().equals(filtreTiff)) {
                boolean alpha = bi.getColorModel().hasAlpha();
                Logging.info("image with alpha channel : " + alpha);
                try {
                    double x = wmsLayer.getImage(0).min.east();
                    double y = wmsLayer.getImage(0).min.north();
                    Envelope2D bbox = new Envelope2D(CRS.decode("EPSG:27561"),
                            x, y,
                            wmsLayer.getImage(0).max.east()-x, wmsLayer.getImage(0).max.north()-y);
                    GridCoverageFactory factory = new GridCoverageFactory();
                    GridCoverage2D coverage = factory.create("tiff", bi, bbox);
                    final File output = new File(file.getParent(), file.getName()+".tif");
                    GeoTiffWriter gtwriter = new GeoTiffWriter(output);
                    GeoTiffWriteParams wp = new GeoTiffWriteParams();
                    wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
                    wp.setCompressionType("LZW");
                    wp.setCompressionQuality(0.75F);
                    final GeoTiffFormat format = new GeoTiffFormat();
                    final ParameterValueGroup params = format.getWriteParameters();
                    params.parameter(
                                    AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString())
                                    .setValue(wp);

                    gtwriter.write(coverage, params.values().toArray(new GeneralParameterValue[1]));
                    gtwriter.dispose();
                    coverage.dispose(true);
                } catch (Exception e) {
                    Logging.error(e);
                }
            }
        }
    }

}
