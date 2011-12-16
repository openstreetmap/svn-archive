/*
 *      PrintPreview.java
 *      
 *      Copyright 2011 Kai Pastor
 *      
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *      
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 *      
 *      
 */

package org.openstreetmap.josm.plugins.print;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JViewport;
import javax.swing.JPanel;
import javax.swing.RepaintManager;

/**
 * A generic print preview component
 * 
 * Given a Printable, a PageFormat and a zoom factor, this component 
 * will render a scrollable and zoomable print preview. If no Printable 
 * is defined, it will fill the printable area of the page format in 
 * plain gray.
 * 
 * There is a special zoom-to-page condition where the component will
 * automatically adjust the zoom level such that the whole page preview
 * will just fit into the size of the component.
 */
class PrintPreview extends JPanel {
    
    /**
     * The PageFormat chosen for printing (and preview)
     */
    protected PageFormat format = null;
    
    /**
     * The current zoom factor for the preview
     */
    protected double zoom = 1.0;
    
    /**
     * If true, the preview is always zoomed such that 
     * the whole page fits into the preview area.
     */
    protected boolean zoomToPage = true;
    
    /**
     * When this flag is true, no painting operations will be performed.
     */
    protected boolean paintingDisabled = false;
    
    /**
     * the printable object for rendering preview contents
     */
    protected Printable printable = null;
    
    /**
     * Constructs a new preview component 
     */
    public PrintPreview() {
        super();
    }

    /**
     * Constructs a new preview component for a printable
     * 
     * @param p the printable object for rendering preview contents, or null
     */
    public PrintPreview(Printable p) {
        super();
        printable = p;
    }

    /**
     * Sets (and unsets) the printable object
     * 
     * @param p a printable object, or null
     */
    public void setPrintable(Printable p) {
        printable = p;
        repaint();
    }
    
    /**
     * Sets the page format
     * 
     * @param format the PageFormat chosen for printing (and preview)
     */
    public void setPageFormat(PageFormat format) {
        this.format = format;
        revalidate();
        repaint();
    }
    
    /**
     * Sets the preview zoom relativ to the actual size
     *
     * @param zoom the zoom factor 
     */
    public void setZoom(double zoom) {
        setZoom(zoom, false);
    }
    
    /**
     * Zoom into the preview
     * 
     * Doubles the current zoom factor, if smaller than 5.0.
     */
    public void zoomIn() {
        double zoom = getZoom();
        if (zoom < 5.0) {
            setZoom(2.0 * zoom);
        }
    }
 
    /**
     * Zoom out of the preview
     * 
     * Set the zoom factor to half its current value, if bigger than 0.1.
     */
    public void zoomOut() {
        double zoom = getZoom();
        if (zoom > 0.1) {
            setZoom(0.5 * zoom);
        }
    }

    /**
     * Zoom to fit the page size
     * 
     * Set the zoom factor such that the whole page fits into the 
     * preview area.
     */
    public void zoomToPage() {
        Container parent = getParent();
        Dimension dim;
        if (parent instanceof JViewport) {
            dim = getParent().getSize(); // could get rid of scrollbars
        }
        else {
            dim = getVisibleRect().getSize();
        }
        int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        double widthZoom = 72.0 * dim.getWidth() / resolution / format.getWidth();
        double heightZoom = 72.0 * dim.getHeight() / resolution / format.getHeight();
        setZoom(Math.min(widthZoom, heightZoom), true);
    }

    /**
     * Sets the preview zoom relativ to the actual size
     *
     * @param zoom the zoom factor 
     * @param zoomToPage when set to true, the zoom factor will be 
     *   adjusted on resize operations such that the whole page always
     *   fits to the preview area.
     */
    protected void setZoom(double zoom, boolean zoomToPage) {
        if (format == null || zoom == this.zoom) {
            return;
        }

        Dimension oldDim = getZoomedPageDimension();
        Rectangle oldView = getVisibleRect();

        paintingDisabled = true; // avoid flickering during zooming

        this.zoom = zoom;
        this.zoomToPage = zoomToPage;
        revalidate(); // Make JScrollPane aware of the change in size
        RepaintManager.currentManager(this).validateInvalidComponents(); 

        Dimension dim = getZoomedPageDimension();
        Rectangle view = getVisibleRect();

        view.x = (int)((0.5*oldView.width+oldView.x)/Math.max(oldView.width,oldDim.width)*Math.max(view.width,dim.width)-0.5*view.width);
        view.y = (int)((0.5*oldView.height+oldView.y)/Math.max(oldView.height,oldDim.height)*Math.max(view.height,dim.height)-0.5*view.height);

        scrollRectToVisible(view);
        paintingDisabled = false;
        repaint(view); // repaint now when zooming is completed
    }

    /**
     * Returns the actual current zoom
     * 
     * This factor might be different from the last explicitly set factor
     * when zoomToPage is true.
     * 
     * @return the zoom factor 
     */
    public double getZoom() {
        if (zoomToPage || zoom < 0.01) {
            // actually this is zoom-to-page 
            Dimension dim = getParent().getSize();
            int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
            double widthZoom = 72.0 * dim.getWidth() / resolution / format.getWidth();
            double heightZoom = 72.0 * dim.getHeight() / resolution / format.getHeight();
            return Math.min(widthZoom, heightZoom);
        }
        return zoom;
    }
        
    /**
     * Get the current dimension of the page preview
     * 
     * @return dimension in pixels
     */
    public Dimension getZoomedPageDimension() {
        int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        double zoom = getZoom();
        int width = (int)(zoom * resolution * format.getWidth() / 72.0);
        int height = (int)(zoom * resolution * format.getHeight() / 72.0);
        return new Dimension(width, height);
    }

    /**
     * Get the current preferred size of the component
     * 
     * @return indicates a zero size when zoomToPage is active, 
     *   the zoomed page dimension otherwise
     */
    @Override
    public Dimension getPreferredSize() {
        if (format == null || zoomToPage == true || zoom < 0.01) {
            return new Dimension(0,0);
        }
        return getZoomedPageDimension();
    }

    /**
     * Paints the component
     * 
     * This will do nothing, not even call super.paintComponent(g), if 
     * painting is disabled in this component.
     * 
     * @param g a Graphics to draw to
     */
    @Override
    public void paintComponent(Graphics g) {
        if (paintingDisabled) {
            return;
        }
        super.paintComponent(g);
        if (format == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        AffineTransform at = g2d.getTransform();

        Dimension out = getZoomedPageDimension();
        double scale = Math.min(
          out.getHeight()/format.getHeight() , out.getWidth()/format.getWidth() );
        double left = 0.5 * ((double)getWidth() - scale * format.getWidth());
        double top  = 0.5 * ((double)getHeight() - scale * format.getHeight());
        
        g2d.translate(left, top);
        g2d.setColor(Color.black);
        g2d.drawRect(-1, -1, out.width+1, out.height+1);
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, out.width, out.height);

        g2d.scale(scale, scale);
        g2d.clip(new Rectangle2D.Double(format.getImageableX(), format.getImageableY(), format.getImageableWidth(), format.getImageableHeight()));
        if (printable != null) {
            try {
                printable.print(g2d, format, 0);
            }
            catch (PrinterException e) {
                // should never happen since we are not printing
            }
        }
        else {
            g2d.setColor(Color.gray);
            g2d.fillRect(0, 0, (int)format.getWidth(), (int)format.getHeight());
        }


        g2d.setTransform(at);
    }

}
