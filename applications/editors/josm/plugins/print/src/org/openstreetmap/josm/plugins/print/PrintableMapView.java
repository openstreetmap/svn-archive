/*
 *      PrintableMapView.java
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

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.NavigatableComponent.SystemOfMeasurement;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;

/**
 * The PrintableMapView class implements a "Printable" perspective on
 * the main MapView.
 * 
 */
public class PrintableMapView extends MapView implements Printable {
    
    /**
     * A fixed map scale if greater than zero.
     */
    protected int fixedMapScale = 0;
    
    /**
     * The factor for scaling the printing graphics to the desired
     * resolution
     */
     protected double g2dFactor;
    
    /**
     * The real MapView which is the data source
     */
    protected MapView mapView;
    
    /**
     * The font size for text added by PrintableMapView
     */
    public static final int FONT_SIZE = 8;

    /**
     * Create a new PrintableMapView.
     */
    public PrintableMapView() {
        /* Initialize MapView with a dummy parent */
        super(new JPanel());
        
        /* Disable MapView's ComponentLister, 
         * as it will interfere with the main MapView. */
        java.awt.event.ComponentListener listeners[] = getComponentListeners();
        for (int i=0; i<listeners.length; i++) {
            removeComponentListener(listeners[i]);
        }
        
        mapView = Main.main.map.mapView;
    }

    /**
     * Set a fixed map scale 1 : "scale"
     * 
     * @param scale the fixed map scale
     */
    public void setFixedMapScale(int scale) {
        this.fixedMapScale = scale;
        rezoomToFixedScale();
    }

    /**
     * Unset the fixed map scale
     * 
     * The map scaling will be chosen automatically such that the
     * main windows map view fits on the page format.
     */
    public void unsetFixedMapScale() {
        setFixedMapScale(0);
        rezoomToFixedScale();
    }

    /** 
     * Get the map scale that will be used for rendering
     */     
    public int getMapScale() {
        if (fixedMapScale > 0 || g2dFactor == 0.0) {
            return fixedMapScale;
        }

        double dist100px = getDist100Pixel() / g2dFactor;
        int mapScale = (int) (dist100px * 72.0 / 2.54);
        return mapScale;
    }

    /**
     * Initialize the PrintableMapView for a particular combination of
     * main MapView, PageFormat and target resolution
     * 
     * @param pageformat the size and orientation of the page being drawn
     */
    public void initialize(PageFormat pageFormat) {
        int resolution = Main.pref.getInteger("print.resolution.dpi", PrintPlugin.DEF_RESOLUTION_DPI);
        g2dFactor = 72.0/resolution;
        setSize((int)(pageFormat.getImageableWidth()/g2dFactor),(int)(pageFormat.getImageableHeight()/g2dFactor));
    }
    
    /**
     * Resizes this component. 
     */
    @Override
    public void setSize(int width, int height) {
        Dimension dim = getSize();
        if (dim.width != width || dim.height != height) {
            super.setSize(width, height);
            zoomTo(mapView.getRealBounds());
            rezoomToFixedScale();
        }
    }
            
    /**
     * Resizes this component. 
     */
    @Override
    public void setSize(Dimension newSize) {
        Dimension dim = getSize();
        if (dim.width != newSize.width || dim.height != newSize.height) {
            super.setSize(newSize);
            zoomTo(mapView.getRealBounds());
            rezoomToFixedScale();
        }
    }

    /**
     * Adjust the zoom as necessary to establish the fixed scale.
     */
    protected void rezoomToFixedScale() {
        if (fixedMapScale > 0) {
            double dist100px = getDist100Pixel() / g2dFactor;
            double mapScale = dist100px * 72.0 / 2.54;
            double mapFactor = fixedMapScale / mapScale;
            zoomToFactor(mapFactor);
        }
    }

    /**
     * Render a page for the printer
     * 
     * Implements java.awt.print.Printable.
     *
     * @param g the context into which the page is drawn
     * @param pageFormat the size and orientation of the page being drawn
     * @param page the zero based index of the page to be drawn 
     * 
     * @return PAGE_EXISTS for page==0 or NO_SUCH_PAGE for page>0
     * 
     * @throws PrinterException thrown when the print job is terminated
     * 
     */
    @Override
    public int print(Graphics g, PageFormat pageFormat, int page) throws
                                                    PrinterException {
        if (page > 0) { /* stop after first page */
            return NO_SUCH_PAGE;
        }

        initialize(pageFormat);

        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        paintMap(g2d, pageFormat);
        paintMapScale(g2d, pageFormat);
        paintMapAttribution(g2d, pageFormat);
        return PAGE_EXISTS;
    }
    
