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
implements MapViewPaintable, AWTEventListener, SelectionChangedListener
{
	enum Mode {None, Drawing, DrawingWidth}
	final private Cursor cursorCrosshair;
	final private Cursor cursorJoinNode;
	private Cursor currCursor;
	
	private static double width = 0;
	private static double lenstep = 0;
	private static boolean useAddr;
	
	private Mode mode = Mode.None;
	private EastNorth p1,p2,p3;
	private Color selectedColor;
	private Point mousePos;
	
	private Point drawStartPos;
	
	Building building = new Building();
	
	public static void SetAddrDialog(boolean _useAddr) {
		useAddr = _useAddr;
	}
	public static void SetSizes(double newwidth,double newlenstep) {
		width = newwidth;
		lenstep = newlenstep;
	}
	public static double getWidth() {
		return width;
	}
	
	public static double getLenStep() {
		return lenstep;
	}
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
		UpdateConstraint(getCurrentDataSet().getSelected());
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
		Main.map.mapView.repaint();
	}

	public void eventDispatched(AWTEvent arg0) {
		if (!(arg0 instanceof KeyEvent)) return;
		KeyEvent ev = (KeyEvent)arg0;
		if (ev.getKeyCode() == KeyEvent.VK_ESCAPE)
			cancelDrawing();
	}

	private void ProcessMouseEvent(MouseEvent e) {
		mousePos = e.getPoint();
		if (mode == Mode.None) return;
		Node n;
		if (mode == Mode.Drawing) {
			if (e.isControlDown()) {
				n = null;
			} else { 
				n = Main.map.mapView.getNearestNode(mousePos);
			}
			if (n == null) {
				p2 = latlon2eastNorth(Main.map.mapView.getLatLon(mousePos.x, mousePos.y));
			} else {
				p2 = latlon2eastNorth(n.getCoor());
			}
			building.setPlace(p2, width, e.isShiftDown()?0:lenstep,e.isShiftDown());
			Main.map.statusLine.setDist(building.getLength());
			return;
		}
		if (mode == Mode.DrawingWidth) {
			if (e.isControlDown()) { 
				n = null;
			} else {
				n = Main.map.mapView.getNearestNode(mousePos);
			}
			if (n == null) {
				p3 = latlon2eastNorth(Main.map.mapView.getLatLon(mousePos.x, mousePos.y));
			} else {
				p3 = latlon2eastNorth(n.getCoor());
			}
			double mwidth =
				((p3.east()-p2.east())*(p2.north()-p1.north())+
				 (p3.north()-p2.north())*(p1.east()-p2.east()))
				/p1.distanceSq(p2) * building.getLength();
			
			building.setWidth(mwidth);
			Main.map.statusLine.setDist(Math.abs(mwidth));			
			return;
		}
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
		
		Node n = Main.map.mapView.getNearestNode(mousePos);
		if (n == null) {
			p1 = latlon2eastNorth(Main.map.mapView.getLatLon(mousePos.x, mousePos.y));
			building.setBase(p1);
		} else {
			p1 = latlon2eastNorth(n.getCoor());
			building.setBase(n);
		}
		mode = Mode.Drawing;
		updateStatusLine();
	}

	private void drawingAdvance(MouseEvent e) {
		ProcessMouseEvent(e);
		if (building.getLength() > 0) {
			if (width == 0 && mode == Mode.Drawing) {
				p2 = building.Point2();
				mode = Mode.DrawingWidth;
				updateStatusLine();
				return;
			}
			Way w = building.create();
			if (w != null && useAddr)
				showAddrDialog(w);
		}
		Main.map.mapView.repaint();
		mode = Mode.None;
		Main.map.statusLine.setHeading(-1);
  		Main.map.statusLine.setAngle(-1);
  		building.reset();
		updateStatusLine();
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1) return;
		if(!Main.map.mapView.isActiveLayerDrawable()) return;		

		if (mode == Mode.None)
			drawingStart(e);
	}

	@Override public void mouseDragged(MouseEvent e) {
		ProcessMouseEvent(e);
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

		if ((mode == Mode.Drawing && dragged) || mode == Mode.DrawingWidth)
			drawingAdvance(e);
	}
	
	private void updCursor() {
		if (mousePos==null) return;
		Node n = Main.map.mapView.getNearestNode(mousePos);
		if (n != null) setCursor(cursorJoinNode); else setCursor(cursorCrosshair);

	}
	@Override public void mouseMoved(MouseEvent e) {
		if(!Main.map.mapView.isActiveLayerDrawable()) return;
		ProcessMouseEvent(e);
		updCursor();
		if (mode!=Mode.None) Main.map.mapView.repaint();
	}

	@Override public String getModeHelpText() {
		if (mode==Mode.None) return tr("Point on angle of building to start drawing");
		if (mode==Mode.Drawing) return tr("Point on opposite end of building");
		if (mode==Mode.DrawingWidth) return tr("Set width of building");
		return "";
	}

	@Override public boolean layerIsSupported(Layer l) {
		return l instanceof OsmDataLayer;
	}
	
	public void UpdateConstraint(Collection<? extends OsmPrimitive> newSelection) {
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
		UpdateConstraint(newSelection);
	}
}
