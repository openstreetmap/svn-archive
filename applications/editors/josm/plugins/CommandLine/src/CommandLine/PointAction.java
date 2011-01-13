/*
 *	  PointAction.java
 *	  
 *	  Copyright 2010 Hind <foxhind@gmail.com>
 *	  
 */

package commandline;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.SelectionChangedListener;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

public class PointAction extends MapMode implements AWTEventListener {
	private CommandLine parentPlugin;
	final private Cursor cursorCrosshair;
	final private Cursor cursorJoinNode;
	private Cursor currentCursor;
	private Point mousePos;
	private Node nearestNode;
	private ArrayList<String> pointList;
	private boolean isCtrlDown;

	public PointAction(MapFrame mapFrame, CommandLine parentPlugin) {
		super(null, "addsegment.png", null, mapFrame, ImageProvider.getCursor("crosshair", null));
		this.parentPlugin = parentPlugin;
		cursorCrosshair = ImageProvider.getCursor("crosshair", null);
		cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
		currentCursor = cursorCrosshair;
		nearestNode = null;
		pointList = new ArrayList<String>();
	}

	@Override public void enterMode() {
		super.enterMode();
		if (getCurrentDataSet() == null) {
			Main.map.selectSelectTool(false);
			return;
		}
		currentCursor = cursorCrosshair;
		Main.map.mapView.addMouseListener(this);
		Main.map.mapView.addMouseMotionListener(this);
		try {
			Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
		} catch (SecurityException ex) {
		}
	}

	@Override public void exitMode() {
		super.exitMode();
		Main.map.mapView.removeMouseListener(this);
		Main.map.mapView.removeMouseMotionListener(this);
		try {
			Toolkit.getDefaultToolkit().removeAWTEventListener(this);
		} catch (SecurityException ex) {
		}
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (isCtrlDown) {
				if (pointList.size() > 0) {
					pointList.remove(pointList.size() - 1);
					updateTextEdit();
				}
			}
			else {
				LatLon coor;
				if (nearestNode == null)
					coor = Main.map.mapView.getLatLon(e.getX(), e.getY());
				else
					coor = nearestNode.getCoor();
				if (coor.isOutSideWorld()) {
					JOptionPane.showMessageDialog(Main.parent,tr("Can not draw outside of the world."));
					return;
				}
				String point = String.valueOf(coor.getX()) + "," + String.valueOf(coor.getY());
				int maxInstances = parentPlugin.currentCommand.parameters.get(parentPlugin.currentCommand.currentParameterNum).maxInstances;
				if (maxInstances == 1) {
					parentPlugin.loadParameter(point, true);
					exitMode();
				}
				else {
					if (pointList.size() < maxInstances || maxInstances == 0) {
						pointList.add(point);
						updateTextEdit();
					}
					else
						System.out.println("Maximum instances!");
				}
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (!Main.map.mapView.isActiveLayerDrawable())
			return;
		processMouseEvent(e);
		updCursor();
		Main.map.mapView.repaint();
	}

	public void eventDispatched(AWTEvent arg0) {
		if (!(arg0 instanceof KeyEvent))
			return;
		KeyEvent ev = (KeyEvent) arg0;
		isCtrlDown = (ev.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0;
		if (ev.getKeyCode() == KeyEvent.VK_ESCAPE && ev.getID() == KeyEvent.KEY_PRESSED) {
			ev.consume();
			cancelDrawing();
		}
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

	private void processMouseEvent(MouseEvent e) {
		if (e != null) {
			mousePos = e.getPoint();
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
					if (!(Main.map.mapMode instanceof PointAction))
						return;
					Main.map.mapView.setCursor(c);
				}
			});
			currentCursor = c;
		} catch (Exception e) {
		}
	}

	public void cancelDrawing() {
		if (Main.map == null || Main.map.mapView == null)
			return;
		Main.map.statusLine.setHeading(-1);
		Main.map.statusLine.setAngle(-1);
		Main.map.mapView.repaint();
		updateStatusLine();
		parentPlugin.endInput();
	}

	public String currentValue() {
		String out = "";
		boolean first = true;
		for (String point : pointList) {
			if (!first)
				out += ";";
			out += point;
			first = false;
		}
		return out;
	}
	
	private void updateTextEdit() {
		Parameter currentParameter = parentPlugin.currentCommand.parameters.get(parentPlugin.currentCommand.currentParameterNum);
		String prefix = tr(currentParameter.description);
		prefix += parentPlugin.commandSymbol;
		String value = currentValue();
		parentPlugin.textField.setText(prefix + value);
	}
}
