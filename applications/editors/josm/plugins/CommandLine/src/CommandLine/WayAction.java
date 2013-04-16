/*
 *      WayAction.java
 *      
 *      Copyright 2011 Hind <foxhind@gmail.com>
 *      
 */

package CommandLine;

import java.awt.AWTEvent;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.ImageProvider;

public class WayAction extends MapMode implements AWTEventListener {
	private CommandLine parentPlugin;
    final private Cursor cursorNormal, cursorActive;
    private Cursor currentCursor;
    private Point mousePos;
    private Way nearestWay;
    private boolean isCtrlDown;
    // private Type type;

	public WayAction(MapFrame mapFrame, CommandLine parentPlugin) {
		super(null, "addsegment.png", null, mapFrame, ImageProvider.getCursor("normal", "selection"));
		this.parentPlugin = parentPlugin;
/*
		this.type = type;
		switch (type) {
			case POINT:
				cursorNormal = ImageProvider.getCursor("crosshair", null);
				cursorActive = ImageProvider.getCursor("crosshair", "joinnode");
				break;
			case NODE:
				cursorNormal = ImageProvider.getCursor("normal", "selection");
				cursorActive = ImageProvider.getCursor("normal", "joinnode");
				break;
			case WAY:
*/
				cursorNormal = ImageProvider.getCursor("normal", "selection");
				cursorActive = ImageProvider.getCursor("normal", "joinway");
/*
				break;
			default:
				cursorNormal = ImageProvider.getCursor("normal", "selection");
				cursorActive = ImageProvider.getCursor("normal", null);
				break;
		}
*/
        currentCursor = cursorNormal;
        nearestWay = null;
	}

	@Override public void enterMode() {
		super.enterMode();
        currentCursor = cursorNormal;
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

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable())
            return;
        processMouseEvent(e);
        updCursor();
        Main.map.mapView.repaint();
        super.mouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!Main.map.mapView.isActiveLayerDrawable())
            return;
        processMouseEvent(e);
        if (nearestWay != null) {
			if (isCtrlDown) {
				Main.main.getCurrentDataSet().clearSelection(nearestWay);
				Main.map.mapView.repaint();
			}
			else {
				int maxInstances = parentPlugin.currentCommand.parameters.get(parentPlugin.currentCommand.currentParameterNum).maxInstances;
				switch (maxInstances) {
				case 0:
					Main.main.getCurrentDataSet().addSelected(nearestWay);
					Main.map.mapView.repaint();
					break;
				case 1:
					Main.main.getCurrentDataSet().addSelected(nearestWay);
					Main.map.mapView.repaint();
					parentPlugin.loadParameter(nearestWay, true);
					exitMode();
					break;
				default:
					if (Main.main.getCurrentDataSet().getSelected().size() < maxInstances) {
						Main.main.getCurrentDataSet().addSelected(nearestWay);
						Main.map.mapView.repaint();
					}
					else
						System.out.println("Maximum instances!");
				}
			}
		}
        super.mousePressed(e);
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
			nearestWay = Main.map.mapView.getNearestWay(mousePos, OsmPrimitive.isUsablePredicate);
			if (nearestWay != null) {
				setCursor(cursorActive);
			}
			else {
				setCursor(cursorNormal);
			}
		}
    }

	private void processMouseEvent(MouseEvent e) {
		if (e != null) { mousePos = e.getPoint(); }
	}

    private void setCursor(final Cursor c) {
        if (currentCursor.equals(c))
            return;
        try {
            // We invoke this to prevent strange things from happening
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    // Don't change cursor when mode has changed already
                    if (!(Main.map.mapMode instanceof WayAction))
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
        parentPlugin.abortInput();
    }
}
