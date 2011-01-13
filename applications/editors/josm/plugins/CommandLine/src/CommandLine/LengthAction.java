/*
 *	  LengthAction.java
 *
 *	  Copyright 2010 Hind <foxhind@gmail.com>
 *
 */

package commandline;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.LinkedList;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.coor.*;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.layer.MapViewPaintable;
import org.openstreetmap.josm.gui.layer.OsmDataLayer;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.tools.Shortcut;

public class LengthAction extends MapMode implements MapViewPaintable, AWTEventListener {
	private CommandLine parentPlugin;
	final private Cursor cursorCrosshair;
	final private Cursor cursorJoinNode;
	private Cursor currentCursor;
	private Color selectedColor;
	private Point drawStartPos;
	private Point drawEndPos;
	private LatLon startCoor;
	private LatLon endCoor;
	private Point mousePos;
	private Node nearestNode;
	private boolean drawing;

	public LengthAction(MapFrame mapFrame, CommandLine parentPlugin) {
		super(null, "building", null, mapFrame, ImageProvider.getCursor("crosshair", null));
		this.parentPlugin = parentPlugin;
		selectedColor = Main.pref.getColor(marktr("selected"), Color.red);
		cursorCrosshair = ImageProvider.getCursor("crosshair", null);
		cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
		currentCursor = cursorCrosshair;
		nearestNode = null;
	}

	@Override
	public void enterMode() {
		super.enterMode();
		Main.map.mapView.addMouseListener(this);
		Main.map.mapView.addMouseMotionListener(this);
		Main.map.mapView.addTemporaryLayer(this);
		try {
			Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
		} catch (SecurityException ex) {
		}
	}

	@Override
	public void exitMode() {
		super.exitMode();
		Main.map.mapView.removeMouseListener(this);
		Main.map.mapView.removeMouseMotionListener(this);
		Main.map.mapView.removeTemporaryLayer(this);
		try {
			Toolkit.getDefaultToolkit().removeAWTEventListener(this);
		} catch (SecurityException ex) {
		}
		if (drawing)
			Main.map.mapView.repaint();
	}

	public void cancelDrawing() {
		if (Main.map == null || Main.map.mapView == null)
			return;
		Main.map.statusLine.setHeading(-1);
		Main.map.statusLine.setAngle(-1);
		updateStatusLine();
		parentPlugin.endInput();
	}

	public void eventDispatched(AWTEvent arg0) {
		if (!(arg0 instanceof KeyEvent))
			return;
		KeyEvent ev = (KeyEvent) arg0;
		if (ev.getKeyCode() == KeyEvent.VK_ESCAPE && ev.getID() == KeyEvent.KEY_PRESSED) {
			if (drawing)
				ev.consume();
			cancelDrawing();
		}
	}

	private void processMouseEvent(MouseEvent e) {
		if (e != null) {
			mousePos = e.getPoint();
		}
	}

	public void paint(Graphics2D g, MapView mv, Bounds bbox) {
		if (!drawing)
			return;

		g.setColor(selectedColor);
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		GeneralPath b = new GeneralPath();
		Point pp1 = drawStartPos;
		Point pp2 = drawEndPos;

		b.moveTo(pp1.x, pp1.y);
		b.lineTo(pp2.x, pp2.y);
		g.draw(b);

		g.setStroke(new BasicStroke(1));
	}

	private void drawingStart(MouseEvent e) {
		mousePos = e.getPoint();
		if (nearestNode != null) {
			drawStartPos = Main.map.mapView.getPoint(nearestNode.getCoor());
		} else {
			drawStartPos = mousePos;
		}
		drawEndPos = drawStartPos;
		startCoor = Main.map.mapView.getLatLon(drawStartPos.x, drawStartPos.y);
		endCoor = startCoor;
		drawing = true;
		updateStatusLine();
	}

	private void drawingFinish() {
		parentPlugin.loadParameter(String.valueOf(startCoor.greatCircleDistance(endCoor)), true);
		drawStartPos = null;
		drawing = false;
		exitMode();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
		  if (!Main.map.mapView.isActiveLayerDrawable())
			return;
		  drawingStart(e);
		}
		else
		  drawing = false;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	  if (e.getButton() != MouseEvent.BUTTON1)
		  return;
	  if (!Main.map.mapView.isActiveLayerDrawable())
		  return;
	  boolean dragged = true;
	  if (drawStartPos != null)
		  dragged = drawEndPos.distance(drawStartPos) > 10;
	  if (drawing && dragged)
		drawingFinish();
	  drawing = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		processMouseEvent(e);
		updCursor();
		if (nearestNode != null)
			drawEndPos = Main.map.mapView.getPoint(nearestNode.getCoor());
		else
			drawEndPos = mousePos;
		endCoor = Main.map.mapView.getLatLon(drawEndPos.x, drawEndPos.y);
    if (drawing) {
      Main.map.statusLine.setDist(startCoor.greatCircleDistance(endCoor));
			Main.map.mapView.repaint();
    }
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!Main.map.mapView.isActiveLayerDrawable())
			return;
		processMouseEvent(e);
		updCursor();
		if (drawing)
			Main.map.mapView.repaint();
	}

	@Override
	public String getModeHelpText() {
		if (drawing)
			return tr("Point on the start");
		else
			return tr("Point on the end");
	}

	@Override
	public boolean layerIsSupported(Layer l) {
		return l instanceof OsmDataLayer;
	}

	private void updCursor() {
		if (mousePos != null) {
			if (!Main.isDisplayingMapView())
				return;
			nearestNode = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isUsablePredicate);
			if (nearestNode != null) {
				setCursor(cursorJoinNode);
			}
			else {
				setCursor(cursorCrosshair);
			}
		}
	}

	private void setCursor(final Cursor c) {
		if (currentCursor.equals(c))
			return;
		try {
			// We invoke this to prevent strange things from happening
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					// Don't change cursor when mode has changed already
					if (!(Main.map.mapMode instanceof LengthAction))
						return;
					Main.map.mapView.setCursor(c);
				}
			});
			currentCursor = c;
		} catch (Exception e) {
		}
	}
}
