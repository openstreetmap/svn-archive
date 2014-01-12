/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.openstreetmap.josm.plugins.elevation.grid;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.Action;
import javax.swing.Icon;

import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.elevation.IVertexRenderer;
import org.openstreetmap.josm.tools.ImageProvider;

/**
 * @author Oliver Wieland <oliver.wieland@online.de>
 *
 */
public class ElevationGridLayer extends Layer implements TileLoaderListener {
    private static final int ELE_ZOOM_LEVEL = 13;
    private IVertexRenderer vertexRenderer;    
    private MemoryTileCache tileCache;
    protected TileSource tileSource;
    protected ElevationGridTileLoader tileLoader;
    protected TileController tileController;
    
    private Bounds lastBounds;
    private TileSet tileSet;

    /**
     * @param info
     */
    public ElevationGridLayer(String name) {
	super(name);
	
	setOpacity(0.8);
	setBackgroundLayer(true);
	vertexRenderer = new SimpleVertexRenderer();
	
	tileCache = new MemoryTileCache();
	tileCache.setCacheSize(500);
	tileSource = new ElevationGridTileSource(name);
	tileLoader = new ElevationGridTileLoader(this);
	tileController = new ElevationGridTileController(tileSource, tileCache, this, tileLoader);
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#paint(java.awt.Graphics2D, org.openstreetmap.josm.gui.MapView, org.openstreetmap.josm.data.Bounds)
     */
    @Override
    public void paint(Graphics2D g, MapView mv, Bounds box) {
	boolean needsNewTileSet = tileSet == null || (lastBounds == null || !lastBounds.equals(box)); 
	
	if (needsNewTileSet) {
	    tileSet = new TileSet(box.getMin(), box.getMax(), ELE_ZOOM_LEVEL); // we use a vector format with constant zoom level
	    lastBounds = box;
	    System.out.println("paint " + tileSet);
	}
	
	if (tileSet.insane()) {
            myDrawString(g, tr("zoom in to load any tiles"), 120, 120);
            return;
        } else if (tileSet.tooLarge()) {
            myDrawString(g, tr("zoom in to load more tiles"), 120, 120);
            return;
        } else if (tileSet.tooSmall()) {
            myDrawString(g, tr("increase zoom level to see more detail"), 120, 120);
            return;
        }
	
	for(int x = tileSet.x0; x <= tileSet.x1; x++) {
	    for(int y = tileSet.y0; y <= tileSet.y1; y++) {	
		Tile t = tileController.getTile(x, y, ELE_ZOOM_LEVEL);
		
		if (t != null && t.isLoaded() && t instanceof ElevationGridTile) {
		    ((ElevationGridTile)t).paintTile(g, mv, vertexRenderer);        		
		} else {
		    // give some consolation...
		    Point topLeft = mv.getPoint(
			    new LatLon(tileSource.tileYToLat(y, ELE_ZOOM_LEVEL),
				       tileSource.tileXToLon(x, ELE_ZOOM_LEVEL)));
		    t.paint(g, topLeft.x, topLeft.y);		    
		}
	    }
	}
    }
    
    
    
    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#getToolTipText()
     */
    @Override
    public String getToolTipText() {
	// TODO Auto-generated method stub
	return null;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#visitBoundingBox(org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor)
     */
    @Override
    public void visitBoundingBox(BoundingXYVisitor v) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#getMenuEntries()
     */
    @Override
    public Action[] getMenuEntries() {
	// TODO Auto-generated method stub
	return null;
    }
   
    @Override
    public void tileLoadingFinished(Tile tile, boolean success) {
	try {
	    //System.out.println("tileLoadingFinished " + tile + ", success = " + success);
	    if (Main.map != null) {
		Main.map.repaint(100);
	    }
	} catch(Exception ex) {
	    System.err.println(ex);
	    ex.printStackTrace(System.err);
	}
    }

    @Override
    public TileCache getTileCache() {
	// TODO Auto-generated method stub
	return tileCache;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#getIcon()
     */
    @Override
    public Icon getIcon() {	
	return ImageProvider.get("layer", "elevation");
    }


    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#mergeFrom(org.openstreetmap.josm.gui.layer.Layer)
     */
    @Override
    public void mergeFrom(Layer from) {
	// TODO Auto-generated method stub
	
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#isMergable(org.openstreetmap.josm.gui.layer.Layer)
     */
    @Override
    public boolean isMergable(Layer other) {
	// TODO Auto-generated method stub
	return false;
    }

    /* (non-Javadoc)
     * @see org.openstreetmap.josm.gui.layer.Layer#getInfoComponent()
     */
    @Override
    public Object getInfoComponent() {
	// TODO Auto-generated method stub
	return null;
    }
    
    
    // Stolen from TMSLayer...
    void myDrawString(Graphics g, String text, int x, int y) {
        Color oldColor = g.getColor();
        g.setColor(Color.black);
        g.drawString(text,x+1,y+1);
        g.setColor(oldColor);
        g.drawString(text,x,y);
    }
    
    private class TileSet {
        int x0, x1, y0, y1;
        int tileMax = -1;

        /**
         * Create a TileSet by known LatLon bbox without layer shift correction
         */
        public TileSet(LatLon topLeft, LatLon botRight, int zoom) {
            if (zoom == 0)
                return;

            x0 = (int)tileSource.lonToTileX(topLeft.lon(),  zoom);
            y0 = (int)tileSource.latToTileY(topLeft.lat(),  zoom);
            x1 = (int)tileSource.lonToTileX(botRight.lon(), zoom);
            y1 = (int)tileSource.latToTileY(botRight.lat(), zoom);
            if (x0 > x1) {
                int tmp = x0;
                x0 = x1;
                x1 = tmp;
            }
            if (y0 > y1) {
                int tmp = y0;
                y0 = y1;
                y1 = tmp;
            }
            tileMax = (int)Math.pow(2.0, zoom);
            if (x0 < 0) {
                x0 = 0;
            }
            if (y0 < 0) {
                y0 = 0;
            }
            if (x1 > tileMax) {
                x1 = tileMax;
            }
            if (y1 > tileMax) {
                y1 = tileMax;
            }
        }
        
        int size() {
            int x_span = x1 - x0 + 1;
            int y_span = y1 - y0 + 1;
            return x_span * y_span;
        }

	@Override
	public String toString() {
	    return "TileSet [x0=" + x0 + ", x1=" + x1 + ", y0=" + y0 + ", y1="
		    + y1 + ", size()=" + size() + ", tilesSpanned()="
		    + tilesSpanned() + "]";
	}

	double tilesSpanned() {
	    return Math.sqrt(1.0 * this.size());
	}

	boolean tooSmall() {
	    return this.tilesSpanned() < 1;
	}

	boolean tooLarge() {
	    return this.tilesSpanned() > 50;
	}

	boolean insane() {
	    return this.tilesSpanned() > 200;
	}
    }
}
