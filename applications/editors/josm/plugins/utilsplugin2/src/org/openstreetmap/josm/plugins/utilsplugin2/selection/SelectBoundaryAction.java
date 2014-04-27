// License: GPL. Copyright 2011 by Alexei Kasatkin
package org.openstreetmap.josm.plugins.utilsplugin2.selection;

import java.awt.Point;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import org.openstreetmap.josm.Main;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.Notification;
import org.openstreetmap.josm.tools.Geometry;

import org.openstreetmap.josm.tools.Shortcut;

/**
 *    Extends current selection by selecting nodes on all touched ways
 */
public class SelectBoundaryAction extends JosmAction {
    private Way lastUsedStartingWay; //used for repeated calls
    private boolean lastUsedLeft;

    public SelectBoundaryAction() {
        super(tr("Area boundary [testing]"), "selboundary", tr("Select relation or all ways that forms area boundary"),
                Shortcut.registerShortcut("tools:selboundary", tr("Tool: {0}","Area boundary [testing]"),
                KeyEvent.VK_SLASH, Shortcut.SHIFT), true);
        putValue("help", ht("/Action/SelectAreaBoundary"));
    }
    
    public static void selectByInternalPoint(EastNorth e) {
        //Node n= new Node(e);
        TreeMap<Double, OsmPrimitive> found = new TreeMap<>();
        for (Way w: getCurrentDataSet().getWays()) {
            if (w.isUsable() && w.isClosed() )  {
                //if (Geometry.nodeInsidePolygon(n, w.getNodes())) {
                if (NodeWayUtils.isPointInsidePolygon(e, NodeWayUtils.getWayPoints(w))) {
                    found.put(Geometry.closedWayArea(w), w);
                }
            }
        }
        for (Relation r: getCurrentDataSet().getRelations()) {
            if (r.isUsable() && r.isMultipolygon())  {
                //if (Geometry.isNodeInsideMultiPolygon(n, r, null)) {
                if (NodeWayUtils.isPointInsideMultipolygon(e, r)) {
                    for (RelationMember m: r.getMembers()) {
                        if (m.isWay() && m.getWay().isClosed()) {
                            found.values().remove(m.getWay());
                        }
                    }
                    // estimate multipolygon size by its bounding box area
                    BBox bBox = r.getBBox();
                    EastNorth en1 = Main.map.mapView.getProjection().latlon2eastNorth(bBox.getTopLeft());
                    EastNorth en2 = Main.map.mapView.getProjection().latlon2eastNorth(bBox.getBottomRight());
                    double s = Math.abs((en1.east()-en2.east())*(en1.north()-en2.north()));
                    if (s==0) s=1e8;
                    found.put(s, r);
                }
            }
        }
        
        if (!found.isEmpty()) {
            getCurrentDataSet().setSelected(Collections.singletonList(
                found.firstEntry().getValue()));
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Way.class);
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Node.class);
        
        Set<Way> newWays = new HashSet<>();
        
        Way w=null;
        
        if (selectedWays.isEmpty()) {
            if (selectedNodes.size()==1 ) {
                for (OsmPrimitive p : selectedNodes.iterator().next().getReferrers()) {
                    if (p instanceof Way && p.isSelectable()) {
                        //if (w!=null) return; // if we want only one way
                        w=(Way) p;
                        break;
                    }
                }
            } else {
                Point p = Main.map.mapView.getMousePosition();
                selectByInternalPoint(Main.map.mapView.getEastNorth(p.x, p.y));
                return;
            }
        } else if (selectedWays.size()==1)  {
            w = selectedWays.iterator().next();
        } else if (selectedWays.contains(lastUsedStartingWay)) { 
            w=lastUsedStartingWay; //repeated call for selected way
            lastUsedLeft = !lastUsedLeft;
        }

        
        if (w==null) return; //no starting way found
        if (!w.isSelectable()) return;
        if (w.isClosed()) return;
        if (w.getNodesCount()<2) return;

        newWays.add(w);
        lastUsedStartingWay = w;
        
                       
        // try going left at each turn
        if (! NodeWayUtils.addAreaBoundary(w, newWays, lastUsedLeft) ) {
            NodeWayUtils.addAreaBoundary(w, newWays, !lastUsedLeft); // try going right at each turn
        }
        
        if (!newWays.isEmpty() ) {
            getCurrentDataSet().setSelected(newWays);
        } else{
        new Notification(
            tr("Nothing found. Please select way that is a part of some polygon formed by connected ways")
            ).setIcon(JOptionPane.WARNING_MESSAGE).show();            
    }
    }

    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            setEnabled(true);
           // updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
         //   setEnabled(false);
            return;
        }
        setEnabled(true);
        //setEnabled(!selection.isEmpty());
    }
}
