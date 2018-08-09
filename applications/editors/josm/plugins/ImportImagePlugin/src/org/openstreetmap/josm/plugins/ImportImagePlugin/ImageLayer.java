// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.ImportImagePlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.media.jai.PlanarImage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.image.ImageWorker;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.ProjectionBounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.Logging;

/**
 *  Layer which contains spatial referenced image data.
 *
 * @author Christoph Beekmans, Fabian Kowitz, Anna Robaszkiewicz, Oliver Kuhn, Martin Ulitzny
 *
 */
public class ImageLayer extends Layer {

    private Logger logger = Logger.getLogger(ImageLayer.class);

    private File imageFile;

    private BufferedImage image = null;

    // coordinates of upper left corner
    private EastNorth upperLeft;
    // Angle of rotation of the image
    private double angle = 0.0;

    // current bbox
    private Envelope2D bbox;

    // Layer icon
    private Icon layericon = null;

    // reference system of the oringinal image
    private CoordinateReferenceSystem sourceRefSys;

    /**
     * Constructor
     */
    public ImageLayer(File file) throws IOException {
        super(file.getName());

        this.imageFile = file;
        this.image = createImage();
        URL iconURL = getClass().getResource("images/layericon.png");
        if (iconURL != null) {
            layericon = new ImageIcon(iconURL);
        }
    }

    /**
     * create spatial referenced image.
     */
    private BufferedImage createImage() throws IOException {

        // geotools type for images and value coverages
        GridCoverage2D coverage = null;
        try {
            // create a grid coverage from the image
            coverage = PluginOperations.createGridFromFile(imageFile, null, true);
            this.sourceRefSys = coverage.getCoordinateReferenceSystem();

            // now reproject grid coverage
            coverage = PluginOperations.reprojectCoverage(coverage, CRS.decode(Main.getProjection().toCode()));

        } catch (FactoryException e) {
            logger.error("Error while creating GridCoverage:", e);
            throw new IOException(e.getMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("No projection file found")) {
                int val = 2;
                if (!GraphicsEnvironment.isHeadless()) {
                    ExtendedDialog ex = new ExtendedDialog(Main.parent, tr("Warning"), 
                        new String[] {tr("Default image projection"), tr("JOSM''s current projection"), tr("Cancel")});
                    // CHECKSTYLE.OFF: LineLength
                    ex.setContent(tr("No projection file (.prj) found.<br>"
                        + "You can choose the default image projection ({0}) or JOSM''s current editor projection ({1}) as original image projection.<br>"
                        + "(It can be changed later from the right click menu of the image layer.)", 
                        ImportImagePlugin.pluginProps.getProperty("default_crs_srid"), Main.getProjection().toCode()));
                    // CHECKSTYLE.ON: LineLength
                    val = ex.showDialog().getValue();
                    if (val == 3) {
                        logger.debug("No projection and user declined un-projected use");
                        throw new LayerCreationCanceledException();
                    }
                }
                CoordinateReferenceSystem src = null;
                try {
                    if (val == 1) {
                        src = PluginOperations.defaultSourceCRS;
                    } else {
                        logger.debug("Passing through image un-projected.");
                        src = CRS.decode(Main.getProjection().toCode());
                    }
                    // create a grid coverage from the image
                    coverage = PluginOperations.createGridFromFile(imageFile, src, false);
                    this.sourceRefSys = coverage.getCoordinateReferenceSystem();
                    if (val == 1) {
                        coverage = PluginOperations.reprojectCoverage(coverage, CRS.decode(Main.getProjection().toCode()));
                    }
                } catch (Exception e1) {
                    logger.error("Error while creating GridCoverage:", e1);
                    throw new IOException(e1);
                }
            } else {
                logger.error("Error while creating GridCoverage:", e);
                throw new IOException(e);
            }

        }
        logger.debug("Coverage created: " + coverage);

        upperLeft = new EastNorth(coverage.getEnvelope2D().x,
                coverage.getEnvelope2D().y + coverage.getEnvelope2D().height);
        angle = 0;
        bbox = coverage.getEnvelope2D();

        RenderedImage img = coverage.getRenderedImage();
        try {
            BufferedImage bi = new ImageWorker(img).getBufferedImage();
            BufferedImage dst = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = dst.createGraphics();
            try {
                // Check image can be drawn correctly
                g2d.drawImage(bi, 0, 0, null);
            } finally {
                g2d.dispose();
            }
            return bi;
        } catch (ArrayIndexOutOfBoundsException e) {
            Logging.debug(e);
            // See #12108 - rescale to bytes in case of ComponentColorModel index error
            return new ImageWorker(img).rescaleToBytes().getBufferedImage();
        }
    }

