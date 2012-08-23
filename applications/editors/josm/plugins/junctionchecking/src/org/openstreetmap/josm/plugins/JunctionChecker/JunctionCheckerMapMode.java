package org.openstreetmap.josm.plugins.JunctionChecker;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.MapView.LayerChangeListener;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.plugins.JunctionChecker.datastructure.ChannelDiGraph;

/**
 * Diese Klasse wurde aus der Klasse EditGpxMode Klasse des editGPX-Plugins erzeugt und nur an wenigen Stellen an
 * die eigenen Bedürfnisse angepaßt
 */
public class JunctionCheckerMapMode extends MapMode implements LayerChangeListener{

	MapFrame frame;
	Point pointPressed;
	ChannelDiGraphLayer layer;
	Rectangle oldRect;
	ChannelDiGraph digraph;

	private static final long serialVersionUID = 3442408951505263850L;

	public JunctionCheckerMapMode(MapFrame mapFrame, String name, String desc) {
		super(name, "junctionchecker.png", desc, mapFrame, Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	@Override public void enterMode() {
		super.enterMode();
		Main.map.mapView.addMouseListener(this);
		Main.map.mapView.addMouseMotionListener(this);
		MapView.addLayerChangeListener(this);
	}

	@Override public void exitMode() {
		super.exitMode();
		Main.map.mapView.removeMouseListener(this);
		Main.map.mapView.removeMouseMotionListener(this);
	}


	public void activeLayerChange(Layer oldLayer, Layer newLayer) {
		if (newLayer instanceof ChannelDiGraphLayer) {
			layer = (ChannelDiGraphLayer) newLayer;
		}
	}

	public void layerAdded(Layer newLayer) {
	}

	public void layerRemoved(Layer oldLayer) {
	}


	public void setFrame(MapFrame newFrame) {
		frame = newFrame;
	}

	@Override public void mousePressed(MouseEvent e) {
		pointPressed = new Point(e.getPoint());
	}


	@Override public void mouseDragged(MouseEvent e) {
		if ( (e.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) ==  InputEvent.BUTTON1_DOWN_MASK) {
			//if button1 is hold, draw the rectangle.
			paintRect(pointPressed, e.getPoint());
		}
	}

	@Override public void mouseReleased(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) {
			return;
		}
		digraph.ereaseJunctioncandidate();//um zu verhindern, dass gefundene Kreuzungen/Kandidaten weiterhin weiß gezeichnet werden
		Point pointReleased = e.getPoint();

		Rectangle r = createRect(pointReleased, pointPressed);
		boolean ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
		boolean shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
		boolean alt = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;
		if (shift == false) {
			digraph.ereaseSelectedChannels();
		}
		//go through nodes and mark the ones in the selection rect as deleted
		if (layer != null && digraph != null) {
			LatLon lefttop = Main.map.mapView.getLatLon(r.x + r.width, r.y + r.height);
			LatLon rightbottom = Main.map.mapView.getLatLon(r.x, r.y);
			digraph.detectSelectedChannels(rightbottom.lon(), rightbottom.lat(), lefttop.lon(), lefttop.lat());
		}
		oldRect = null;
		Main.map.mapView.repaint();

	}

	/**
	 * create rectangle out of two given corners
	 */
	public Rectangle createRect(Point p1, Point p2) {
		int x,y,w,h;
		if (p1.x == p2.x && p1.y == p2.y) {
			//if p1 and p2 same points draw a small rectangle around them
			x = p1.x -1;
			y = p1.y -1;
			w = 3;
			h = 3;
		} else {
			if (p1.x < p2.x){
				x = p1.x;
				w = p2.x-p1.x;
			} else {
				x = p2.x;
				w = p1.x-p2.x;
			}
			if (p1.y < p2.y) {
				y = p1.y;
				h = p2.y-p1.y;
			} else {
				y = p2.y;
				h = p1.y-p2.y;
			}
		}
		return new Rectangle(x,y,w,h);
	}

	/**
	 * Draw a selection rectangle on screen.
	 */
	private void paintRect(Point p1, Point p2) {
		if (frame != null) {
			Graphics g = frame.getGraphics();
	
			Rectangle r = oldRect;
			if (r != null) {
				//overwrite old rct
				g.setXORMode(Color.BLACK);
				g.setColor(Color.WHITE);
				g.drawRect(r.x,r.y,r.width,r.height);
			}
	
			g.setXORMode(Color.BLACK);
			g.setColor(Color.WHITE);
			r = createRect(p1,p2);
			g.drawRect(r.x,r.y,r.width,r.height);
			oldRect = r;
		}
	}

	public ChannelDiGraph getDigraph() {
		return digraph;
	}

	public void setDigraph(ChannelDiGraph digraph) {
		this.digraph = digraph;
	}

	@Override
	public void destroy() {
		super.destroy();
		MapView.removeLayerChangeListener(this);
	}
}
