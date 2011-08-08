/***************************************************************************
 *   Copyright (C) 2009 by Tomasz Stelmach                                 *
 *   http://www.stelmach-online.net/                                       *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package org.openstreetmap.josm.plugins.piclayer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.RenameLayerAction;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;

/**
 * Base class for layers showing images. Actually it does all the showing. The
 * subclasses are supposed only to create images in different ways (load from
 * files, copy from clipboard, hack into a spy satellite and download them,
 * anything...)
 */
public abstract class PicLayerAbstract extends Layer
{
    // Counter - just for naming of layers
    private static int m_counter = 0;
    // This is the main image to be displayed
    private Image m_image = null;
    // Initial position of the image in the real world
    private EastNorth m_initial_position;
    // Position of the image in the real world
    private EastNorth m_position;
    // Angle of rotation of the image
    private double m_angle = 0.0;
    // Scale of the image
    private double m_scalex = 1.0;
    private double m_scaley = 1.0;
    // Shear of the image
    private double m_shearx = 0.0;
    private double m_sheary = 0.0;
    // The scale that was set on the map during image creation
    private double m_initial_scale = 1.0;
    // Layer icon
    private Icon m_layericon = null;

    // Keys for saving in Properties
    private final String INITIAL_POS_X = "INITIAL_POS_X";
    private final String INITIAL_POS_Y = "INITIAL_POS_y";
    private final String POSITION_X = "POSITION_X";
    private final String POSITION_Y = "POSITION_Y";
    private final String ANGLE = "ANGLE";
    private final String INITIAL_SCALE = "INITIAL_SCALE";
    private final String SCALEX = "SCALEX";
    private final String SCALEY = "SCALEY";
    private final String SHEARX = "SHEARX";
    private final String SHEARY = "SHEARY";

