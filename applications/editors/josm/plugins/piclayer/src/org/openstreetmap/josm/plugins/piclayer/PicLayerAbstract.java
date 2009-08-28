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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.Main;
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
    private BufferedImage m_image = null;
    // Initial position of the image in the real world
    private EastNorth m_initial_position;
    // Position of the image in the real world
    private EastNorth m_position;
    // Angle of rotation of the image
    private double m_angle = 0.0;
    // Scale of the image
    private double m_scalex = 1.0;
    private double m_scaley = 1.0;
    // The scale that was set on the map during image creation
    private double m_initial_scale = 0;
    // Popup menu items
    private Component m_popupmenu[] = null;
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

    /**
     * Constructor
     */
    public PicLayerAbstract() {
        super("PicLayer #" + m_counter);

        //Increase number
        m_counter++;

        // Create popup menu
        // Reset submenu
        JMenu reset_submenu = new JMenu( "Reset" );
        reset_submenu.add( new ResetPictureAllAction( this ) );
        reset_submenu.addSeparator();
        reset_submenu.add( new ResetPicturePositionAction( this ) );
        reset_submenu.add( new ResetPictureAngleAction( this ) );
        reset_submenu.add( new ResetPictureScaleAction( this ) );
        // Main menu
        m_popupmenu = new Component[]{
                reset_submenu,
                new JSeparator(),
                new JMenuItem( new SavePictureCalibrationAction(this)),
                new JMenuItem( new LoadPictureCalibrationAction(this)),
                new JSeparator(),
                new JMenuItem( new HelpAction() )
        };

        // Load layer icon
        m_layericon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(PicLayerAbstract.class.getResource("/images/layericon.png")));
    }

    /**
     * Initializes the image. Gets the image from a subclass and stores some
     * initial parameters. Throws exception if something fails.
     */
    public void Initialize() throws IOException {

        // Create image
        Image image = createImage();
        if ( image == null ) {
            throw new IOException( "Image not created properly.");
        }
        // Convert to Buffered Image - not sure if this is the right way...
        m_image = new BufferedImage( image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB );
        Graphics g = m_image.getGraphics();
        g.drawImage( image, 0, 0, null );

        // If the map does not exist - we're screwed. We should not get into this situation in the first place!
        if ( Main.map != null && Main.map.mapView != null ) {
            // Geographical position of the image
            m_initial_position = m_position = Main.map.mapView.getCenter();
            // Initial scale at which the image was loaded
            m_initial_scale = Main.map.mapView.getDist100Pixel();
        } else {
            throw new IOException( "Could not find the map object." );
        }
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
    public Component[] getMenuEntries() {
        return m_popupmenu;
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
    public void paint(Graphics arg0, MapView arg1) {

        if ( m_image != null && arg0 instanceof Graphics2D) {

            // Position image at the right graphical place
            EastNorth center = Main.map.mapView.getCenter();
            EastNorth leftop = Main.map.mapView.getEastNorth( 0, 0 );
            double pixel_per_en = ( Main.map.mapView.getWidth() / 2.0 ) / ( center.east() - leftop.east() );

            //     This is now the offset in screen pixels
            double pic_offset_x = (( m_position.east() - leftop.east() ) * pixel_per_en);
            double pic_offset_y = (( leftop.north() - m_position.north() ) * pixel_per_en);

            // Let's use Graphics 2D
            Graphics2D g = (Graphics2D)arg0.create();
            // Move
            g.translate( pic_offset_x, pic_offset_y );
            // Rotate
            g.rotate( m_angle * Math.PI / 180.0 );
            // Scale
            double scalex = m_scalex * m_initial_scale / Main.map.mapView.getDist100Pixel();
            double scaley = m_scaley * m_initial_scale / Main.map.mapView.getDist100Pixel();
            g.scale( scalex, scaley );

            // Draw picture
            g.drawImage( m_image, -m_image.getWidth() / 2, -m_image.getHeight() / 2, null );

            // Draw additional rectangle for the active pic layer
            if ( Main.map.mapView.getActiveLayer() == this ) {
                g.setColor( new Color( 0xFF0000 ) );
                g.drawRect(
                    -m_image.getWidth() / 2,
                    -m_image.getHeight() / 2,
                    m_image.getWidth(),
                    m_image.getHeight()
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
     * Scales the picture. Scaled in... don't know but works ok :)
     */
    public void scalePictureBy( double scalex, double scaley ) {
        m_scalex += scalex;
        m_scaley += scaley;
    }

    /**
     * Rotates the picture. Scales in angles.
     */
    public void rotatePictureBy( double angle ) {
        m_angle += angle;
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

    @Override
    public void visitBoundingBox(BoundingXYVisitor arg0) {
        // TODO Auto-generated method stub

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
  			m_position.setLocation(pos_x, pos_y);
    		m_initial_position.setLocation(pos_x, pos_y);
    		m_angle = angle;
    		m_scalex = scale_x;
    		m_scaley = scale_y;
    		m_initial_scale = in_scale;
    		// Refresh
            Main.map.mapView.repaint();
    }
}
