package org.openstreetmap.josm.plugins.routes;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.mapmode.MapMode;
import org.openstreetmap.josm.command.ChangeCommand;
import org.openstreetmap.josm.command.RemoveRelationMemberCommand;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MapFrame;
import org.openstreetmap.josm.tools.Shortcut;

public class RelationEditMode extends MapMode {
	private static final long serialVersionUID = -7767329767438266289L;
	
	private Way highlightedWay;
	
	public RelationEditMode(MapFrame mapFrame) {
        super(tr("Edit relation"), "node/autonode", tr("Edit relations"),
                Shortcut.registerShortcut("mapmode:editRelation", tr("Mode: {0}", tr("Edit relation")), KeyEvent.VK_H, Shortcut.GROUP_EDIT),
                mapFrame, Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
    @Override
	public void enterMode() {
    	super.enterMode();
    	Main.map.mapView.addMouseListener(this);
    	Main.map.mapView.addMouseMotionListener(this);
    }
    
    @Override
	public void exitMode() {
    	super.exitMode();
    	Main.map.mapView.removeMouseListener(this);
    	Main.map.mapView.removeMouseMotionListener(this);
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
    	Way nearestWay = Main.map.mapView.getNearestWay(e.getPoint());
    	if (nearestWay != highlightedWay) {
    		if (highlightedWay != null) {
    			highlightedWay.highlighted = false;
    		}
    		if (nearestWay != null) {
    			nearestWay.highlighted = true;
    		}
    		highlightedWay = nearestWay;
			Main.map.mapView.repaint();	
    	}
    }
    
    @Override
	public void mouseClicked(MouseEvent e) {
    	Way way = Main.map.mapView.getNearestWay(e.getPoint());
    	
    	Collection<OsmPrimitive> selectedRelations = Main.main.getCurrentDataSet().getSelectedRelations();
    	
    	if (way != null) {
    		
    		if (selectedRelations.isEmpty()) {
    			JOptionPane.showMessageDialog(Main.parent, tr("No relation is selected"));
    		}
    		
    		for (OsmPrimitive rel:selectedRelations) {
    			Relation r = (Relation)rel;
    			RelationMember foundMember = null;
    			for (RelationMember member:r.getMembers()) {
    				if (member.getMember() == way) {
    					foundMember = member;
    					break;
    				}
    			}
    			
    			if (foundMember != null) {
    				Main.main.undoRedo.add(new RemoveRelationMemberCommand(r, new RelationMember("", way)));
    			} else {
    				Relation newRelation = new Relation(r);
    				newRelation.addMember(new RelationMember("", way));
    	            Main.main.undoRedo.add(new ChangeCommand(r, newRelation));
    			}
    		}
    	}
    }

	

}
