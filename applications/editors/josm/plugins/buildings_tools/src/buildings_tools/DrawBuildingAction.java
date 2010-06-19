package buildings_tools;

import static org.openstreetmap.josm.tools.I18n.marktr;
import static org.openstreetmap.josm.tools.I18n.tr;

import static buildings_tools.BuildingsToolsPlugin.latlon2eastNorth;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;

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

@SuppressWarnings("serial")
public class DrawBuildingAction extends MapMode 
implements MapViewPaintable, AWTEventListener, SelectionChangedListener {
	enum Mode {None, Drawing, DrawingWidth, DrawingAngFix}
	final private Cursor cursorCrosshair;
	final private Cursor cursorJoinNode;
	private Cursor currCursor;
	
	private Mode mode = Mode.None;
	private Mode nextMode = Mode.None;
	
	private Color selectedColor;
	private Point mousePos;
	private Point drawStartPos;
	
	Building building = new Building();
	
	public DrawBuildingAction(MapFrame mapFrame) {
		super(tr("Draw buildings"),"building",tr("Draw buildings"),
				Shortcut.registerShortcut("mapmode:buildings",
						tr("Mode: {0}", tr("Draw buildings")),
						KeyEvent.VK_W, Shortcut.GROUP_EDIT),
				mapFrame,getCursor());
		
		cursorCrosshair = getCursor();
		cursorJoinNode = ImageProvider.getCursor("crosshair", "joinnode");
		currCursor = cursorCrosshair;
		
		selectedColor = Main.pref.getColor(marktr("selected"), Color.red);
	}
	private static Cursor getCursor() {
		try {
			return ImageProvider.getCursor("crosshair", null);
		} catch (Exception e) {
		}
		return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
	}
	/**
	 * Displays the given cursor instead of the normal one
	 * @param Cursors One of the available cursors
	 */
	private void setCursor(final Cursor c) {
		if(currCursor.equals(c))
			return;
		try {
			// We invoke this to prevent strange things from happening
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					// Don't change cursor when mode has changed already
					if(!(Main.map.mapMode instanceof DrawBuildingAction))
						return;
					Main.map.mapView.setCursor(c);
				}
			});
			currCursor = c;
		} catch(Exception e) {}
	}
	private static void showAddrDialog(Way w) {
		AddressDialog dlg = new AddressDialog();
		int answer = dlg.getValue();
		if (answer == 1) {
			dlg.saveValues();
			String tmp;
			tmp = dlg.getHouseNum();
			if (tmp!=null&&tmp!="") w.put("addr:housenumber",tmp);
			tmp = dlg.getStreetName();
			if (tmp!=null&&tmp!="") w.put("addr:street",tmp);
		}
	}

	@Override public void enterMode() {
		super.enterMode();
		if (getCurrentDataSet() == null) {
			Main.map.selectSelectTool(false);
			return;
		}
		currCursor = cursorCrosshair;
		Main.map.mapView.addMouseListener(this);
		Main.map.mapView.addMouseMotionListener(this);
		Main.map.mapView.addTemporaryLayer(this);
		DataSet.selListeners.add(this);
		updateConstraint(getCurrentDataSet().getSelected());
		try {
			Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.KEY_EVENT_MASK);
		} catch (SecurityException ex) { }
	}

	@Override public void exitMode() {
		super.exitMode();
		Main.map.mapView.removeMouseListener(this);
		Main.map.mapView.removeMouseMotionListener(this);
		Main.map.mapView.removeTemporaryLayer(this);
		DataSet.selListeners.remove(this);
		try {
			Toolkit.getDefaultToolkit().removeAWTEventListener(this);
		} catch (SecurityException ex) { }
		if (mode!=Mode.None) Main.map.mapView.repaint();
		mode = Mode.None;
	}
	
	public void cancelDrawing() {
		mode = Mode.None;
		if(Main.map == null || Main.map.mapView == null)
			return;
		Main.map.statusLine.setHeading(-1);
  		Main.map.statusLine.setAngle(-1);
  		building.reset();
		Main.map.mapView.repaint();
		updateStatusLine();
	}

	public void eventDispatched(AWTEvent arg0) {
		if (!(arg0 instanceof KeyEvent)) return;
		KeyEvent ev = (KeyEvent)arg0;
		if (ev.getKeyCode() == KeyEvent.VK_ESCAPE)
			cancelDrawing();
	}
	
	private EastNorth getPoint(MouseEvent e) {
		Node n;
		if (e.isControlDown()) {
			n = null;
		} else { 
			n = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isUsablePredicate);
		}
		if (n == null) {
			return latlon2eastNorth(Main.map.mapView.getLatLon(mousePos.x, mousePos.y));
		} else {
			return latlon2eastNorth(n.getCoor());
		}
	}
	
	private Mode modeDrawing(MouseEvent e) {
		EastNorth p = getPoint(e);
		if (building.isRectDrawing() && (!e.isShiftDown() || ToolSettings.isBBMode())) {
			building.setPlaceRect(p);
			return e.isShiftDown() ? Mode.DrawingAngFix : Mode.None;
		} else {
			building.setPlace(p, ToolSettings.getWidth(),
					ToolSettings.getLenStep(),e.isShiftDown());
			Main.map.statusLine.setDist(building.getLength());
			return this.nextMode = ToolSettings.getWidth() == 0? Mode.DrawingWidth : Mode.None;
		}
	}

	private Mode modeDrawingWidth(MouseEvent e) {
		building.setWidth(getPoint(e));
		Main.map.statusLine.setDist(Math.abs(building.getWidth()));
		return Mode.None;
	}

	private Mode modeDrawingAngFix(MouseEvent e) {
		building.angFix(getPoint(e));
		return Mode.None;
	}

	private void processMouseEvent(MouseEvent e) {
		mousePos = e.getPoint();
		if (mode == Mode.None) {
			nextMode = Mode.None;
			return;
		}

		if (mode == Mode.Drawing) {
			nextMode = modeDrawing(e);
		} else if (mode == Mode.DrawingWidth) {
			nextMode = modeDrawingWidth(e);
		} else if (mode == Mode.DrawingAngFix) {
			nextMode = modeDrawingAngFix(e);
		} else
			throw new AssertionError("Invalid drawing mode");
	}
	
	public void paint(Graphics2D g, MapView mv,Bounds bbox)
	{
		if (mode == Mode.None) return;
		if (building.getLength() == 0) return;
		
		g.setColor(selectedColor);
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		building.paint(g, mv);
		
		g.setStroke(new BasicStroke(1));

	}
	
	private void drawingStart(MouseEvent e)
	{
		mousePos = e.getPoint();
		drawStartPos = mousePos;
		
		Node n = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isUsablePredicate);
		if (n == null) {
			building.setBase(latlon2eastNorth(Main.map.mapView.getLatLon(mousePos.x, mousePos.y)));
		} else {
			building.setBase(n);
		}
		mode = Mode.Drawing;
		updateStatusLine();
	}

	private void drawingAdvance(MouseEvent e) {
		processMouseEvent(e);
		if (this.mode != Mode.None && this.nextMode == Mode.None) {
			drawingFinish();
		} else {
			mode = this.nextMode;
			updateStatusLine();
		}
	}
	
	private void drawingFinish() {
		if (building.getLength() != 0) {
			Way w = building.create();
			if (w != null && ToolSettings.isUsingAddr())
				showAddrDialog(w);
		}
		cancelDrawing();
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) return;
		if(!Main.map.mapView.isActiveLayerDrawable()) return;		

		if (mode == Mode.None)
			drawingStart(e);
	}

	@Override public void mouseDragged(MouseEvent e) {
		processMouseEvent(e);
		updCursor();
		if (mode!=Mode.None) Main.map.mapView.repaint();
	}

	@Override public void mouseReleased(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) return;
		if(!Main.map.mapView.isActiveLayerDrawable()) return;
		boolean dragged = true;
		if (drawStartPos != null)
			dragged = e.getPoint().distance(drawStartPos) > 10;
		drawStartPos = null;

		if (mode == Mode.Drawing && !dragged) return;
		if (mode == Mode.None) return;

		drawingAdvance(e);
	}
	
	private void updCursor() {
		if (mousePos==null) return;
		Node n = Main.map.mapView.getNearestNode(mousePos, OsmPrimitive.isUsablePredicate);
		if (n != null) setCursor(cursorJoinNode); else setCursor(cursorCrosshair);

	}
	@Override public void mouseMoved(MouseEvent e) {
		if(!Main.map.mapView.isActiveLayerDrawable()) return;
		processMouseEvent(e);
		updCursor();
		if (mode!=Mode.None) Main.map.mapView.repaint();
	}

	@Override public String getModeHelpText() {
		if (mode==Mode.None) return tr("Point on the corner of the building to start drawing");
		if (mode==Mode.Drawing) return tr("Point on opposite end of the building");
		if (mode==Mode.DrawingWidth) return tr("Set width of the building");
		return "";
	}

	@Override public boolean layerIsSupported(Layer l) {
		return l instanceof OsmDataLayer;
	}
	
	public void updateConstraint(Collection<? extends OsmPrimitive> newSelection) {
		building.disableAngConstraint();
		if (newSelection.size()!=2)return;
   		Object[] arr = newSelection.toArray();
   		if (!(arr[0] instanceof Node&&arr[1] instanceof Node)) return;
   		EastNorth p1,p2;
   		p1=latlon2eastNorth(((Node)arr[0]).getCoor());
   		p2=latlon2eastNorth(((Node)arr[1]).getCoor());
   		building.setAngConstraint(p1.heading(p2));
	}
	
	public void selectionChanged(Collection<? extends OsmPrimitive> newSelection) {
		updateConstraint(newSelection);
	}
}
