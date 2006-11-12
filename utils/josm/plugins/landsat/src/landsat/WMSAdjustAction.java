package landsat; 

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.tools.ImageProvider;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.gui.IconToggleButton;

public class WMSAdjustAction extends MapMode implements
		MouseListener, MouseMotionListener{

	WMSImage selectedImage; 
	boolean mouseDown;
	EastNorth prevEastNorth;

	public WMSAdjustAction(MapFrame mapFrame) {
		super("landsatAdjust", "movelandsat", 
						"Adjust the position of the WMS layer", mapFrame, 
						ImageProvider.getCursor("normal", "move"));
	}

	@Override public void enterMode() {
		super.enterMode();
		Main.map.mapView.addMouseListener(this);
		Main.map.mapView.addMouseMotionListener(this);
	}

	@Override public void exitMode() {
		super.exitMode();
		Main.map.mapView.removeMouseListener(this);
		Main.map.mapView.removeMouseMotionListener(this);
	}

	@Override public void mousePressed(MouseEvent e) {
		if (e.getButton() != MouseEvent.BUTTON1)
			return;

		 for(Layer layer:Main.map.mapView.getAllLayers()) {
			if (layer instanceof WMSLayer) {
				prevEastNorth=Main.map.mapView.getEastNorth(e.getX(),e.getY());
				selectedImage = ((WMSLayer)layer).findImage(prevEastNorth);
				if(selectedImage!=null){
					Main.map.mapView.setCursor
						(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				}
			}
		}
	}

	@Override public void mouseDragged(MouseEvent e) {
			/*
		if (e.getButton() != MouseEvent.BUTTON1)
			return;
			*/

		if(selectedImage!=null) {
			EastNorth eastNorth=
					Main.map.mapView.getEastNorth(e.getX(),e.getY());
			if(selectedImage.contains(eastNorth)) {
				selectedImage.displace(eastNorth.east()-prevEastNorth.east(), 
									eastNorth.north()-prevEastNorth.north());
				prevEastNorth = eastNorth;
			}
			Main.map.mapView.repaint();
		}
	}

	@Override public void mouseReleased(MouseEvent e) {
		Main.map.mapView.repaint();
		Main.map.mapView.setCursor(Cursor.getDefaultCursor());
		selectedImage = null;	
	}

	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}
	public void mouseMoved(MouseEvent e) {
	}

	@Override public void mouseClicked(MouseEvent e) {
	}
}
