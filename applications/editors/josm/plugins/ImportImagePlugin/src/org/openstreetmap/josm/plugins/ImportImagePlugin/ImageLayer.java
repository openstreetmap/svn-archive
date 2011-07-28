package org.openstreetmap.josm.plugins.ImportImagePlugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Component;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.media.jai.PlanarImage;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

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
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;

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
     * 
     * @param file
     * @throws IOException 
     */
    public ImageLayer(File file) throws IOException {
        super(file.getName());
        
        this.imageFile = file;
        this.image = (BufferedImage) createImage();
        layericon = new ImageIcon(ImportImagePlugin.pluginClassLoader.getResource("images/layericon.png"));
    }

    /**
     * create spatial referenced image.
     * 
     * @return
     * @throws IOException
     */
    private Image createImage() throws IOException {

        // geotools type for images and value coverages
        GridCoverage2D coverage = null;
        try {
            // create a grid coverage from the image
            coverage = PluginOperations.createGridFromFile(imageFile, null, true);
            this.sourceRefSys = coverage.getCoordinateReferenceSystem();
            
            // now reproject grid coverage
            coverage = PluginOperations.reprojectCoverage(coverage, CRS.decode(Main.proj.toCode()));
        
        } catch (FactoryException e) {
            logger.error("Error while creating GridCoverage:",e);
            throw new IOException(e.getMessage());
        } catch (Exception e) {
            if(e.getMessage().contains("No projection file found"))
            {
                int useUnprojected = JOptionPane.showConfirmDialog(Main.parent, "<html>No projection file (.prj) found.<br />Use the image unprojected?</html>", "Missing projection", JOptionPane.YES_NO_OPTION);
                if (useUnprojected == 0) { // Yes
                    logger.debug("Passing through image un-projected.");
                    try {
                        // create a grid coverage from the image
                        coverage = PluginOperations.createGridFromFile(imageFile, null, false);
                        this.sourceRefSys = coverage.getCoordinateReferenceSystem();
                    } catch (Exception e1) {
                        logger.error("Error while creating GridCoverage:",e1);
                        throw new IOException(e1);
                    }
                } else { // No
                    logger.debug("No projection and user declined un-projected use");
                    throw new LayerCreationCancledException();
                }
            }
            else
            {
                logger.error("Error while creating GridCoverage:",e);
                throw new IOException(e);
            }

        }
        logger.debug("Coverage created: " + coverage);

        // TODO
        upperLeft = new EastNorth(coverage.getEnvelope2D().x,
                coverage.getEnvelope2D().y + coverage.getEnvelope2D().height);
        angle = 0;
        bbox = coverage.getEnvelope2D();

        // Refresh
        // Main.map.mapView.repaint();
//      PlanarImage image = (PlanarImage) coverage.getRenderedImage();
//      logger.info("Color Model: " + coverage.getRenderedImage().getColorModel());
        ImageWorker worker = new ImageWorker(coverage.getRenderedImage());

        return worker.getBufferedImage();
    }

    @Override
    public void paint(Graphics2D g2, MapView mv, Bounds bounds) {

        if (image != null && g2 != null) {

            // Position image at the right graphical place
            EastNorth center = Main.map.mapView.getCenter();
            EastNorth leftop = Main.map.mapView.getEastNorth(0, 0);
            double pixel_per_east_unit = (Main.map.mapView.getWidth() / 2.0)
                    / (center.east() - leftop.east());
            double pixel_per_north_unit = (Main.map.mapView.getHeight() / 2.0)
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
            ProjectionBounds projbounds = Main.map.mapView
                    .getProjectionBounds();

            double width = projbounds.maxEast - projbounds.minEast;
            double height = projbounds.maxNorth - projbounds.minNorth;

            double ratio_x = (this.bbox.getMaxX() - this.bbox.getMinX())
                    / width;
            double ratio_y = (this.bbox.getMaxY() - this.bbox.getMinY())
                    / height;

            double pixels4bbox_width = ratio_x * Main.map.mapView.getWidth();
            double pixels4bbox_height = ratio_y * Main.map.mapView.getHeight();

            // Scale image to JOSM extents
            double scalex = pixels4bbox_width / image.getWidth();
            double scaley = pixels4bbox_height / image.getHeight();

            if ((scalex > 10) || (scaley > 10)) {
                logger.warn("Not drawing image - scale too big");
                return;
            }
            g.scale(scalex, scaley);

            // Draw picture
            g.drawImage(image, 0, 0, null);
            
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void mergeFrom(Layer arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void visitBoundingBox(BoundingXYVisitor arg0) {
        // TODO Auto-generated method stub
    }
    

    @Override
    public String getToolTipText() {
        // TODO Auto-generated method stub
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
     * 
     * @param newRefSys
     * @throws IOException 
     * @throws FactoryException 
     * @throws NoSuchAuthorityCodeException 
     */
    void resample(CoordinateReferenceSystem refSys) throws IOException, NoSuchAuthorityCodeException, FactoryException
    {
        logger.debug("resample");
        GridCoverage2D coverage =  PluginOperations.createGridFromFile(this.imageFile, refSys, true);
        coverage = PluginOperations.reprojectCoverage(coverage, CRS.decode(Main.proj.toCode()));
        this.bbox = coverage.getEnvelope2D();
        this.image = ((PlanarImage)coverage.getRenderedImage()).getAsBufferedImage();
        
        // TODO
        upperLeft = new EastNorth(coverage.getEnvelope2D().y, coverage
                .getEnvelope2D().x
                + coverage.getEnvelope2D().width);
        angle = 0;

        // repaint and zoom to new bbox
        Main.map.mapView.repaint();
        LatLon min = new LatLon(bbox.getMinX(), bbox.getMinY());
        LatLon max = new LatLon(bbox.getMaxX(), bbox.getMaxY());
        Main.map.mapView.zoomTo(new Bounds(min, max));
        
        
    }
    
    /**
     * Action that creates a dialog GUI element with properties of a layer.
     * 
     */
    public class LayerPropertiesAction extends AbstractAction
    {
        public ImageLayer imageLayer;
        
        public LayerPropertiesAction(ImageLayer imageLayer){
            super(tr("Layer Properties"));
            this.imageLayer = imageLayer;
        }

        public void actionPerformed(ActionEvent arg0) {
            
            LayerPropertiesDialog layerProps = new LayerPropertiesDialog(imageLayer, PluginOperations.crsDescriptions);
            layerProps.setLocation(Main.parent.getWidth() / 4 , Main.parent.getHeight() / 4);
            layerProps.setVisible(true);
        }
        
    }
    
    /**
     * Exception which represents that the layer creation has been cancled by the
     * user.
     *
     */
    class LayerCreationCancledException extends IOException{
    }
    
    public CoordinateReferenceSystem getSourceRefSys() {
        return sourceRefSys;
    }

    public void setSourceRefSys(CoordinateReferenceSystem sourceRefSys) {
        this.sourceRefSys = sourceRefSys;
    }
}
