package org.openstreetmap.josm.plugins.measurement;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.gui.MapFrame;

public class MeasurementMode extends MapMode {

	private static final long serialVersionUID = 3853830673475744263L;
	
	public MeasurementMode(MapFrame mapFrame, String name, String desc) {
		super(name, "measurement.png", desc, mapFrame, Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));		
	}

	@Override public void enterMode() {
		super.enterMode();
		Main.map.mapView.addMouseListener(this);
	}

	@Override public void exitMode() {
		super.exitMode();
		Main.map.mapView.removeMouseListener(this);
	}

	/**
	 * If user clicked with the left button, add a node at the current mouse
	 * position.
	 *
	 * If in nodesegment mode, add the node to the line segment by splitting the
	 * segment. The new created segment will be inserted in every way the segment
	 * was part of.
	 */
	@Override public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3){
			MeasurementPlugin.getCurrentLayer().removeLastPoint();
		}else if (e.getButton() == MouseEvent.BUTTON1){
			LatLon coor = Main.map.mapView.getLatLon(e.getX(), e.getY());
			if (coor.isOutSideWorld()) {
				JOptionPane.showMessageDialog(Main.parent,tr("Can not draw outside of the world."));
				return;
			}
			if (MeasurementPlugin.currentLayer == null){
				Main.main.addLayer(new MeasurementLayer("Measurement"));
			}
			MeasurementPlugin.currentLayer.mouseClicked(e);
		}
	}

}