    @Override
    public void paint(Graphics2D g2, MapView mv, Bounds bounds) {

        if (image != null && g2 != null) {

            // Position image at the right graphical place
            EastNorth center = mv.getCenter();
            EastNorth leftop = mv.getEastNorth(0, 0);
            double pixel_per_east_unit = (mv.getWidth() / 2.0)
                    / (center.east() - leftop.east());
            double pixel_per_north_unit = (mv.getHeight() / 2.0)
                    / (leftop.north() - center.north());

            // This is now the offset in screen pixels
            double pic_offset_x = ((upperLeft.east() - leftop.east()) * pixel_per_east_unit);
            double pic_offset_y = ((leftop.north() - upperLeft.north()) * pixel_per_north_unit);

            Graphics2D g = (Graphics2D) g2.create();

            // Move picture by offset from upper left corner
            g.translate(pic_offset_x, pic_offset_y);

            // Rotate image by angle
            g.rotate(angle * Math.PI / 180.0);

            // Determine scale to fit JOSM extents
            ProjectionBounds projbounds = mv.getProjectionBounds();

            double width = projbounds.maxEast - projbounds.minEast;
            double height = projbounds.maxNorth - projbounds.minNorth;

            double ratio_x = (this.bbox.getMaxX() - this.bbox.getMinX())
                    / width;
            double ratio_y = (this.bbox.getMaxY() - this.bbox.getMinY())
                    / height;

            double pixels4bbox_width = ratio_x * mv.getWidth();
            double pixels4bbox_height = ratio_y * mv.getHeight();

            // Scale image to JOSM extents
            double scalex = pixels4bbox_width / image.getWidth();
            double scaley = pixels4bbox_height / image.getHeight();

            if ((scalex > 10) || (scaley > 10)) {
                logger.warn("Not drawing image - scale too big");
                return;
            }
            g.scale(scalex, scaley);

            // Draw picture
            try {
                g.drawImage(image, 0, 0, null);
            } catch (ArrayIndexOutOfBoundsException e) {
                // TODO: prevents this to happen when displaying GeoTIFF images (see #7902)
                Logging.error(e);
            }

        } else {
            logger.error("Error while dawing image: image == null or Graphics == null");
        }
    }

    public Envelope2D getBbox() {
        return bbox;
    }

    @Override
    public Icon getIcon() {
        // TODO Auto-generated method stub
        return this.layericon;
    }

    @Override
    public Object getInfoComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Action[] getMenuEntries() {
        return new Action[]{
                LayerListDialog.getInstance().createActivateLayerAction(this),
                LayerListDialog.getInstance().createShowHideLayerAction(),
                LayerListDialog.getInstance().createDeleteLayerAction(),
                SeparatorLayerAction.INSTANCE,
                new RenameLayerAction(getAssociatedFile(), this),
                new LayerPropertiesAction(this),
                SeparatorLayerAction.INSTANCE,
                new LayerListPopup.InfoAction(this)};
    }

    @Override
    public boolean isMergable(Layer arg0) {
        return false;
    }

    @Override
    public void mergeFrom(Layer arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor visitor) {
            EastNorth min = new EastNorth(getBbox().getMinX(), getBbox().getMinY());
            EastNorth max = new EastNorth(getBbox().getMaxX(), getBbox().getMaxY());
            visitor.visit(min);
            visitor.visit(max);
    }

    @Override
    public String getToolTipText() {
        return this.getName();
    }

    public File getImageFile() {
        return imageFile;
    }

    public BufferedImage getImage() {
        return image;
    }

    /**
     * loads the image and reprojects it using a transformation
     * calculated by the new reference system.
     */
    void resample(CoordinateReferenceSystem refSys) throws IOException, NoSuchAuthorityCodeException, FactoryException {
        logger.debug("resample");
        GridCoverage2D coverage = PluginOperations.createGridFromFile(this.imageFile, refSys, true);
        coverage = PluginOperations.reprojectCoverage(coverage, CRS.decode(Main.getProjection().toCode()));
        this.bbox = coverage.getEnvelope2D();
        this.image = ((PlanarImage) coverage.getRenderedImage()).getAsBufferedImage();

        upperLeft = new EastNorth(coverage.getEnvelope2D().x, coverage
                .getEnvelope2D().y
                + coverage.getEnvelope2D().height);
        angle = 0;

        // repaint and zoom to new bbox
        BoundingXYVisitor boundingXYVisitor = new BoundingXYVisitor();
        visitBoundingBox(boundingXYVisitor);
        MainApplication.getMap().mapView.zoomTo(boundingXYVisitor);
    }

    /**
     * Action that creates a dialog GUI element with properties of a layer.
     *
     */
    public static class LayerPropertiesAction extends AbstractAction {
        public ImageLayer imageLayer;

        public LayerPropertiesAction(ImageLayer imageLayer) {
            super(tr("Layer Properties"));
            this.imageLayer = imageLayer;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            LayerPropertiesDialog layerProps = new LayerPropertiesDialog(imageLayer, PluginOperations.crsDescriptions);
            layerProps.setLocation(Main.parent.getWidth() / 4, Main.parent.getHeight() / 4);
            layerProps.setVisible(true);
        }
    }

    /**
     * Exception which represents that the layer creation has been canceled by the user.
     */
    static class LayerCreationCanceledException extends IOException{
    }

    public CoordinateReferenceSystem getSourceRefSys() {
        return sourceRefSys;
    }

    public void setSourceRefSys(CoordinateReferenceSystem sourceRefSys) {
        this.sourceRefSys = sourceRefSys;
    }
}
