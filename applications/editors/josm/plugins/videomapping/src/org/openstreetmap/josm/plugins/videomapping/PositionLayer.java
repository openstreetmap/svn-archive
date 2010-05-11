package org.openstreetmap.josm.plugins.videomapping;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

public class PositionLayer extends Layer implements MouseListener {
	private GpxData gps;
	public PositionLayer(String name,GpxData gps) {
		super(name);		
		this.gps = gps;
		Main.map.mapView.addMouseListener(this);
	}

	@Override
	public Icon getIcon() {
		return ImageProvider.get("videomapping.png");
	}

	@Override
	public Object getInfoComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Component[] getMenuEntries() {
        return new Component[]{
                new JMenuItem(LayerListDialog.getInstance().createShowHideLayerAction(this)),
                new JMenuItem(LayerListDialog.getInstance().createDeleteLayerAction(this)),
                new JSeparator(),
                //TODO here my stuff
                new JSeparator(),
                new JMenuItem(new LayerListPopup.InfoAction(this))
         };
	}
	  


	@Override
	public String getToolTipText() {
		return tr("Shows current position in the video");
	}

	@Override
	public boolean isMergable(Layer arg0) {
		return false;
	}

	@Override
	public void mergeFrom(Layer arg0) {
		// no merging nescesarry
	}

	@Override
	public void paint(Graphics2D g, MapView map, Bounds bound) {
		//TODO this is just an blue screen test
		g.setColor(Color.BLUE);
		g.fillRect(0, 0, map.getWidth(),map.getHeight());
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor arg0) {
		// TODO dunno what to do here

	}

	//jump to the right position in video
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(Main.map.mapView.getActiveLayer() == this) {
				//only on leftclicks of our layer
				getNearestPoint(e.getPoint());
			}
		}
		
	}

	//finds the corresponding timecode in GPXtrack
	private Point getNearestPoint(Point point) {
		Main.map.mapView.getn
		return point;
		
	}

	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
