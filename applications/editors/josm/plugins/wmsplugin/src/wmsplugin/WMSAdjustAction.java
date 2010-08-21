package wmsplugin;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;


public class WMSAdjustAction extends MapMode implements MouseListener, MouseMotionListener{
	//static private final Logger logger = Logger.getLogger(WMSAdjustAction.class.getName());

	GeorefImage selectedImage;
	boolean mouseDown;
	EastNorth prevEastNorth;
	private WMSLayer adjustingLayer;

	public WMSAdjustAction(MapFrame mapFrame) {
		super(tr("Adjust WMS"), "adjustwms",
				tr("Adjust the position of the selected WMS layer"), mapFrame,
				ImageProvider.getCursor("normal", "move"));
	}



	@Override public void enterMode() {
		super.enterMode();
		if (!Main.isDisplayingMapView() || !(Main.map.mapView.getActiveLayer() instanceof WMSLayer)) {
			return;
		}
		adjustingLayer = (WMSLayer) Main.map.mapView.getActiveLayer();
		if (!adjustingLayer.isVisible()) {
			adjustingLayer.setVisible(true);
		}
		Main.map.mapView.addMouseListener(this);
		Main.map.mapView.addMouseMotionListener(this);
	}

	@Override public void exitMode() {
		super.exitMode();
		Main.map.mapView.removeMouseListener(this);
		Main.map.mapView.removeMouseMotionListener(this);
		adjustingLayer = null;
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1)
			return;

		if (adjustingLayer.isVisible()) {
			prevEastNorth=Main.map.mapView.getEastNorth(e.getX(),e.getY());
			selectedImage = adjustingLayer.findImage(prevEastNorth);
			if(selectedImage!=null) {
				Main.map.mapView.setCursor
				(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
		}
	}

	@Override public void mouseDragged(MouseEvent e) {
		if(selectedImage!=null) {
			EastNorth eastNorth=
				Main.map.mapView.getEastNorth(e.getX(),e.getY());
			adjustingLayer.displace(
					eastNorth.east()-prevEastNorth.east(),
					eastNorth.north()-prevEastNorth.north()
			);
			prevEastNorth = eastNorth;
			Main.map.mapView.repaint();
		}
	}

	@Override public void mouseReleased(MouseEvent e) {
		Main.map.mapView.repaint();
		Main.map.mapView.setCursor(Cursor.getDefaultCursor());
		selectedImage = null;
		prevEastNorth = null;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}

	@Override public void mouseClicked(MouseEvent e) {
	}

	// This only makes the buttons look disabled, but since no keyboard shortcut is
	// provided there aren't any other means to activate this tool
	@Override public boolean layerIsSupported(Layer l) {
		return l instanceof WMSLayer;
	}

}
