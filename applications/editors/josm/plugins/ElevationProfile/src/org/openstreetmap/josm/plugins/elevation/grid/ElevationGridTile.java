package org.openstreetmap.josm.plugins.elevation.grid;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.interfaces.TileCache;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.plugins.elevation.ElevationHelper;
import org.openstreetmap.josm.plugins.elevation.IVertexRenderer;
import org.openstreetmap.josm.plugins.elevation.gui.Triangle;

public class ElevationGridTile extends Tile {
    private BlockingDeque<EleVertex> toDo = new LinkedBlockingDeque<EleVertex>();
    private BlockingDeque<EleVertex> vertices = new LinkedBlockingDeque<EleVertex>();

    private Bounds box;
    
    public ElevationGridTile(TileSource source, int xtile, int ytile, int zoom) {
	super(source, xtile, ytile, zoom);

	box = tile2Bounds(xtile, ytile, zoom);
	initQueue();
    }

    public ElevationGridTile(TileSource source, int xtile, int ytile, int zoom,
	    BufferedImage image) {
	super(source, xtile, ytile, zoom, image);
	
	
    }

    @Override
    public void loadPlaceholderFromCache(TileCache cache) {
	// TODO Auto-generated method stub
	super.loadPlaceholderFromCache(cache);
	
	//System.out.println("loadPlaceholderFromCache");
    }

    @Override
    public String getUrl() throws IOException {
	// TODO Auto-generated method stub
	return super.getUrl();
    }

    /**
     * Use {@link ElevationGridTile#paintTile(Graphics2D, MapView, IVertexRenderer)} to render the tile as grid. This method just issues a debug text.
     */
    @Override
    public void paint(Graphics g, int x, int y) {
	super.paint(g, x, y);
	
	//g.drawString(String.format("EGT %d/%d ", getXtile(), getYtile()), x, y);
	g.drawString(getStatus(), x, y);
    }
    
    /**
     * Paints the vertices of this tile.
     *
     * @param g the graphics context
     * @param mv the map view
     * @param vertexRenderer the vertex renderer
     */
    public void paintTile(Graphics2D g, MapView mv, IVertexRenderer vertexRenderer) {
	BlockingDeque<EleVertex> list = getVertices();
	for (EleVertex eleVertex : list) {
	    Point p0 = mv.getPoint(eleVertex.get(0));
	    Point p1 = mv.getPoint(eleVertex.get(1));
	    Point p2 = mv.getPoint(eleVertex.get(2));
	    Triangle shape = new Triangle(p0, p1, p2);

	    // obtain vertex color
	    g.setColor(vertexRenderer.getElevationColor(eleVertex));
	    // TODO: Move to renderer
	    g.fill(shape);
	}
    }

    @Override
    public void loadImage(InputStream input) throws IOException {
	if (isLoaded()) return;	
	
	// We abuse the loadImage method to render the vertices...
	// 
	while (toDo.size() > 0) {
	    EleVertex vertex = toDo.poll();
	    
	    if (vertex.isFinished()) {		
		vertices.add(vertex);
	    } else {
		List<EleVertex> newV = vertex.divide();
		for (EleVertex eleVertex : newV) {
		    toDo.add(eleVertex);
		}
	    }
	}
	setLoaded(true);
    }
    
    public BlockingDeque<EleVertex> getVertices() {
        return vertices;
    }

    private Bounds tile2Bounds(final int x, final int y, final int zoom) {
	Bounds bb = new Bounds(
		new LatLon(source.tileYToLat(y, zoom), source.tileXToLon(x, zoom)),
		new LatLon(source.tileYToLat(y + 1, zoom), source.tileXToLon(x + 1, zoom)));

	return bb;
    }

    /**
     * Inits the 'todo' queue with the initial vertices.
     */
    private void initQueue() {
	LatLon min = box.getMin();
	LatLon max = box.getMax();
	
	// compute missing coordinates
	LatLon h1 = new LatLon(min.lat(), max.lon()); 
	LatLon h2 = new LatLon(max.lat(), min.lon());
	
	// compute elevation coords
	EleCoordinate p0 = new EleCoordinate(min, ElevationHelper.getElevation(min));	
	EleCoordinate p1 = new EleCoordinate(h1, ElevationHelper.getElevation(h1));
	EleCoordinate p2 = new EleCoordinate(max, ElevationHelper.getElevation(max));
	EleCoordinate p3 = new EleCoordinate(h2, ElevationHelper.getElevation(h2));
		
	// compute initial vertices
	EleVertex v1 = new EleVertex(p0, p1, p2);
	EleVertex v2 = new EleVertex(p2, p3, p0);
	// enqueue vertices
	toDo.add(v1);
	toDo.add(v2);
    }

    @Override
    public String toString() {
	return "ElevationGridTile [box=" + box + ", xtile=" + xtile
		+ ", ytile=" + ytile + "]";
    }
    
    
}