    /** 
     * Paint the map
     * 
     * This implementation is derived from MapView's paint and
     * from other JOSM core components.
     * 
     * @param g2d the graphics context to use for painting
     * @param pageFormat the size and orientation of the page being drawn
     */
    public void paintMap(Graphics2D g2d, PageFormat pageFormat) {
        AffineTransform at = g2d.getTransform();
        g2d.scale(g2dFactor, g2dFactor);
        
        Bounds box = getRealBounds();
        List<Layer> visibleLayers = getVisibleLayersInZOrder();
        for (Layer l : visibleLayers) {
            try {
                if (! (l instanceof OsmDataLayer)) {
                    /* OsmDataLayer does not need this.*/
                    
                    /* This manipulations may have all kinds of unwanted side effects
                     * but many Layer implementations heavily depend on Main.map.mapView. */
                    Main.map.mapView = this;
                }
                if (l.getOpacity() < 1) {
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)l.getOpacity()));
                }
                l.paint(g2d, this, box);
                g2d.setPaintMode();
            }
            finally {
                Main.map.mapView = mapView;
            }
        }

        g2d.setTransform(at);
    }

    /** 
     * Paint a linear scale and a lexical scale
     * 
     * This implementation is derived from JOSM's MapScaler, 
     * NavigatableComponent and SystemOfMeasurement.
     *
     * @param g2d the graphics context to use for painting
     * @param pageFormat the size and orientation of the page being drawn
     */
    public void paintMapScale(Graphics2D g2d, PageFormat pageFormat) {
        SystemOfMeasurement som = getSystemOfMeasurement();
        double dist100px = getDist100Pixel() / g2dFactor;
        double dist = dist100px / som.aValue;
        String unit  = som.aName;
        if (!Main.pref.getBoolean("system_of_measurement.use_only_lower_unit", false) && dist > som.bValue / som.aValue) {
            dist = dist100px / som.bValue;
            unit  = som.bName;
        }
        long distExponent = (long) Math.floor(Math.log(dist) / Math.log(10));
        double distMantissa = dist / Math.pow(10, distExponent);
        double distScale = 1.0;
        if (distMantissa <= 2.5) {
            distScale = 2.5 / distMantissa;
        }
        else if (distMantissa <= 4.0) {
            distScale = 5.0 / distMantissa;
        }
        else {
            distScale = 10.0 / distMantissa;
        }

        Font labelFont = new Font("Arial", Font.PLAIN, FONT_SIZE);
        g2d.setFont(labelFont);

        /* length of scale */
        int x = (int) (100.0 * distScale);

        /* offset from the left paper border to the left end of the bar */
        Rectangle2D bound = g2d.getFontMetrics().getStringBounds("0", g2d);
        int xLeft = (int)(bound.getWidth()/2);
        
        /* offset from the left paper border to the right label */
        String rightLabel = som.getDistText(dist100px * distScale);
        bound = g2d.getFontMetrics().getStringBounds(rightLabel, g2d);
        int xRight = xLeft+(int) Math.max(0.95*x, x-bound.getWidth()/2);
        
        int h        = FONT_SIZE / 2; // raster, height of the bar
        int yLexical = 3 * h;         // baseline of the lexical scale
        int yBar     = 4 * h;         // top of the bar
        int yLabel   = 8 * h;         // baseline of the labels
        int w  = (int)(distScale * 100.0);  // length of the bar
        int ws = (int)(distScale * 20.0);   // length of a segment
        
        /* white background */
        g2d.setColor(Color.WHITE);
        g2d.fillRect(xLeft-1, yBar-1, w+2, h+2);
        
        /* black foreground */
        g2d.setColor(Color.BLACK);
        g2d.drawRect(xLeft, yBar, w, h);
        g2d.fillRect(xLeft, yBar, ws, h);
        g2d.fillRect(xLeft+(int)(distScale * 40.0), yBar, ws, h);
        g2d.fillRect(xLeft+w-ws, yBar, ws, h);
        g2d.setFont(labelFont);
        paintText(g2d, "0", 0, yLabel);
        paintText(g2d, rightLabel, xRight, yLabel);
        
        /* lexical scale */
        int mapScale = getMapScale();
        String lexicalScale = tr("Scale") + " 1 : " + mapScale;

        Font scaleFront = new Font("Arial", Font.BOLD, FONT_SIZE);
        g2d.setFont(scaleFront);
        bound = g2d.getFontMetrics().getStringBounds(lexicalScale, g2d);
        int xLexical = Math.max(0, xLeft + (w - (int)bound.getWidth()) / 2);
        paintText(g2d, lexicalScale, xLexical, yLexical);
    }
    
    /** 
     * Paint an attribution text
     * 
     * @param g2d the graphics context to use for painting
     * @param pageFormat the size and orientation of the page being drawn
     */
    public void paintMapAttribution(Graphics2D g2d, PageFormat pageFormat) {
        String text = Main.pref.get("print.attribution", PrintPlugin.DEF_ATTRIBUTION);
        
        if (text == null) {
            return;
        }
            
        Font attributionFont = new Font("Arial", Font.PLAIN, FONT_SIZE * 8 / 10);
        g2d.setFont(attributionFont);

        text += "\n";
        int y = FONT_SIZE * 3 / 2;
        int from = 0;
        int to = text.indexOf("\n", from);
        while (to >= from) {
            String line = text.substring(from, to);

            Rectangle2D bound = g2d.getFontMetrics().getStringBounds(line, g2d);
            int x = (int)((pageFormat.getImageableWidth() - bound.getWidth()) - FONT_SIZE/2);
            
            paintText(g2d, line, x, y);

            y   += FONT_SIZE * 5 / 4;
            from = to + 1;
            to   = text.indexOf("\n", from);
        }
    }
    
    /**
     * Paint a text.
     * 
     * This method will not only draw the letters but also a background 
     * which improves redability.
     * 
     * @param g2d the graphics context to use for painting
     * @param text the text to be drawn
     * @param x the x coordinate
     * @param y the y coordinate
     */
    
    public void paintText(Graphics2D g2d, String text, int x, int y) {
            AffineTransform ax = g2d.getTransform();
            g2d.translate(x,y);

            FontRenderContext frc = g2d.getFontRenderContext();
            GlyphVector gv = g2d.getFont().createGlyphVector(frc, text);
            Shape textOutline = gv.getOutline();

            g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2d.setColor(Color.WHITE);
            g2d.draw(textOutline);
            
            g2d.setStroke(new BasicStroke());
            g2d.setColor(Color.BLACK);
            g2d.drawString(text, 0, 0);

            g2d.setTransform(ax);
    }

    /**
     * Get the active layer
     * 
     * This method is implemented in MapView but we need at this instance.
     */
    @Override
    public Layer getActiveLayer() {
        return mapView.getActiveLayer();
    }

    /**
     * Get the position of a particular layer in the stack of layers
     * 
     * This method is implemented in MapView but we need at this instance.
     */
    @Override
    public int getLayerPos(Layer layer) {
        return mapView.getLayerPos(layer);
    }
    
    /**
     * This method is implemented in MapView but protected.
     * 
     * The algorithm is slightly modified.
     */
    @Override
    protected List<Layer> getVisibleLayersInZOrder() {
        ArrayList<Layer> layers = new ArrayList<Layer>();
        for (Layer l: mapView.getAllLayersAsList()) {
            if (l.isVisible()) {
                layers.add(l);
            }
        }
        Collections.sort(
            layers,
            new Comparator<Layer>() {
                @Override
                public int compare(Layer l2, Layer l1) { // l1 and l2 swapped!
                    if (l1 instanceof OsmDataLayer && l2 instanceof OsmDataLayer) {
                        if (l1 == getActiveLayer()) return -1;
                        if (l2 == getActiveLayer()) return 1;
                        return Integer.valueOf(getLayerPos(l1)).
                                     compareTo(getLayerPos(l2));
                    } else
                        return Integer.valueOf(getLayerPos(l1)).
                                     compareTo(getLayerPos(l2));
                }
            }
        );
        return layers;
    }
}
