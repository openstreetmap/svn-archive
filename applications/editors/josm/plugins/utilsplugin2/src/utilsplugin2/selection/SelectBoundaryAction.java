// License: GPL. Copyright 2011 by Alexei Kasatkin
package utilsplugin2.selection;

import java.util.LinkedHashSet;
import static org.openstreetmap.josm.gui.help.HelpUtil.ht;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
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
                KeyEvent.VK_SLASH, Shortcut.GROUPS_ALT1+Shortcut.GROUP_EDIT), true);
        putValue("help", ht("/Action/SelectAreaBoundary"));
    }

    public void actionPerformed(ActionEvent e) {
        long t=System.currentTimeMillis();
        Set<Way> selectedWays = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Way.class);
        Set<Node> selectedNodes = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Node.class);
        LinkedHashSet<Relation> selectedRelations = OsmPrimitive.getFilteredSet(getCurrentDataSet().getSelected(), Relation.class);
        
        Set<Way> newWays = new HashSet<Way>();
        
        Way w=null;
        Relation selectedRelation=null;
        
        if (selectedRelations.size()==1) {
            selectedRelation = selectedRelations.iterator().next();
            if (selectedRelation.getMemberPrimitives().contains(lastUsedStartingWay)) {
                w=lastUsedStartingWay; 
                // repeated call for selected relation
            }
        } else if (selectedWays.isEmpty()) {
            if (selectedNodes.size()==1 ) {
                for (OsmPrimitive p : selectedNodes.iterator().next().getReferrers()) {
                    if (p instanceof Way && p.isSelectable()) {
                        //if (w!=null) return; // ifwe want only one way
                        w=(Way) p;
                        break;
                    }
                }
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
        
        List<Relation> rels=new ArrayList<Relation>();
        for (OsmPrimitive p : w.getReferrers()) {
            if (p instanceof Relation && p.isSelectable()) {
                rels.add((Relation) p);
            }
        }
        if (selectedRelation!=null) {
            int idx = rels.indexOf(selectedRelation); 
            // selectedRelation has number idx in active relation list
            if (idx>=0) {
               // select next relation
               if (idx+1<rels.size())
                   getCurrentDataSet().setSelected(Arrays.asList(rels.get(idx+1)));
               else 
                   getCurrentDataSet().setSelected(Arrays.asList(rels.get(0))); 
               return;
            }
        } else if (rels.size()>0) {
               getCurrentDataSet().setSelected(Arrays.asList(rels.get(0)));
               return;
        }

        
        
        // try going left at each turn
        if (! NodeWayUtils.addAreaBoundary(w, newWays, lastUsedLeft) ) {
            NodeWayUtils.addAreaBoundary(w, newWays, !lastUsedLeft); // try going right at each turn
        }
        
        
        
        if (!newWays.isEmpty() ) {
            getCurrentDataSet().setSelected(newWays);
        } else{
        JOptionPane.showMessageDialog(Main.parent,
               tr("Nothing found. Please select way that is a part of some polygon formed by connected ways"),
               tr("Warning"), JOptionPane.WARNING_MESSAGE);
        }

    }

    @Override
    protected void updateEnabledState() {
        if (getCurrentDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getCurrentDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        if (selection == null) {
            setEnabled(false);
            return;
        }
        setEnabled(!selection.isEmpty());
    }


}