    /**
     * Constructor
     */
    public PicLayerAbstract() {
        super("PicLayer #" + m_counter);

        //Increase number
        m_counter++;

        // Load layer icon
        m_layericon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(PicLayerAbstract.class.getResource("/images/layericon.png")));
    }

    /**
     * Initializes the image. Gets the image from a subclass and stores some
     * initial parameters. Throws exception if something fails.
     */
    public void initialize() throws IOException {
        // First, we initialize the calibration, so that createImage() can rely on it
        
        // If the map does not exist - we're screwed. We should not get into this situation in the first place!
        if ( Main.map != null && Main.map.mapView != null ) {
            // Geographical position of the image
            // getCenter() documentation claims it returns a clone, but this is not in line with the code,
            // which actually returns the same object upon subsequent calls. This messes things up
            // when we loading several pictures and associated cal's in one go.
            // So as a workaround, copy the object manually :
            // TODO: not sure about this code below, probably there is a better way to clone the objects
            EastNorth center = Main.map.mapView.getCenter();
            m_initial_position = new EastNorth(center.east(), center.north());
            m_position = new EastNorth(center.east(), center.north());
            // Initial scale at which the image was loaded
            m_initial_scale = Main.map.mapView.getDist100Pixel();
        } else {
            throw new IOException(tr("Could not find the map object."));
        }

        // Create image
        m_image = createImage();
        if ( m_image == null ) {
            throw new IOException(tr("PicLayer failed to load or import the image."));
        }
        // Load image completely
        (new ImageIcon(m_image)).getImage();
    }

    /**
     * To be overridden by subclasses. Provides an image from an external sources.
     * Throws exception if something does not work.
     *
     * TODO: Replace the IOException by our own exception.
     */
    protected abstract Image createImage() throws IOException;

    /**
     * To be overridden by subclasses. Returns the user readable name of the layer.
     */
    protected abstract String getPicLayerName();

    @Override
    public Icon getIcon() {
        return m_layericon;
    }

    @Override
    public Object getInfoComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Action[] getMenuEntries() {
        // Main menu
        return new Action[] {
                new ResetSubmenuAction(),
                SeparatorLayerAction.INSTANCE,
                new SavePictureCalibrationAction(this),
                new LoadPictureCalibrationAction(this),
                SeparatorLayerAction.INSTANCE,
                new RenameLayerAction(null,this)
        };
    }

    @Override
    public String getToolTipText() {
        return getPicLayerName();
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
    public void paint(Graphics2D g2, MapView mv, Bounds bounds) {

        if ( m_image != null) {

            // Position image at the right graphical place
            EastNorth center = Main.map.mapView.getCenter();
            EastNorth leftop = Main.map.mapView.getEastNorth( 0, 0 );
            double pixel_per_en = ( Main.map.mapView.getWidth() / 2.0 ) / ( center.east() - leftop.east() );

            //     This is now the offset in screen pixels
            double pic_offset_x = (( m_position.east() - leftop.east() ) * pixel_per_en);
            double pic_offset_y = (( leftop.north() - m_position.north() ) * pixel_per_en);

            // Let's use Graphics 2D
            Graphics2D g = (Graphics2D)g2.create();
            // Move
            g.translate( pic_offset_x, pic_offset_y );
            // Rotate
            g.rotate( m_angle * Math.PI / 180.0 );
            // Scale
            double scalex = m_scalex * m_initial_scale / Main.map.mapView.getDist100Pixel();
            double scaley = m_scaley * m_initial_scale / Main.map.mapView.getDist100Pixel();
            g.scale( scalex, scaley );
            // Shear
            g.shear(m_shearx, m_sheary);

            // Draw picture
            g.drawImage( m_image, -m_image.getWidth(null) / 2, -m_image.getHeight(null) / 2, null );

            // Draw additional rectangle for the active pic layer
            if ( Main.map.mapView.getActiveLayer() == this ) {
                g.setColor( new Color( 0xFF0000 ) );
                g.drawRect(
                    -m_image.getWidth(null) / 2,
                    -m_image.getHeight(null) / 2,
                    m_image.getWidth(null),
                    m_image.getHeight(null)
                );
            }
        } else {
            // TODO: proper logging
            System.out.println( "PicLayerAbstract::paint - general drawing error (m_image is null or Graphics not 2D" );
        }
    }

    /**
     * Moves the picture. Scaled in EastNorth...
     */
    public void movePictureBy( double east, double north ) {
        m_position = m_position.add( east, north );
    }

    /**
     * Scales the picture. scalex and scaley will multiply the current factor
     */
    public void scalePictureBy( double scalex, double scaley ) {
        m_scalex *= scalex;
        m_scaley *= scaley;
    }

    /**
     * Rotates the picture. Scales in angles.
     */
    public void rotatePictureBy( double angle ) {
        m_angle += angle;
    }

    /**
     * Shear the picture. shearx and sheary will separately add to the
     * corresponding current value
     */
    public void shearPictureBy( double shearx, double sheary ) {
        m_shearx += shearx;
        m_sheary += sheary;
    }

    /**
     * Sets the image position to the initial position
     */
    public void resetPosition() {
        m_position = m_initial_position;
    }

    /**
     * Sets the image scale to 1.0
     */
    public void resetScale() {
        m_scalex = 1.0;
        m_scaley = 1.0;
    }

    /**
     * Sets the image angle to 0.0
     */
    public void resetAngle() {
        m_angle = 0.0;
    }


    /**
     * Sets the image to no shear
     */
    public void resetShear() {
        m_shearx = 0.0;
        m_sheary = 0.0;
    }
    @Override
    /**
     * Computes the (rough) bounding box.
     * We ignore the rotation, the resulting bounding box contains any possible
     * rotation.
     */
    public void visitBoundingBox(BoundingXYVisitor arg0) {
        if ( m_image == null )
            return;
        String projcode = Main.proj.toCode();

        // TODO: bounding box only supported when coordinates are in meters
        // The reason for that is that this .cal think makes us a hard time. 
        // The position is stored as a raw data (can be either in degrees or 
        // in meters, depending on the projection used at creation), but the 
        // initial scale is in m/100pix
        // So for now, we support the bounding box only when everything is in meters
        if (projcode.equals("EPSG:4326") )
            return;
            
        EastNorth center = m_position;
        double w = m_image.getWidth(null);
        double h = m_image.getHeight(null);
        double diag_pix = Math.sqrt(w*w+h*h);
        
        // m_initial_scale is a the scale (unit: m/100pix) at creation time
        double diag_m = (diag_pix/100) * m_initial_scale;
        
        double factor = Math.max(m_scalex, m_scaley);
        double offset = factor * diag_m / 2.0;

        EastNorth topleft = center.add(-offset, -offset);
        EastNorth bottomright = center.add(offset, offset);
        arg0.visit(topleft);
        arg0.visit(bottomright);
    }

    /**
     * Saves the calibration data into properties structure
     * @param props Properties to save to
     */
    public void saveCalibration( Properties props ) {
        // Save
        props.put(INITIAL_POS_X, "" + m_initial_position.getX());
        props.put(INITIAL_POS_Y, "" + m_initial_position.getY());
        props.put(POSITION_X, "" + m_position.getX());
        props.put(POSITION_Y, "" + m_position.getY());
        props.put(INITIAL_SCALE, "" + m_initial_scale);
        props.put(SCALEX, "" + m_scalex);
        props.put(SCALEY, "" + m_scaley);
        props.put(ANGLE, "" + m_angle);
        props.put(SHEARX, "" + m_shearx);
        props.put(SHEARY, "" + m_sheary);
    }

    /**
     * Loads calibration data from file
     * @param file The file to read from
     * @return
     */
    public void loadCalibration(File file) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));
        loadCalibration(props);
    }

    /**
     * Loads calibration data from properties structure
     * @param props Properties to load from
     * @return
     */
    public void loadCalibration( Properties props ) {
        // Load
            double pos_x = Double.valueOf( props.getProperty(POSITION_X));
            double pos_y = Double.valueOf( props.getProperty(POSITION_Y));
            double in_pos_x = Double.valueOf( props.getProperty(INITIAL_POS_X));
            double in_pos_y = Double.valueOf( props.getProperty(INITIAL_POS_Y));
            double angle = Double.valueOf( props.getProperty(ANGLE));
            double in_scale = Double.valueOf( props.getProperty(INITIAL_SCALE));
            double scale_x = Double.valueOf( props.getProperty(SCALEX));
            double scale_y = Double.valueOf( props.getProperty(SCALEY));
            double shear_x = Double.valueOf( props.getProperty(SHEARX));
            double shear_y = Double.valueOf( props.getProperty(SHEARY));
            m_position.setLocation(pos_x, pos_y);
            m_initial_position.setLocation(pos_x, pos_y);
            m_angle = angle;
            m_scalex = scale_x;
            m_scaley = scale_y;
            m_shearx = shear_x;
            m_sheary = shear_y;
            m_initial_scale = in_scale;
            // Refresh
            Main.map.mapView.repaint();
    }

    private class ResetSubmenuAction extends AbstractAction implements LayerAction {

        public ResetSubmenuAction() {
            super(tr("Reset"));
        }

        public void actionPerformed(ActionEvent e) {
        }

        public Component createMenuComponent() {
            JMenu reset_submenu = new JMenu(this);
            reset_submenu.add( new ResetPictureAllAction( PicLayerAbstract.this ) );
            reset_submenu.addSeparator();
            reset_submenu.add( new ResetPicturePositionAction( PicLayerAbstract.this ) );
            reset_submenu.add( new ResetPictureAngleAction( PicLayerAbstract.this ) );
            reset_submenu.add( new ResetPictureScaleAction( PicLayerAbstract.this ) );
            reset_submenu.add( new ResetPictureShearAction( PicLayerAbstract.this ) );
            return reset_submenu;
        }

        public boolean supportLayers(List<Layer> layers) {
            return layers.size() == 1 && layers.get(0) instanceof PicLayerAbstract;
        }

    }
}
