package org.openstreetmap.josm.plugins.videomapping;


import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import static org.openstreetmap.josm.tools.I18n.*;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.gpx.GpxData;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.gui.dialogs.LayerListDialog;
import org.openstreetmap.josm.gui.dialogs.LayerListPopup;
import org.openstreetmap.josm.gui.layer.Layer;
import org.openstreetmap.josm.tools.ImageProvider;

public class PositionLayer extends Layer implements MouseListener {
	private DataSet ds;
	private Node sel;
	private Iterator<Node> it;
		
	public PositionLayer(String name, final DataSet ds) {
		super(name);		
		this.ds = ds;
		Main.map.mapView.addMouseListener(this);		
		Timer t  = new Timer();
		it=ds.getNodes().iterator();
		
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				sel=it.next();
				System.out.println(sel.getTimestamp());
				Main.map.mapView.repaint();
			}
		},1000,1000);
		
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

	// no merging nescesarry
	@Override
	public boolean isMergable(Layer arg0) {
		return false;
	}

	@Override
	public void mergeFrom(Layer arg0) {
		
	}

	
	
	@Override
	//Draw the current position
	public void paint(Graphics2D g, MapView map, Bounds bound) {
		Point p;
		for(Node n: ds.getNodes()) {
			p = Main.map.mapView.getPoint(n.getEastNorth());
			if(n.isHighlighted())
			{
				g.setColor(Color.red);
				g.drawString(n.getTimestamp().toString(), p.x+10, p.y);
			}
			else
			{
				g.setColor(Color.green);
			}
			g.drawOval(p.x - 2, p.y - 2, 4, 4); // small circles
		}
		if (sel!=null){
			p=Main.map.mapView.getPoint(sel.getEastNorth());;
			g.setColor(Color.CYAN);
			g.drawRect(p.x, p.y, 100, 100);
		};
	}

	@Override
	public void visitBoundingBox(BoundingXYVisitor arg0) {
		// TODO dunno what to do here

	}

	//jump to the right position in video
	public void mouseClicked(MouseEvent e) {		
		//only on leftclicks of our layer
		if(e.getButton() == MouseEvent.BUTTON1) {
			JOptionPane.showMessageDialog(Main.parent,"test");
			getNearestNode(e.getPoint());
			Main.map.mapView.repaint();
		}
		
	}

	//finds the corresponding timecode in GPXtrack by given screen coordinates
	private void getNearestNode(Point mouse) {
		Point p;
		Rectangle rect = new Rectangle(mouse, new Dimension(30, 30));		
		//iterate through all possible notes
		for(Node node : ds.getNodes())
		{
			p = Main.map.mapView.getPoint(node);
			if (rect.contains(p))
			{				
				node.setHighlighted(true);
			}
			
		}	
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
